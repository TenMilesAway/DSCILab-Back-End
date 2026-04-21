package com.agileboot.domain.lab.achievement;

import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.lab.achievement.command.CreateLabAchievementCommand;
import com.agileboot.domain.lab.achievement.command.FundAssociationCommand;
import com.agileboot.domain.lab.achievement.db.LabAchievementEntity;
import com.agileboot.domain.lab.achievement.db.LabAchievementService;
import com.agileboot.domain.lab.achievement.dto.LabAchievementDTO;
import com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity;
import com.agileboot.domain.lab.paper.author.LabPaperAuthorService;
import com.agileboot.domain.lab.paper.dto.LabPaperAuthorDTO;
import com.agileboot.domain.lab.paper.dto.PublicPaperAuthorDTO;
import com.agileboot.domain.lab.achievement.dto.LabFundAssociationDTO;
import com.agileboot.domain.lab.achievement.query.LabAchievementQuery;
import com.agileboot.domain.lab.fund.db.LabFundPaperRelEntity;
import com.agileboot.domain.lab.fund.db.LabFundPaperRelService;
import com.agileboot.domain.lab.paper.db.LabPaperEntity;
import com.agileboot.domain.lab.paper.db.LabPaperService;
import com.agileboot.domain.lab.project.author.LabProjectAuthorEntity;
import com.agileboot.domain.lab.project.author.LabProjectAuthorService;
import com.agileboot.domain.lab.project.db.LabProjectEntity;
import com.agileboot.domain.lab.project.db.LabProjectService;
import com.agileboot.domain.lab.user.db.LabUserEntity;
import com.agileboot.domain.lab.user.db.LabUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.agileboot.domain.lab.category.CategoryCompatibilityService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 成果管理应用服务
 */
@Service
@RequiredArgsConstructor
public class LabAchievementApplicationService {

    private final LabAchievementService achievementService;
    private static final DateTimeFormatter PUBLISH_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private static LocalDate parseYearToLocalDate(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String value = raw.trim();
        try {
            if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(value, PUBLISH_DATE_FORMATTER);
            }
            if (value.matches("\\d{4}-\\d{2}")) {
                return LocalDate.parse(value + "-01", PUBLISH_DATE_FORMATTER);
            }
            if (value.matches("\\d{4}")) {
                return LocalDate.of(Integer.parseInt(value), 1, 1);
            }
        } catch (DateTimeParseException ex) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                "发表日期格式应为yyyy-MM-dd");
        }
        throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
            "发表日期格式应为yyyy-MM-dd");
    }

    private static java.time.LocalDate parseYearMonthToLocalDate(String ymStr) {
        if (ymStr == null || ymStr.trim().isEmpty()) return null;
        String s = ymStr.trim();
        if (!s.matches("\\d{4}-\\d{2}")) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "项目年月格式应为yyyy-MM");
        }
        String[] parts = s.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        if (month < 1 || month > 12) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "项目月份需在01-12之间");
        }
        return java.time.LocalDate.of(year, month, 1);
    }

    private final LabPaperAuthorService authorService;
    private final LabProjectAuthorService projectAuthorService;
    private final LabUserService labUserService;
    private final CategoryCompatibilityService categoryCompatibilityService;
    private final com.agileboot.domain.lab.category.db.LabAchievementCategoryService categoryService;
    private final LabPaperService paperService;
    private final LabProjectService projectService;
    private final LabFundPaperRelService fundPaperRelService;
    private java.util.List<Long> getProjectCategoryIds() {
        com.agileboot.domain.lab.category.db.LabAchievementCategoryEntity projectTop =
            categoryService.getByCategoryCode("PROJECT");
        if (projectTop == null) {
            return java.util.Collections.emptyList();
        }
        java.util.List<Long> ids = categoryService.getDescendantIdsIncludeInactive(projectTop.getId());
        java.util.List<Long> result = ids == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(ids);
        result.add(projectTop.getId());
        return result;
    }

    /**
     * 分页查询成果列表
     */
    public PageDTO<LabAchievementDTO> getAchievementList(LabAchievementQuery query) {
        LambdaQueryWrapper<LabAchievementEntity> wrapper = new LambdaQueryWrapper<>();

        // 基础过滤
        wrapper.eq(LabAchievementEntity::getDeleted, false);

        // 关键词搜索
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(LabAchievementEntity::getTitle, query.getKeyword())
                .or().like(LabAchievementEntity::getKeywords, query.getKeyword()));
        }

        // 类型/分类过滤：当传入 categoryId 或 parentCategoryId 时，忽略 type 兼容参数
        if (query.getType() != null && query.getCategoryId() == null && query.getParentCategoryId() == null) {
            wrapper.eq(LabAchievementEntity::getType, query.getType());
        }
        if (Boolean.TRUE.equals(query.getExcludeProject())) {
            wrapper.ne(LabAchievementEntity::getType, 2);
            java.util.List<Long> projectCategoryIds = getProjectCategoryIds();
            if (!projectCategoryIds.isEmpty()) {
                wrapper.notIn(LabAchievementEntity::getCategoryId, projectCategoryIds);
            }
        }
        wrapper.eq(query.getPaperType() != null, LabAchievementEntity::getPaperType, query.getPaperType());
        wrapper.eq(query.getProjectType() != null, LabAchievementEntity::getProjectType, query.getProjectType());
        // 父级分类聚合（管理员查询包含未启用的分类）
        if (query.getParentCategoryId() != null && query.getCategoryId() == null) {
            java.util.List<Long> descendantIds = categoryService.getDescendantIdsIncludeInactive(query.getParentCategoryId());
            if (descendantIds == null || descendantIds.isEmpty()) {
                return new PageDTO<>(java.util.Collections.emptyList(), 0L);
            }
            wrapper.in(LabAchievementEntity::getCategoryId, descendantIds);
        }
        // 二级分类精确
        wrapper.eq(query.getCategoryId() != null, LabAchievementEntity::getCategoryId, query.getCategoryId());

        // 状态过滤
        wrapper.eq(query.getPublished() != null, LabAchievementEntity::getPublished, query.getPublished());
        wrapper.eq(query.getIsVerified() != null, LabAchievementEntity::getIsVerified, query.getIsVerified());
        wrapper.eq(query.getOwnerUserId() != null, LabAchievementEntity::getOwnerUserId, query.getOwnerUserId());

        // 按作者用户ID过滤（仅统计内部作者：user_id=authorUserId 且 deleted=false）
        if (query.getAuthorUserId() != null) {
            java.util.List<Long> byAuthorIds = new java.util.ArrayList<>();
            byAuthorIds.addAll(authorService.lambdaQuery()
                .eq(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getUserId, query.getAuthorUserId())
                .eq(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getDeleted, false)
                .list()
                .stream()
                .map(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getPaperId)
                .collect(Collectors.toList()));
            byAuthorIds.addAll(projectAuthorService.lambdaQuery()
                .eq(LabProjectAuthorEntity::getUserId, query.getAuthorUserId())
                .eq(LabProjectAuthorEntity::getDeleted, false)
                .list()
                .stream()
                .map(LabProjectAuthorEntity::getProjectId)
                .collect(Collectors.toList()));
            byAuthorIds = byAuthorIds.stream().distinct().collect(Collectors.toList());
            if (byAuthorIds.isEmpty()) {
                return new PageDTO<>(java.util.Collections.emptyList(), 0L);
            }
            wrapper.in(LabAchievementEntity::getId, byAuthorIds);
        }

        // 日期范围过滤
        if (query.getDateStart() != null || query.getDateEnd() != null) {
            wrapper.and(w -> {
                // 论文按发表日期
                w.and(w1 -> w1.eq(LabAchievementEntity::getType, 1)
                    .ge(query.getDateStart() != null, LabAchievementEntity::getPublishDate, query.getDateStart())
                    .le(query.getDateEnd() != null, LabAchievementEntity::getPublishDate, query.getDateEnd()))
                // 项目按开始日期
                .or(w2 -> w2.eq(LabAchievementEntity::getType, 2)
                    .ge(query.getDateStart() != null, LabAchievementEntity::getProjectStartDate, query.getDateStart())
                    .le(query.getDateEnd() != null, LabAchievementEntity::getProjectStartDate, query.getDateEnd()));
            });
        }

        // 排序：论文按发表日期，项目按开始日期，都是降序
        wrapper.orderByDesc(LabAchievementEntity::getCreateTime);

        // 使用通用分页工具，自动填充默认页码与大小，避免空指针
        IPage<LabAchievementEntity> result = achievementService.page(query.toPage(), wrapper);

        List<LabAchievementEntity> records = result.getRecords();
        List<LabAchievementDTO> dtoList = records.stream()
            .map(LabAchievementDTO::fromEntity)
            .collect(Collectors.toList());

        // 批量填充作者（管理端列表需要完整作者）
        if (!records.isEmpty()) {
            List<Long> paperIds = records.stream()
                .filter(e -> Integer.valueOf(1).equals(e.getType()))
                .map(LabAchievementEntity::getId)
                .collect(Collectors.toList());
            List<Long> projectIds = records.stream()
                .filter(e -> Integer.valueOf(2).equals(e.getType()))
                .map(LabAchievementEntity::getId)
                .collect(Collectors.toList());

            java.util.Map<Long, List<LabPaperAuthorDTO>> map = new java.util.HashMap<>();
            if (!paperIds.isEmpty()) {
                List<com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity> authors = authorService.getAuthorsByPaperIds(paperIds);
                for (com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity a : authors) {
                    map.computeIfAbsent(a.getPaperId(), k -> new java.util.ArrayList<>())
                       .add(LabPaperAuthorDTO.fromEntity(a));
                }
            }
            if (!projectIds.isEmpty()) {
                List<LabProjectAuthorEntity> projectAuthors = projectAuthorService.getAuthorsByProjectIds(projectIds);
                java.util.Set<Long> projectIdsWithAuthors = new java.util.HashSet<>();
                for (LabProjectAuthorEntity author : projectAuthors) {
                    map.computeIfAbsent(author.getProjectId(), k -> new java.util.ArrayList<>())
                       .add(convertProjectAuthor(author));
                    projectIdsWithAuthors.add(author.getProjectId());
                }
                List<Long> legacyNeeded = projectIds.stream()
                    .filter(id -> !projectIdsWithAuthors.contains(id) || !map.containsKey(id))
                    .collect(Collectors.toList());
                if (!legacyNeeded.isEmpty()) {
                    List<com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity> legacyAuthors = queryLegacyProjectAuthors(legacyNeeded);
                    for (com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity legacy : legacyAuthors) {
                        map.computeIfAbsent(legacy.getPaperId(), k -> new java.util.ArrayList<>())
                           .add(LabPaperAuthorDTO.fromEntity(legacy));
                    }
                }
            }
            for (LabAchievementDTO dto : dtoList) {
                dto.setAuthors(map.getOrDefault(dto.getId(), java.util.Collections.emptyList()));
            }

            java.util.Map<Long, LabPaperEntity> paperMap = new java.util.HashMap<>();
            if (!paperIds.isEmpty()) {
                paperService.listByIds(paperIds).forEach(paper -> {
                    if (paper != null && paper.getId() != null) {
                        paperMap.put(paper.getId(), paper);
                    }
                });
            }

            java.util.Map<Long, LabProjectEntity> projectMap = new java.util.HashMap<>();
            if (!projectIds.isEmpty()) {
                projectService.listByIds(projectIds).forEach(project -> {
                    if (project != null && project.getId() != null) {
                        projectMap.put(project.getId(), project);
                    }
                });
            }

            for (LabAchievementDTO dto : dtoList) {
                mergeChildSnapshotIntoDto(dto, paperMap.get(dto.getId()), projectMap.get(dto.getId()));
            }

            if (!paperIds.isEmpty()) {
                java.util.Map<Long, List<LabFundPaperRelEntity>> fundEntityMap = fundPaperRelService.lambdaQuery()
                    .in(LabFundPaperRelEntity::getPaperId, paperIds)
                    .list()
                    .stream()
                    .collect(Collectors.groupingBy(
                        LabFundPaperRelEntity::getPaperId
                    ));
                for (LabAchievementDTO dto : dtoList) {
                    if (Integer.valueOf(1).equals(dto.getType())) {
                        List<LabFundPaperRelEntity> rels = fundEntityMap.get(dto.getId());
                        if (rels == null) {
                            dto.setFundIds(java.util.Collections.emptyList());
                            dto.setFundAssociations(java.util.Collections.emptyList());
                        } else {
                            List<Long> fundIds = rels.stream()
                                .map(LabFundPaperRelEntity::getFundId)
                                .collect(Collectors.toList());
                            List<LabFundAssociationDTO> fundDtos = rels.stream()
                                .map(rel -> {
                                    LabFundAssociationDTO item = new LabFundAssociationDTO();
                                    item.setFundId(rel.getFundId());
                                    item.setAmount(rel.getAmount());
                                    return item;
                                })
                                .collect(Collectors.toList());
                            dto.setFundIds(fundIds);
                            dto.setFundAssociations(fundDtos);
                        }
                    }
                }
            }
        }

        return new PageDTO<>(dtoList, result.getTotal());
    }

    /**
     * 分页查询成果列表
     */
    public Page<LabAchievementDTO> getAchievementPage(LabAchievementQuery query) {
        Page<LabAchievementEntity> entityPage = achievementService.page(query.toPage(), query.toQueryWrapper());

        // 手动转换分页结果
        Page<LabAchievementDTO> dtoPage = new Page<>();
        dtoPage.setCurrent(entityPage.getCurrent());
        dtoPage.setSize(entityPage.getSize());
        dtoPage.setTotal(entityPage.getTotal());
        dtoPage.setPages(entityPage.getPages());

        // 转换记录
        List<LabAchievementDTO> dtoList = entityPage.getRecords().stream()
            .map(entity -> {
                LabAchievementDTO dto = BeanUtil.copyProperties(entity, LabAchievementDTO.class);
                // 设置分类信息
                if (entity.getCategoryId() != null) {
                    com.agileboot.domain.lab.category.db.LabAchievementCategoryEntity category =
                        categoryService.getById(entity.getCategoryId());
                    if (category != null) {
                        dto.setCategoryName(category.getCategoryName());
                    }
                }
                return dto;
            })
            .collect(Collectors.toList());

        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    /**
     * 获取成果详情
     */
    public LabAchievementDTO getAchievementDetail(Long id) {
        LabAchievementEntity entity = achievementService.getByIdNotDeleted(id);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "成果");
        }

        LabAchievementDTO dto = LabAchievementDTO.fromEntity(entity);
        LabPaperEntity paper = null;
        LabProjectEntity project = null;
        if (Integer.valueOf(1).equals(entity.getType())) {
            paper = paperService.getById(entity.getId());
        } else if (Integer.valueOf(2).equals(entity.getType())) {
            project = projectService.getById(entity.getId());
        }
        mergeChildSnapshotIntoDto(dto, paper, project);

        // 设置所有者姓名
        if (entity.getOwnerUserId() != null) {
            com.agileboot.domain.lab.user.db.LabUserEntity owner = labUserService.getById(entity.getOwnerUserId());
            if (owner != null) {
                dto.setOwnerUserName(owner.getRealName());
            }
        }

        // 填充作者（管理端：全部作者，不过滤）
        if (Integer.valueOf(1).equals(entity.getType())) {
            List<com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity> authors =
                authorService.getAuthorsByPaperId(entity.getId());
            dto.setAuthors(authors.stream()
                .map(com.agileboot.domain.lab.paper.dto.LabPaperAuthorDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList()));
        } else if (Integer.valueOf(2).equals(entity.getType())) {
            List<LabProjectAuthorEntity> authors = projectAuthorService.getAuthorsByProjectId(entity.getId());
            List<LabPaperAuthorDTO> projectAuthorDtos = authors.stream()
                .map(this::convertProjectAuthor)
                .collect(java.util.stream.Collectors.toList());
            if (projectAuthorDtos.isEmpty()) {
                List<com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity> legacy =
                    queryLegacyProjectAuthors(java.util.Collections.singletonList(entity.getId()));
                projectAuthorDtos = legacy.stream()
                    .map(com.agileboot.domain.lab.paper.dto.LabPaperAuthorDTO::fromEntity)
                    .collect(java.util.stream.Collectors.toList());
            }
            dto.setAuthors(projectAuthorDtos);
        } else {
            dto.setAuthors(java.util.Collections.emptyList());
        }

        if (Integer.valueOf(1).equals(entity.getType())) {
            List<LabFundPaperRelEntity> rels = fundPaperRelService.lambdaQuery()
                .eq(LabFundPaperRelEntity::getPaperId, entity.getId())
                .list();
            if (rels != null && !rels.isEmpty()) {
                dto.setFundIds(rels.stream()
                    .map(LabFundPaperRelEntity::getFundId)
                    .collect(Collectors.toList()));
                dto.setFundAssociations(rels.stream()
                    .map(rel -> {
                        LabFundAssociationDTO item = new LabFundAssociationDTO();
                        item.setFundId(rel.getFundId());
                        item.setAmount(rel.getAmount());
                        return item;
                    })
                    .collect(Collectors.toList()));
            } else {
                dto.setFundIds(java.util.Collections.emptyList());
                dto.setFundAssociations(java.util.Collections.emptyList());
            }
        }

        return dto;
    }

    public List<LabFundAssociationDTO> getAchievementFundAssociations(Long achievementId) {
        LabAchievementEntity entity = achievementService.getByIdNotDeleted(achievementId);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "成果");
        }
        if (!Integer.valueOf(1).equals(entity.getType())) {
            throw new ApiException(ErrorCode.Business.COMMON_UNSUPPORTED_OPERATION, "仅论文类成果支持基金关联操作");
        }
        return fundPaperRelService.lambdaQuery()
            .eq(LabFundPaperRelEntity::getPaperId, achievementId)
            .list()
            .stream()
            .map(rel -> {
                LabFundAssociationDTO dto = new LabFundAssociationDTO();
                dto.setFundId(rel.getFundId());
                dto.setAmount(rel.getAmount());
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAchievementFundAssociations(Long achievementId,
                                                  List<FundAssociationCommand> associations,
                                                  Long currentUserId,
                                                  boolean isAdmin) {
        LabAchievementEntity entity = achievementService.getByIdNotDeleted(achievementId);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "成果");
        }
        if (!Integer.valueOf(1).equals(entity.getType())) {
            throw new ApiException(ErrorCode.Business.COMMON_UNSUPPORTED_OPERATION, "仅论文类成果支持基金关联操作");
        }
        if (!isAdmin && !achievementService.canEdit(achievementId, currentUserId, false)) {
            throw new ApiException(ErrorCode.Business.PERMISSION_NOT_ALLOWED_TO_OPERATE);
        }
        java.util.Date now = new java.util.Date();
        updateFundRelations(achievementId, associations == null ? java.util.Collections.emptyList() : associations);
        paperService.lambdaUpdate()
            .eq(LabPaperEntity::getId, achievementId)
            .set(LabPaperEntity::getUpdateTime, now)
            .update();
        achievementService.lambdaUpdate()
            .eq(LabAchievementEntity::getId, achievementId)
            .set(LabAchievementEntity::getUpdateTime, now)
            .update();
    }

    /**
     * 创建成果
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createAchievement(CreateLabAchievementCommand command, Long currentUserId) {
        // 校验业务规则
        // 基于 categoryId/旧字段做一致性预处理与自动补全
        preprocessAchievementCommand(command);
        validateAchievementCommand(command);

        // 创建实体
        LabAchievementEntity entity = new LabAchievementEntity();
        entity.setTitle(command.getTitle());
        entity.setTitleEn(command.getTitleEn());
        entity.setDescription(command.getDescription());
        entity.setKeywords(command.getKeywords());
        entity.setType(command.getType());
        entity.setPaperType(command.getPaperType());
        entity.setProjectType(command.getProjectType());
        entity.setCategoryId(command.getCategoryId());
        entity.setVenue(command.getVenue());
        entity.setPublishDate(parseYearToLocalDate(command.getPublishDate()));
        entity.setProjectStartDate(parseYearMonthToLocalDate(command.getProjectStartDate()));
        entity.setProjectEndDate(parseYearMonthToLocalDate(command.getProjectEndDate()));
        entity.setReference(command.getReference());
        entity.setLinkUrl(command.getLinkUrl());
        entity.setGitUrl(command.getGitUrl());
        entity.setHomepageUrl(command.getHomepageUrl());
        entity.setPdfUrl(command.getPdfUrl());
        entity.setDoi(command.getDoi());
        entity.setFundingAmount(command.getFundingAmount());
        entity.setOwnerUserId(currentUserId);
        entity.setPublished(command.getPublished() != null ? command.getPublished() : false);
        entity.setIsVerified(false); // 新创建的成果默认未审核
        entity.setExtra(command.getExtra());
        entity.setDeleted(false);
        entity.setCreateTime(new java.util.Date());
        entity.setUpdateTime(new java.util.Date());

        achievementService.save(entity);
        syncSplitTables(entity, command);
        syncFundRelations(entity, command);

        // 批量创建作者（如果提供）
        if (command.getAuthors() != null && !command.getAuthors().isEmpty()) {
            // 创建场景也启用 upsert：若存在同一(achievement_id,user_id)的软删记录则复活，避免唯一键冲突
            createAuthorsForAchievement(entity.getId(), command.getAuthors(), true);
        }

        return entity.getId();
    }

    /**
     * 更新成果
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAchievement(Long id, CreateLabAchievementCommand command, Long currentUserId, boolean isAdmin) {
        // 检查成果是否存在
        LabAchievementEntity entity = achievementService.getByIdNotDeleted(id);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "成果");
        }

        // 权限检查
        if (!isAdmin && !achievementService.canEdit(id, currentUserId, false)) {
            throw new ApiException(ErrorCode.Business.PERMISSION_NOT_ALLOWED_TO_OPERATE);
        }

        // 如果是非管理员修改已审核的成果：允许修改，但需要打回审核状态
        boolean needResetVerify = Boolean.TRUE.equals(entity.getIsVerified()) && !isAdmin;
        if (needResetVerify) {
            entity.setIsVerified(false);
        }

        // 校验业务规则
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        //  
        // 
        //  
        // 
        // 
        // 
        // 
        //  
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        //
        //  
        //   
        //
        //
        //
        // 
        //
        // 
        //
        //
        // 
        //
        // 
        // 
        // 
        //  
        //  
        // 
        // 
        // 
        // 
        //
        //
        // 
        // 
        // 
        // 
        // 
        // 
        //
        // 
        //
        //
        //
        //
        // 
        // 
        //
        // 
        //
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        //
        // 
        //
        //
        // 
        // 
        // 
        //
        // 
        //
        // 
        // 
        // 
        //
        //
        // 
        //
        //
        // 
        // 
        //
        // 
        // 
        // 
        // 
        // 
        //
        //
        // 
        // 
        // 
        // 
        // 
        // 
        //
        // 
        // 
        //
        //
        // 
        // 
        // 
        //
        //
        // 
        // 
        //
        // 
        // 
        // 
        // 
        //
        // 
        // 
        //
        //
        // 
        // 
        //
        //
        //
        // 
        //
        // 
        // 
        // 
        //
        //
        // 
        //
        // 
        //
        // 
        //
        // 
        //
        // 
        // 
        // 
        //
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        // 
        //
        // 
        // 
        // 
        // 
        // 
        //
        // 
        // 
        // 
        //
        // 
        // 
        // 
        // 
        // 
        //
        //
        //
        //
        //
        //
        // 
        //
        //
        // 
        //
        //
        //
        //
        //
        // 
        // 
        //
        // 
        // 
        //
        //
        //
        //
        //
        // 
        //
        // 
        //
        // 
        //
        //
        //
        preprocessAchievementCommand(command);
        validateAchievementCommand(command);

        // 更新字段（标题、描述等始终可改）
        entity.setTitle(command.getTitle());
        entity.setTitleEn(command.getTitleEn());
        entity.setDescription(command.getDescription());
        entity.setKeywords(command.getKeywords());

        // 关键字段：若当前未审核或管理员，或者刚刚打回审核（needResetVerify=true）则允许修改
        if (!Boolean.TRUE.equals(entity.getIsVerified()) || isAdmin || needResetVerify) {
            entity.setType(command.getType());
            entity.setPaperType(command.getPaperType());
            entity.setProjectType(command.getProjectType());
            entity.setCategoryId(command.getCategoryId());
            entity.setPublishDate(parseYearToLocalDate(command.getPublishDate()));
            entity.setProjectStartDate(parseYearMonthToLocalDate(command.getProjectStartDate()));
            entity.setProjectEndDate(parseYearMonthToLocalDate(command.getProjectEndDate()));
            entity.setDoi(command.getDoi());
        }

        entity.setVenue(command.getVenue());
        entity.setReference(command.getReference());
        entity.setLinkUrl(command.getLinkUrl());
        entity.setGitUrl(command.getGitUrl());
        entity.setHomepageUrl(command.getHomepageUrl());
        entity.setPdfUrl(command.getPdfUrl());
        entity.setFundingAmount(command.getFundingAmount());
        entity.setPublished(command.getPublished()); // 修复：添加published字段的更新
        entity.setExtra(command.getExtra());
        entity.setUpdateTime(new java.util.Date());

        // 使用UpdateWrapper强制更新null值
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<LabAchievementEntity> updateWrapper =
            new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        updateWrapper.eq("id", entity.getId())
            .set("title", entity.getTitle())
            .set("title_en", entity.getTitleEn())
            .set("description", entity.getDescription())
            .set("keywords", entity.getKeywords())
            .set("type", entity.getType())
            .set("paper_type", entity.getPaperType())
            .set("project_type", entity.getProjectType())
            .set("category_id", entity.getCategoryId())
            .set("venue", entity.getVenue())
            .set("publish_date", entity.getPublishDate())
            .set("project_start_date", entity.getProjectStartDate())
            .set("project_end_date", entity.getProjectEndDate())
            .set("reference", entity.getReference())
            .set("link_url", entity.getLinkUrl())
            .set("git_url", entity.getGitUrl())
            .set("homepage_url", entity.getHomepageUrl())
            .set("pdf_url", entity.getPdfUrl())
            .set("doi", entity.getDoi())
            .set("funding_amount", entity.getFundingAmount())
            .set("published", entity.getPublished())
            .set("is_verified", entity.getIsVerified())
            .set("extra", entity.getExtra())
            .set("update_time", entity.getUpdateTime());

        syncSplitTables(entity, command);
        achievementService.update(updateWrapper);
        syncFundRelations(entity, command);

        // 若提交了作者列表，则替换（全量覆盖）：先软删旧作者，再按新列表创建
        if (command.getAuthors() != null) {
            System.out.println("DEBUG: 开始处理作者列表，成果ID=" + id + "，作者数量=" + command.getAuthors().size());
            int removed = authorService.hardDeleteAllByPaperId(id);
            System.out.println("DEBUG: 已物理删除原作者记录数量=" + removed);
            createAuthorsForAchievement(id, command.getAuthors(), true);
            System.out.println("DEBUG: 作者列表处理完成");
        } else {
            System.out.println("DEBUG: 未提供作者列表，跳过作者处理");
        }
    }

    /**
     * 删除成果
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAchievement(Long id, Long currentUserId, boolean isAdmin) {
        // 检查成果是否存在
        LabAchievementEntity entity = achievementService.getByIdNotDeleted(id);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "成果");
        }

        // 权限检查：所有实验室成员（lab_user表中的用户）都可以删除成果
        // 管理员当然也可以删除
        // 外部作者无法登录系统，因此无法调用到这个方法
        // 能调用到这里说明用户已经通过身份验证，是实验室成员
        if (!isAdmin) {
            // 所有实验室成员都可以删除成果，无需额外权限检查
            // 这里可以添加日志记录删除操作
        }

        // 先删除成果（软删除）
        boolean achievementDeleted = achievementService.lambdaUpdate()
            .eq(LabAchievementEntity::getId, id)
            .set(LabAchievementEntity::getDeleted, true)
            .set(LabAchievementEntity::getUpdateTime, new java.util.Date())
            .update();

        if (!achievementDeleted) {
            throw new ApiException(ErrorCode.Business.COMMON_UNSUPPORTED_OPERATION, "成果删除失败");
        }

        // 同步拆分表的删除状态
        paperService.lambdaUpdate()
            .eq(LabPaperEntity::getId, id)
            .set(LabPaperEntity::getDeleted, true)
            .set(LabPaperEntity::getUpdateTime, new java.util.Date())
            .update();
        projectService.lambdaUpdate()
            .eq(LabProjectEntity::getId, id)
            .set(LabProjectEntity::getDeleted, true)
            .set(LabProjectEntity::getUpdateTime, new java.util.Date())
            .update();

        // 成果删除成功后，级联删除所有相关的作者关联记录
        // 因为成果已经不存在，这些关联记录就没有意义了，应该彻底清理
        authorService.hardDeleteAllByPaperId(id);
    }

    /**
     * 发布/取消发布成果
     */
    @Transactional(rollbackFor = Exception.class)
    public void publishAchievement(Long id, Boolean published, Long currentUserId, boolean isAdmin) {
        // 检查成果是否存在
        LabAchievementEntity entity = achievementService.getByIdNotDeleted(id);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "成果");
        }

        // 权限检查：
        // - 设置为 true（发布）：仅管理员
        // - 设置为 false（隐藏）：管理员 或 老师 且“参与了该成果”（内部作者）均可
        if (Boolean.TRUE.equals(published)) {
            if (!isAdmin) {
                throw new ApiException(ErrorCode.Business.PERMISSION_NOT_ALLOWED_TO_OPERATE);
            }
        } else {
            boolean isTeacher = false;
            com.agileboot.domain.lab.user.db.LabUserEntity u = labUserService.getById(currentUserId);
            if (u != null && com.agileboot.domain.lab.user.db.LabUserEntity.Identity.TEACHER.getCode().equals(u.getIdentity())) {
                isTeacher = true;
            }
            boolean isParticipant = isAchievementAuthor(id, currentUserId);
            boolean isOwner = achievementService.isOwner(id, currentUserId);
            if (!(isAdmin || (isTeacher && (isOwner || isParticipant)))) {
                throw new ApiException(ErrorCode.Business.PERMISSION_NOT_ALLOWED_TO_OPERATE);
            }
        }

        entity.setPublished(published);
        entity.setUpdateTime(new java.util.Date());
        achievementService.updateById(entity);
        syncPublishState(entity);
    }

    /**
     * 审核/取消审核成果
     */
    @Transactional(rollbackFor = Exception.class)
    public void verifyAchievement(Long id, Boolean verified, Long currentUserId, boolean isAdmin) {
        // 检查成果是否存在
        LabAchievementEntity entity = achievementService.getByIdNotDeleted(id);
        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "成果");
        }

        // 权限检查：仅管理员可审核
        if (!isAdmin) {
            throw new ApiException(ErrorCode.Business.PERMISSION_NOT_ALLOWED_TO_OPERATE);
        }

        entity.setIsVerified(verified);
        entity.setUpdateTime(new java.util.Date());
        achievementService.updateById(entity);
        syncVerifyState(entity);
    }

    private void syncPublishState(LabAchievementEntity entity) {
        paperService.lambdaUpdate()
            .eq(LabPaperEntity::getId, entity.getId())
            .set(LabPaperEntity::getPublished, entity.getPublished())
            .set(LabPaperEntity::getUpdateTime, entity.getUpdateTime())
            .update();
        projectService.lambdaUpdate()
            .eq(LabProjectEntity::getId, entity.getId())
            .set(LabProjectEntity::getPublished, entity.getPublished())
            .set(LabProjectEntity::getUpdateTime, entity.getUpdateTime())
            .update();
    }

    private void syncVerifyState(LabAchievementEntity entity) {
        paperService.lambdaUpdate()
            .eq(LabPaperEntity::getId, entity.getId())
            .set(LabPaperEntity::getIsVerified, entity.getIsVerified())
            .set(LabPaperEntity::getUpdateTime, entity.getUpdateTime())
            .update();
        projectService.lambdaUpdate()
            .eq(LabProjectEntity::getId, entity.getId())
            .set(LabProjectEntity::getIsVerified, entity.getIsVerified())
            .set(LabProjectEntity::getUpdateTime, entity.getUpdateTime())
            .update();
    }

    private void mergeChildSnapshotIntoDto(LabAchievementDTO dto, LabPaperEntity paper, LabProjectEntity project) {
        if (dto == null) {
            return;
        }
        if (paper != null) {
            dto.setTitle(paper.getTitle());
            dto.setTitleEn(paper.getTitleEn());
            dto.setDescription(paper.getDescription());
            dto.setKeywords(paper.getKeywords());
            dto.setPaperType(paper.getPaperTypeId());
            dto.setVenue(paper.getPublication());
            dto.setPublishDate(paper.getPublishDate());
            dto.setReference(paper.getReference());
            dto.setDoi(paper.getDoi());
            dto.setLinkUrl(paper.getLinkUrl());
            dto.setGitUrl(paper.getGitUrl());
            dto.setHomepageUrl(paper.getHomepageUrl());
            dto.setPdfUrl(paper.getPdfUrl());
            dto.setOwnerUserId(paper.getOwnerUserId());
            dto.setPublished(paper.getPublished());
            dto.setIsVerified(paper.getIsVerified());
            dto.setExtra(paper.getExtra());
            dto.setCreateTime(paper.getCreateTime());
            dto.setUpdateTime(paper.getUpdateTime());
        } else if (project != null) {
            dto.setTitle(project.getTitle());
            dto.setTitleEn(project.getTitleEn());
            dto.setDescription(project.getDescription());
            dto.setKeywords(project.getKeywords());
            dto.setProjectType(project.getProjectTypeId());
            dto.setProjectStartDate(project.getProjectStartDate());
            dto.setProjectEndDate(project.getProjectEndDate());
            dto.setFundingAmount(project.getFundingAmount());
            dto.setReference(project.getReference());
            dto.setLinkUrl(project.getLinkUrl());
            dto.setGitUrl(project.getGitUrl());
            dto.setHomepageUrl(project.getHomepageUrl());
            dto.setPdfUrl(project.getPdfUrl());
            dto.setOwnerUserId(project.getOwnerUserId());
            dto.setPublished(project.getPublished());
            dto.setIsVerified(project.getIsVerified());
            dto.setExtra(project.getExtra());
            dto.setCreateTime(project.getCreateTime());
            dto.setUpdateTime(project.getUpdateTime());
        }
    }

    private void mergeChildSnapshotIntoPublicDto(com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO dto,
                                                 LabPaperEntity paper,
                                                 LabProjectEntity project) {
        if (dto == null) {
            return;
        }
        if (paper != null) {
            dto.setTitle(paper.getTitle());
            dto.setTitleEn(paper.getTitleEn());
            dto.setDescription(paper.getDescription());
            dto.setKeywords(paper.getKeywords());
            dto.setPaperType(paper.getPaperTypeId());
            dto.setVenue(paper.getPublication());
            dto.setPublishDate(paper.getPublishDate());
            dto.setReference(paper.getReference());
            dto.setDoi(paper.getDoi());
            dto.setLinkUrl(paper.getLinkUrl());
            dto.setGitUrl(paper.getGitUrl());
            dto.setHomepageUrl(paper.getHomepageUrl());
            dto.setPdfUrl(paper.getPdfUrl());
            dto.setPublished(paper.getPublished());
            dto.setExtra(paper.getExtra());
            dto.setCreateTime(paper.getCreateTime());
        } else if (project != null) {
            dto.setTitle(project.getTitle());
            dto.setTitleEn(project.getTitleEn());
            dto.setDescription(project.getDescription());
            dto.setKeywords(project.getKeywords());
            dto.setProjectType(project.getProjectTypeId());
            dto.setProjectStartDate(project.getProjectStartDate());
            dto.setProjectEndDate(project.getProjectEndDate());
            dto.setFundingAmount(project.getFundingAmount());
            dto.setReference(project.getReference());
            dto.setLinkUrl(project.getLinkUrl());
            dto.setGitUrl(project.getGitUrl());
            dto.setHomepageUrl(project.getHomepageUrl());
            dto.setPdfUrl(project.getPdfUrl());
            dto.setPublished(project.getPublished());
            dto.setExtra(project.getExtra());
            dto.setCreateTime(project.getCreateTime());
        }
    }

    private void syncSplitTables(LabAchievementEntity entity, CreateLabAchievementCommand command) {
        Integer resolvedType = resolveType(command, entity);
        if (Integer.valueOf(1).equals(resolvedType)) {
            upsertPaper(entity, command);
            projectService.removeById(entity.getId());
        } else if (Integer.valueOf(2).equals(resolvedType)) {
            upsertProject(entity, command);
            paperService.removeById(entity.getId());
        } else {
            paperService.removeById(entity.getId());
            projectService.removeById(entity.getId());
        }
    }

    private void syncFundRelations(LabAchievementEntity entity, CreateLabAchievementCommand command) {
        Integer resolvedType = resolveType(command, entity);
        if (!Integer.valueOf(1).equals(resolvedType)) {
            fundPaperRelService.lambdaUpdate()
                .eq(LabFundPaperRelEntity::getPaperId, entity.getId())
                .remove();
            return;
        }

        List<FundAssociationCommand> funds = resolveFundAssociations(command);
        updateFundRelations(entity.getId(), funds);
    }

    private List<FundAssociationCommand> resolveFundAssociations(CreateLabAchievementCommand command) {
        if (command == null) {
            return java.util.Collections.emptyList();
        }
        if (command.getFundAssociations() != null && !command.getFundAssociations().isEmpty()) {
            return command.getFundAssociations().stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getFundId() != null)
                .collect(Collectors.toList());
        }
        if (command.getFundIds() != null && !command.getFundIds().isEmpty()) {
            return command.getFundIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(fundId -> {
                    FundAssociationCommand cmd = new FundAssociationCommand();
                    cmd.setFundId(fundId);
                    cmd.setAmount(null);
                    return cmd;
                })
                .collect(Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }

    private void updateFundRelations(Long paperId, List<FundAssociationCommand> associations) {
        fundPaperRelService.lambdaUpdate()
            .eq(LabFundPaperRelEntity::getPaperId, paperId)
            .remove();

        if (associations == null || associations.isEmpty()) {
            return;
        }

        java.util.LinkedHashMap<Long, java.math.BigDecimal> merged = new java.util.LinkedHashMap<>();
        for (FundAssociationCommand association : associations) {
            Long fundId = association.getFundId();
            if (fundId == null) {
                continue;
            }
            java.math.BigDecimal amount = association.getAmount();
            merged.put(fundId, amount);
        }

        if (merged.isEmpty()) {
            return;
        }

        java.util.Date now = new java.util.Date();
        List<LabFundPaperRelEntity> relations = merged.entrySet().stream()
            .map(entry -> {
                LabFundPaperRelEntity rel = new LabFundPaperRelEntity();
                rel.setFundId(entry.getKey());
                rel.setPaperId(paperId);
                rel.setAmount(entry.getValue());
                rel.setCreateTime(now);
                rel.setUpdateTime(now);
                return rel;
            })
            .collect(Collectors.toList());
        fundPaperRelService.saveBatch(relations);
    }

    private Integer resolveType(CreateLabAchievementCommand command, LabAchievementEntity entity) {
        if (command != null && command.getResolvedType() != null) {
            return command.getResolvedType();
        }
        return entity.getType();
    }

    private Integer resolveSubType(CreateLabAchievementCommand command, LabAchievementEntity entity) {
        if (command != null && command.getResolvedSubType() != null) {
            return command.getResolvedSubType();
        }
        if (entity.getType() != null) {
            if (Integer.valueOf(1).equals(entity.getType())) {
                return entity.getPaperType();
            }
            if (Integer.valueOf(2).equals(entity.getType())) {
                return entity.getProjectType();
            }
        }
        return null;
    }

    private void upsertPaper(LabAchievementEntity entity, CreateLabAchievementCommand command) {
        LabPaperEntity existing = paperService.getById(entity.getId());
        LabPaperEntity paper = existing != null ? existing : new LabPaperEntity();
        paper.setId(entity.getId());
        paper.setTitle(entity.getTitle());
        paper.setTitleEn(entity.getTitleEn());
        paper.setDescription(entity.getDescription());
        paper.setKeywords(entity.getKeywords());
        paper.setPaperTypeId(resolveSubType(command, entity));
        paper.setPublication(entity.getVenue());
        paper.setPublishDate(entity.getPublishDate());
        paper.setPublishDateDisplay(entity.getPublishDate() != null ? entity.getPublishDate().toString() : null);
        paper.setReference(entity.getReference());
        paper.setDoi(entity.getDoi());
        paper.setLinkUrl(entity.getLinkUrl());
        paper.setExtraUrl(entity.getLinkUrl());
        paper.setGitUrl(entity.getGitUrl());
        paper.setHomepageUrl(entity.getHomepageUrl());
        paper.setPdfUrl(entity.getPdfUrl());
        paper.setNotes(entity.getDescription());
        paper.setOwnerUserId(entity.getOwnerUserId());
        paper.setPublished(entity.getPublished());
        paper.setIsVerified(entity.getIsVerified());
        paper.setExtra(entity.getExtra());
        paper.setCreatorId(entity.getCreatorId());
        paper.setUpdaterId(entity.getUpdaterId());
        paper.setDeleted(Boolean.TRUE.equals(entity.getDeleted()));
        paper.setUpdateTime(entity.getUpdateTime());
        if (existing != null && existing.getCreateTime() != null) {
            paper.setCreateTime(existing.getCreateTime());
        } else {
            paper.setCreateTime(entity.getCreateTime());
        }

        if (existing == null) {
            paperService.save(paper);
        } else {
            paperService.updateById(paper);
        }
    }

    private void upsertProject(LabAchievementEntity entity, CreateLabAchievementCommand command) {
        LabProjectEntity existing = projectService.getById(entity.getId());
        LabProjectEntity project = existing != null ? existing : new LabProjectEntity();
        project.setId(entity.getId());
        project.setTitle(entity.getTitle());
        project.setTitleEn(entity.getTitleEn());
        project.setDescription(entity.getDescription());
        project.setKeywords(entity.getKeywords());
        project.setProjectTypeId(resolveSubType(command, entity));
        project.setProjectStartDate(entity.getProjectStartDate());
        project.setProjectEndDate(entity.getProjectEndDate());
        project.setFundingAmount(entity.getFundingAmount());
        project.setReference(entity.getReference());
        project.setLinkUrl(entity.getLinkUrl());
        project.setGitUrl(entity.getGitUrl());
        project.setHomepageUrl(entity.getHomepageUrl());
        project.setPdfUrl(entity.getPdfUrl());
        project.setOwnerUserId(entity.getOwnerUserId());
        project.setPublished(entity.getPublished());
        project.setIsVerified(entity.getIsVerified());
        project.setExtra(entity.getExtra());
        project.setCreatorId(entity.getCreatorId());
        project.setUpdaterId(entity.getUpdaterId());
        project.setDeleted(Boolean.TRUE.equals(entity.getDeleted()));
        project.setUpdateTime(entity.getUpdateTime());
        if (existing != null && existing.getCreateTime() != null) {
            project.setCreateTime(existing.getCreateTime());
        } else {
            project.setCreateTime(entity.getCreateTime());
        }

        if (existing == null) {
            projectService.save(project);
        } else {
            projectService.updateById(project);
        }
    }

    /**
     * 预处理（强制只用 categoryId）
     * - 必须传二级（叶子）categoryId；否则报错
     * - 始终以 categoryId 为准，自动推导并覆盖 type 及 paperType/projectType（忽略客户端传入的旧字段）
     */
    private void preprocessAchievementCommand(CreateLabAchievementCommand command) {
        Long categoryId = command.getCategoryId();
        if (categoryId == null) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "必须传二级分类ID：categoryId");
        }
        com.agileboot.domain.lab.category.CategoryCompatibilityService.LegacyTypeMapping mapping =
            categoryCompatibilityService.getLegacyTypeByCategory(categoryId);
        if (mapping == null) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                "无效的成果类型ID: " + categoryId + "（请传有效的二级分类ID）");
        }
        Integer derivedType = mapping.getType();
        Integer derivedSubType = mapping.getSubType();
        if (derivedSubType == null) {
            // 如果传的是一级分类：尝试自动降级为该大类下的“其他…”叶子分类
            Long leafId = categoryCompatibilityService.resolveWritableLeafCategoryId(categoryId);
            if (leafId == null || leafId.equals(categoryId)) {
                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                    "必须传二级分类ID：categoryId=" + categoryId + "（该分类下暂无二级分类）");
            }
            // 用可写入的叶子ID替换
            command.setCategoryId(leafId);
            categoryId = leafId;
            // 重新计算映射
            mapping = categoryCompatibilityService.getLegacyTypeByCategory(leafId);
            if (mapping == null) {
                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                    "无效的成果类型ID: " + leafId + "（请传有效的二级分类ID）");
            }
            derivedType = mapping.getType();
            derivedSubType = mapping.getSubType();
        }

        // 缓存映射结果，供后续同步拆分表使用
        command.setResolvedType(derivedType);
        command.setResolvedSubType(derivedSubType);

        // 设置 legacy 字段，保持与新分类的兼容
        if (derivedType != null) {
            command.setType(derivedType);
            if (Integer.valueOf(1).equals(derivedType)) {
                command.setPaperType(derivedSubType);
                command.setProjectType(null);
            } else if (Integer.valueOf(2).equals(derivedType)) {
                command.setPaperType(null);
                command.setProjectType(derivedSubType);
            } else {
                command.setPaperType(null);
                command.setProjectType(null);
            }
        } else {
            command.setPaperType(null);
            command.setProjectType(null);
        }
    }

    /**
     * 校验成果命令
     */
    private void validateAchievementCommand(CreateLabAchievementCommand command) {
        // 基础校验：只验证必填字段
        if (command.getCategoryId() == null) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "成果分类不能为空");
        }
        if (command.getTitle() == null || command.getTitle().trim().isEmpty()) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "成果标题不能为空");
        }

        // 日期校验：如果同时设置了项目开始和结束日期，检查逻辑
        if (StringUtils.hasText(command.getProjectStartDate()) && StringUtils.hasText(command.getProjectEndDate())) {
            java.time.LocalDate start = parseYearMonthToLocalDate(command.getProjectStartDate());
            java.time.LocalDate end = parseYearMonthToLocalDate(command.getProjectEndDate());
            if (end != null && start != null && end.isBefore(start)) {
                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "项目结束日期不能早于开始日期");
            }
        }
    }

    /**
     * 获取我的成果列表（我拥有的 + 我参与的）
     */
    public PageDTO<LabAchievementDTO> getMyAchievementList(com.agileboot.domain.lab.achievement.query.MyAchievementQuery query, Long currentUserId) {
        // 查询我参与的成果ID（作为作者，显示所有参与的成果，不管visible设置）
        List<Long> authorAchievementIds = new java.util.ArrayList<>();
        authorAchievementIds.addAll(authorService.lambdaQuery()
            .eq(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getUserId, currentUserId)
            .eq(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getDeleted, false)
            .list()
            .stream()
            .map(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getPaperId)
            .collect(Collectors.toList()));
        authorAchievementIds.addAll(projectAuthorService.lambdaQuery()
            .eq(LabProjectAuthorEntity::getUserId, currentUserId)
            .eq(LabProjectAuthorEntity::getDeleted, false)
            .list()
            .stream()
            .map(LabProjectAuthorEntity::getProjectId)
            .collect(Collectors.toList()));
        final List<Long> mergedAuthorAchievementIds = authorAchievementIds.stream()
            .distinct()
            .collect(Collectors.toList());

        // 合并条件：我拥有的 OR 我参与的
        LambdaQueryWrapper<LabAchievementEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabAchievementEntity::getDeleted, false)
               .and(w -> {
                   w.eq(LabAchievementEntity::getOwnerUserId, currentUserId);
                   if (!mergedAuthorAchievementIds.isEmpty()) {
                       w.or().in(LabAchievementEntity::getId, mergedAuthorAchievementIds);
                   }
               });

        // 应用查询条件
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(LabAchievementEntity::getTitle, query.getKeyword())
                .or().like(LabAchievementEntity::getKeywords, query.getKeyword()));
        }
        // 当传入 categoryId 或 parentCategoryId 时，忽略 type 兼容参数
        if (query.getType() != null && query.getCategoryId() == null && query.getParentCategoryId() == null) {
            wrapper.eq(LabAchievementEntity::getType, query.getType());
        }
        if (Boolean.TRUE.equals(query.getExcludeProject())) {
            wrapper.ne(LabAchievementEntity::getType, 2);
            java.util.List<Long> projectCategoryIds = getProjectCategoryIds();
            if (!projectCategoryIds.isEmpty()) {
                wrapper.notIn(LabAchievementEntity::getCategoryId, projectCategoryIds);
            }
        }

        //         
        wrapper.eq(query.getPaperType() != null, LabAchievementEntity::getPaperType, query.getPaperType());
        wrapper.eq(query.getProjectType() != null, LabAchievementEntity::getProjectType, query.getProjectType());
        wrapper.eq(query.getPublished() != null, LabAchievementEntity::getPublished, query.getPublished());
        wrapper.eq(query.getIsVerified() != null, LabAchievementEntity::getIsVerified, query.getIsVerified());

        // 分类筛选（我的成果）：支持一级聚合或二级精确，包含未启用的分类
        if (query.getParentCategoryId() != null && query.getCategoryId() == null) {
            java.util.List<Long> descendantIds = categoryService.getDescendantIdsIncludeInactive(query.getParentCategoryId());
            if (descendantIds == null || descendantIds.isEmpty()) {
                return new PageDTO<>(java.util.Collections.emptyList(), 0L);
            }
            wrapper.in(LabAchievementEntity::getCategoryId, descendantIds);
        }
        wrapper.eq(query.getCategoryId() != null, LabAchievementEntity::getCategoryId, query.getCategoryId());


        // 仅在“我的成果”范围内添加姓名过滤（拥有者/作者）
        if (StringUtils.hasText(query.getOwnerName())) {
            java.util.List<Long> ownerIds = labUserService.lambdaQuery()
                .eq(com.agileboot.domain.lab.user.db.LabUserEntity::getDeleted, false)
                .and(w -> w.like(com.agileboot.domain.lab.user.db.LabUserEntity::getRealName, query.getOwnerName())
                         .or().like(com.agileboot.domain.lab.user.db.LabUserEntity::getEnglishName, query.getOwnerName()))
                .list()
                .stream().map(com.agileboot.domain.lab.user.db.LabUserEntity::getId)
                .collect(java.util.stream.Collectors.toList());
            if (ownerIds.isEmpty()) {
                return new PageDTO<>(java.util.Collections.emptyList(), 0L);
            }
            wrapper.in(LabAchievementEntity::getOwnerUserId, ownerIds);
        }
        if (StringUtils.hasText(query.getAuthorName())) {
            java.util.Set<Long> byAuthor = new java.util.HashSet<>();
            // 作者表 name/name_en 模糊
            java.util.List<com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity> nameMatched = authorService.lambdaQuery()
                .eq(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getDeleted, false)
                .and(w -> w.like(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getName, query.getAuthorName())
                         .or().like(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getNameEn, query.getAuthorName()))
                .list();
            nameMatched.forEach(a -> byAuthor.add(a.getPaperId()));
            java.util.List<LabProjectAuthorEntity> projectNameMatched = projectAuthorService.lambdaQuery()
                .eq(LabProjectAuthorEntity::getDeleted, false)
                .and(w -> w.like(LabProjectAuthorEntity::getName, query.getAuthorName())
                    .or().like(LabProjectAuthorEntity::getNameEn, query.getAuthorName()))
                .list();
            projectNameMatched.forEach(a -> byAuthor.add(a.getProjectId()));
            // 内部作者按 lab_user 姓名模糊 -> 反查成就ID
            java.util.List<Long> userIds = labUserService.lambdaQuery()
                .eq(com.agileboot.domain.lab.user.db.LabUserEntity::getDeleted, false)
                .and(w -> w.like(com.agileboot.domain.lab.user.db.LabUserEntity::getRealName, query.getAuthorName())
                         .or().like(com.agileboot.domain.lab.user.db.LabUserEntity::getEnglishName, query.getAuthorName()))
                .list().stream().map(com.agileboot.domain.lab.user.db.LabUserEntity::getId)
                .collect(java.util.stream.Collectors.toList());
            if (!userIds.isEmpty()) {
                java.util.List<Long> byUser = authorService.lambdaQuery()
                    .eq(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getDeleted, false)
                    .in(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getUserId, userIds)
                    .list().stream().map(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getPaperId)
                    .collect(java.util.stream.Collectors.toList());
                byAuthor.addAll(byUser);
                java.util.List<Long> projectByUser = projectAuthorService.lambdaQuery()
                    .eq(LabProjectAuthorEntity::getDeleted, false)
                    .in(LabProjectAuthorEntity::getUserId, userIds)
                    .list().stream().map(LabProjectAuthorEntity::getProjectId)
                    .collect(java.util.stream.Collectors.toList());
                byAuthor.addAll(projectByUser);
            }
            if (byAuthor.isEmpty()) {
                return new PageDTO<>(java.util.Collections.emptyList(), 0L);
            }
            wrapper.in(LabAchievementEntity::getId, byAuthor);
        }


        // 日期范围过滤
        if (query.getDateStart() != null || query.getDateEnd() != null) {
            wrapper.and(w -> {
                w.and(w1 -> w1.eq(LabAchievementEntity::getType, 1)
                    .ge(query.getDateStart() != null, LabAchievementEntity::getPublishDate, query.getDateStart())
                    .le(query.getDateEnd() != null, LabAchievementEntity::getPublishDate, query.getDateEnd()))
                .or(w2 -> w2.eq(LabAchievementEntity::getType, 2)
                    .ge(query.getDateStart() != null, LabAchievementEntity::getProjectStartDate, query.getDateStart())
                    .le(query.getDateEnd() != null, LabAchievementEntity::getProjectStartDate, query.getDateEnd()));
            });
        }

        wrapper.orderByDesc(LabAchievementEntity::getCreateTime);

        IPage<LabAchievementEntity> result = achievementService.page(query.toPage(), wrapper);

        List<LabAchievementDTO> dtoList = result.getRecords().stream()
            .map(LabAchievementDTO::fromEntity)
            .collect(Collectors.toList());

        // 填充作者（我的成果：显示全部作者，不过滤 visible）
        if (result.getTotal() > 0) {
            java.util.List<Long> paperIds = result.getRecords().stream()
                .filter(e -> Integer.valueOf(1).equals(e.getType()))
                .map(LabAchievementEntity::getId)
                .collect(Collectors.toList());
            java.util.List<Long> projectIds = result.getRecords().stream()
                .filter(e -> Integer.valueOf(2).equals(e.getType()))
                .map(LabAchievementEntity::getId)
                .collect(Collectors.toList());

            java.util.Map<Long, java.util.List<LabPaperAuthorDTO>> map = new java.util.HashMap<>();
            java.util.Map<Long, Boolean> myVisibilityMap = new java.util.HashMap<>();

            if (!paperIds.isEmpty()) {
                java.util.List<com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity> authors = authorService.getAuthorsByPaperIds(paperIds);
                for (com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity a : authors) {
                    map.computeIfAbsent(a.getPaperId(), k -> new java.util.ArrayList<>())
                       .add(LabPaperAuthorDTO.fromEntity(a));
                    if (currentUserId.equals(a.getUserId())) {
                        myVisibilityMap.put(a.getPaperId(), a.getVisible());
                    }
                }
            }
            if (!projectIds.isEmpty()) {
                java.util.List<LabProjectAuthorEntity> authors = projectAuthorService.getAuthorsByProjectIds(projectIds);
                java.util.Set<Long> projectIdsWithAuthors = new java.util.HashSet<>();
                for (LabProjectAuthorEntity a : authors) {
                    map.computeIfAbsent(a.getProjectId(), k -> new java.util.ArrayList<>())
                       .add(convertProjectAuthor(a));
                    if (currentUserId.equals(a.getUserId())) {
                        myVisibilityMap.put(a.getProjectId(), a.getVisible());
                    }
                    projectIdsWithAuthors.add(a.getProjectId());
                }
                List<Long> legacyNeeded = projectIds.stream()
                    .filter(id -> !projectIdsWithAuthors.contains(id) || !map.containsKey(id))
                    .collect(Collectors.toList());
                if (!legacyNeeded.isEmpty()) {
                    List<com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity> legacy = queryLegacyProjectAuthors(legacyNeeded);
                    for (com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity legacyAuthor : legacy) {
                        map.computeIfAbsent(legacyAuthor.getPaperId(), k -> new java.util.ArrayList<>())
                           .add(LabPaperAuthorDTO.fromEntity(legacyAuthor));
                        if (currentUserId.equals(legacyAuthor.getUserId())) {
                            myVisibilityMap.put(legacyAuthor.getPaperId(), legacyAuthor.getVisible());
                        }
                    }
                }
            }

            for (LabAchievementDTO dto : dtoList) {
                dto.setAuthors(map.getOrDefault(dto.getId(), java.util.Collections.emptyList()));
                dto.setMyVisibility(myVisibilityMap.get(dto.getId()));
            }

            if (!paperIds.isEmpty()) {
                java.util.Map<Long, java.util.List<Long>> fundMap = fundPaperRelService.lambdaQuery()
                    .in(LabFundPaperRelEntity::getPaperId, paperIds)
                    .list()
                    .stream()
                    .collect(Collectors.groupingBy(
                        LabFundPaperRelEntity::getPaperId,
                        Collectors.mapping(LabFundPaperRelEntity::getFundId, Collectors.toList())
                    ));
                for (LabAchievementDTO dto : dtoList) {
                    if (Integer.valueOf(1).equals(dto.getType())) {
                        dto.setFundIds(fundMap.getOrDefault(dto.getId(), java.util.Collections.emptyList()));
                    }
                }
            }
        }


        return new PageDTO<>(dtoList, result.getTotal());
    }

    /**
     * 切换我在某成果中的个人页可见性（仅作者本人可操作自己的可见性）
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean toggleMyVisibilityInAchievement(Long achievementId, Boolean visible, Long currentUserId, String type) {
        if (achievementId == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "成果");
        }
        String normalizedType = type == null ? "" : type.trim().toLowerCase();
        Boolean newVisible = Boolean.TRUE.equals(visible);

        if (!"paper".equals(normalizedType) && !"project".equals(normalizedType)) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                "type参数必须为paper或project");
        }

        if ("paper".equals(normalizedType)) {
            LabPaperEntity paper = paperService.getById(achievementId);
            if (paper == null || Boolean.TRUE.equals(paper.getDeleted())) {
                throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, achievementId, "论文成果");
            }
            LabPaperAuthorEntity paperAuthorRecord = authorService.getAuthorRecord(achievementId, currentUserId);
            if (paperAuthorRecord == null) {
                throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "您不是该成果的作者");
            }
            paperAuthorRecord.setVisible(newVisible);
            paperAuthorRecord.setUpdateTime(new java.util.Date());
            authorService.updateById(paperAuthorRecord);
            return paperAuthorRecord.getVisible();
        }

        LabProjectEntity project = projectService.getById(achievementId);
        if (project == null || Boolean.TRUE.equals(project.getDeleted())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, achievementId, "项目成果");
        }
        LabProjectAuthorEntity projectAuthorRecord = projectAuthorService.lambdaQuery()
            .eq(LabProjectAuthorEntity::getProjectId, achievementId)
            .eq(LabProjectAuthorEntity::getUserId, currentUserId)
            .eq(LabProjectAuthorEntity::getDeleted, false)
            .one();
        if (projectAuthorRecord == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "您不是该成果的作者");
        }
        projectAuthorRecord.setVisible(newVisible);
        projectAuthorRecord.setUpdateTime(new java.util.Date());
        projectAuthorService.updateById(projectAuthorRecord);
        return projectAuthorRecord.getVisible();
    }

    private Integer resolveAchievementTypeForVisibility(Long achievementId, Long currentUserId) {
        if (achievementId == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "成果");
        }

        LabPaperAuthorEntity paperAuthor = authorService.getAuthorRecord(achievementId, currentUserId);
        if (paperAuthor != null) {
            LabPaperEntity paper = paperService.getById(achievementId);
            if (paper != null && !Boolean.TRUE.equals(paper.getDeleted())) {
                return 1;
            }
        }

        LabProjectAuthorEntity projectAuthor = projectAuthorService.lambdaQuery()
            .eq(LabProjectAuthorEntity::getProjectId, achievementId)
            .eq(LabProjectAuthorEntity::getUserId, currentUserId)
            .eq(LabProjectAuthorEntity::getDeleted, false)
            .one();
        if (projectAuthor != null) {
            LabProjectEntity project = projectService.getById(achievementId);
            if (project != null && !Boolean.TRUE.equals(project.getDeleted())) {
                return 2;
            }
        }

        LabPaperEntity paper = paperService.getById(achievementId);
        if (paper != null && !Boolean.TRUE.equals(paper.getDeleted())) {
            return 1;
        }

        LabProjectEntity project = projectService.getById(achievementId);
        if (project != null && !Boolean.TRUE.equals(project.getDeleted())) {
            return 2;
        }

        throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, achievementId, "成果");
    }
    /**
     * 获取公开成果列表（所有未删除的成果）
     */
    public PageDTO<com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO> getPublicAchievementList(com.agileboot.domain.lab.achievement.query.PublicAchievementQuery query) {
        LambdaQueryWrapper<LabAchievementEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabAchievementEntity::getDeleted, false);

        // 按作者查询：优先使用精确的用户ID，如果没有提供则使用姓名模糊查询
        if (query.getAuthorUserId() != null) {
            // 精确查询：按用户ID查询
            List<Long> idsByUser = new java.util.ArrayList<>();
            idsByUser.addAll(authorService.lambdaQuery()
                .eq(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getDeleted, false)
                .eq(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getUserId, query.getAuthorUserId())
                .list()
                .stream()
                .map(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getPaperId)
                .collect(Collectors.toList()));
            idsByUser.addAll(projectAuthorService.lambdaQuery()
                .eq(LabProjectAuthorEntity::getDeleted, false)
                .eq(LabProjectAuthorEntity::getUserId, query.getAuthorUserId())
                .list()
                .stream()
                .map(LabProjectAuthorEntity::getProjectId)
                .collect(Collectors.toList()));
            idsByUser = idsByUser.stream().distinct().collect(Collectors.toList());
            if (idsByUser.isEmpty()) {
                return new PageDTO<>(java.util.Collections.emptyList(), 0L);
            }
            wrapper.in(LabAchievementEntity::getId, idsByUser);
        } else if (StringUtils.hasText(query.getAuthorName())) {
            // 按作者姓名查询：只查询作者表，不进行用户表反向查询，避免重名问题
            Set<Long> matchedAchievementIds = new java.util.HashSet<>();

            // 查询作者表中姓名匹配的记录
            com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity> authorQuery =
                authorService.lambdaQuery()
                    .eq(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getDeleted, false)
                    .and(w -> w.like(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getName, query.getAuthorName())
                        .or().like(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getNameEn, query.getAuthorName()));

            // 如果提供了邮箱，则进一步筛选
            if (StringUtils.hasText(query.getAuthorEmail())) {
                authorQuery.eq(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getEmail, query.getAuthorEmail());
            }

            List<com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity> authorMatches = authorQuery.list();

            for (com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity author : authorMatches) {
                if (author.getUserId() != null) {
                    // 内部作者：必须验证邮箱一致性
                    com.agileboot.domain.lab.user.db.LabUserEntity user = labUserService.getById(author.getUserId());
                    if (user != null && !Boolean.TRUE.equals(user.getDeleted())) {
                        // 只有当作者表和用户表的邮箱一致时才匹配
                        if (author.getEmail() != null && user.getEmail() != null &&
                            author.getEmail().equalsIgnoreCase(user.getEmail())) {
                            matchedAchievementIds.add(author.getPaperId());
                        }
                        // 其他情况都跳过，确保数据准确性
                    }
                } else {
                    // 外部作者：只有在提供邮箱参数且匹配时才包含
                    if (StringUtils.hasText(query.getAuthorEmail()) &&
                        author.getEmail() != null &&
                        author.getEmail().equalsIgnoreCase(query.getAuthorEmail())) {
                        matchedAchievementIds.add(author.getPaperId());
                    }
                    // 没有提供邮箱参数时，跳过所有外部作者，避免重名混淆
                }
            }

            // 查询项目作者
            com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<LabProjectAuthorEntity> projectAuthorQuery =
                projectAuthorService.lambdaQuery()
                    .eq(LabProjectAuthorEntity::getDeleted, false)
                    .and(w -> w.like(LabProjectAuthorEntity::getName, query.getAuthorName())
                        .or().like(LabProjectAuthorEntity::getNameEn, query.getAuthorName()));
            if (StringUtils.hasText(query.getAuthorEmail())) {
                projectAuthorQuery.eq(LabProjectAuthorEntity::getEmail, query.getAuthorEmail());
            }
            List<LabProjectAuthorEntity> projectAuthorMatches = projectAuthorQuery.list();
            for (LabProjectAuthorEntity author : projectAuthorMatches) {
                if (author.getUserId() != null) {
                    com.agileboot.domain.lab.user.db.LabUserEntity user = labUserService.getById(author.getUserId());
                    if (user != null && !Boolean.TRUE.equals(user.getDeleted())) {
                        if (author.getEmail() != null && user.getEmail() != null &&
                            author.getEmail().equalsIgnoreCase(user.getEmail())) {
                            matchedAchievementIds.add(author.getProjectId());
                        }
                    }
                } else if (StringUtils.hasText(query.getAuthorEmail()) &&
                    author.getEmail() != null &&
                    author.getEmail().equalsIgnoreCase(query.getAuthorEmail())) {
                    matchedAchievementIds.add(author.getProjectId());
                }
            }

            if (matchedAchievementIds.isEmpty()) {
                // 没有找到匹配的作者，返回空结果
                return new PageDTO<>(java.util.Collections.emptyList(), 0L);
            }
            wrapper.in(LabAchievementEntity::getId, matchedAchievementIds);
        }




        // 应用其他筛选条件
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(LabAchievementEntity::getTitle, query.getKeyword())
                .or().like(LabAchievementEntity::getKeywords, query.getKeyword()));
        }
        // 当传入 categoryId 或 parentCategoryId 时，忽略 type 兼容参数
        if (query.getType() != null && query.getCategoryId() == null && query.getParentCategoryId() == null) {
            wrapper.eq(LabAchievementEntity::getType, query.getType());
        }
        wrapper.eq(query.getPaperType() != null, LabAchievementEntity::getPaperType, query.getPaperType());
        wrapper.eq(query.getProjectType() != null, LabAchievementEntity::getProjectType, query.getProjectType());
        // 新增：按父级分类ID聚合查询（一级分类 -> 所有后代二级分类）
        if (query.getParentCategoryId() != null && query.getCategoryId() == null) {
            java.util.List<Long> descendantIds = categoryService.getDescendantIds(query.getParentCategoryId());
            if (descendantIds == null || descendantIds.isEmpty()) {
                return new PageDTO<>(java.util.Collections.emptyList(), 0L);
            }
            wrapper.in(LabAchievementEntity::getCategoryId, descendantIds);
        }

        // 新增：按分类ID筛选（公开接口）
        wrapper.eq(query.getCategoryId() != null, LabAchievementEntity::getCategoryId, query.getCategoryId());

        // 日期范围过滤
        if (query.getDateStart() != null || query.getDateEnd() != null) {
            wrapper.and(w -> {
                w.and(w1 -> w1.eq(LabAchievementEntity::getType, 1)
                    .ge(query.getDateStart() != null, LabAchievementEntity::getPublishDate, query.getDateStart())
                    .le(query.getDateEnd() != null, LabAchievementEntity::getPublishDate, query.getDateEnd()))
                .or(w2 -> w2.eq(LabAchievementEntity::getType, 2)
                    .ge(query.getDateStart() != null, LabAchievementEntity::getProjectStartDate, query.getDateStart())
                    .le(query.getDateEnd() != null, LabAchievementEntity::getProjectStartDate, query.getDateEnd()));
            });
        }

        // 排序：按创建时间降序
        wrapper.orderByDesc(LabAchievementEntity::getCreateTime);

        IPage<LabAchievementEntity> result = achievementService.page(query.toPage(), wrapper);

        List<com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO> dtoList = result.getRecords().stream()
            .map(com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO::fromEntity)
            .collect(Collectors.toList());

        if (!dtoList.isEmpty()) {
            List<Long> paperIds = result.getRecords().stream()
                .filter(record -> Integer.valueOf(1).equals(record.getType()))
                .map(LabAchievementEntity::getId)
                .collect(Collectors.toList());
            java.util.Map<Long, LabPaperEntity> paperMap = new java.util.HashMap<>();
            if (!paperIds.isEmpty()) {
                paperService.listByIds(paperIds).forEach(paper -> {
                    if (paper != null && paper.getId() != null) {
                        paperMap.put(paper.getId(), paper);
                    }
                });
            }

            List<Long> projectIds = result.getRecords().stream()
                .filter(record -> Integer.valueOf(2).equals(record.getType()))
                .map(LabAchievementEntity::getId)
                .collect(Collectors.toList());
            java.util.Map<Long, LabProjectEntity> projectMap = new java.util.HashMap<>();
            if (!projectIds.isEmpty()) {
                projectService.listByIds(projectIds).forEach(project -> {
                    if (project != null && project.getId() != null) {
                        projectMap.put(project.getId(), project);
                    }
                });
            }

            for (com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO dto : dtoList) {
                mergeChildSnapshotIntoPublicDto(dto, paperMap.get(dto.getId()), projectMap.get(dto.getId()));
            }

            if (!paperIds.isEmpty()) {
                java.util.Map<Long, List<LabFundPaperRelEntity>> fundEntityMap = fundPaperRelService.lambdaQuery()
                    .in(LabFundPaperRelEntity::getPaperId, paperIds)
                    .list()
                    .stream()
                    .collect(Collectors.groupingBy(LabFundPaperRelEntity::getPaperId));
                for (com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO dto : dtoList) {
                    if (Integer.valueOf(1).equals(dto.getType())) {
                        List<LabFundPaperRelEntity> rels = fundEntityMap.get(dto.getId());
                        if (rels == null) {
                            dto.setFundAssociations(java.util.Collections.emptyList());
                        } else {
                            dto.setFundAssociations(rels.stream()
                                .map(rel -> {
                                    LabFundAssociationDTO item = new LabFundAssociationDTO();
                                    item.setFundId(rel.getFundId());
                                    item.setAmount(rel.getAmount());
                                    return item;
                                })
                                .collect(Collectors.toList()));
                        }
                    } else {
                        dto.setFundAssociations(java.util.Collections.emptyList());
                    }
                }
            } else {
                for (com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO dto : dtoList) {
                    if (Integer.valueOf(1).equals(dto.getType())) {
                        dto.setFundAssociations(java.util.Collections.emptyList());
                    } else {
                        dto.setFundAssociations(java.util.Collections.emptyList());
                    }
                }
            }
        }

        // 填充分类名称
        if (!dtoList.isEmpty()) {
            List<Long> categoryIds = dtoList.stream()
                .map(com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO::getCategoryId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

            if (!categoryIds.isEmpty()) {
                List<com.agileboot.domain.lab.category.db.LabAchievementCategoryEntity> categories =
                    categoryService.lambdaQuery()
                        .in(com.agileboot.domain.lab.category.db.LabAchievementCategoryEntity::getId, categoryIds)
                        .eq(com.agileboot.domain.lab.category.db.LabAchievementCategoryEntity::getDeleted, false)
                        .list();

                Map<Long, String> categoryNameMap = categories.stream()
                    .collect(Collectors.toMap(
                        com.agileboot.domain.lab.category.db.LabAchievementCategoryEntity::getId,
                        com.agileboot.domain.lab.category.db.LabAchievementCategoryEntity::getCategoryName
                    ));

                for (com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO dto : dtoList) {
                    if (dto.getCategoryId() != null) {
                        dto.setCategoryName(categoryNameMap.get(dto.getCategoryId()));
                    }
                }
            }
        }

        // 填充作者信息（过滤可见性：外部作者全显示，内部作者仅显示visible=true）
        if (!dtoList.isEmpty()) {
            List<Long> paperIds = dtoList.stream()
                .filter(dto -> Integer.valueOf(1).equals(dto.getType()))
                .map(com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO::getId)
                .collect(Collectors.toList());
            List<Long> projectIds = dtoList.stream()
                .filter(dto -> Integer.valueOf(2).equals(dto.getType()))
                .map(com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO::getId)
                .collect(Collectors.toList());

            Map<Long, List<com.agileboot.domain.lab.paper.dto.PublicPaperAuthorDTO>> authorMap = new java.util.HashMap<>();

            if (!paperIds.isEmpty()) {
                List<com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity> allAuthors =
                    authorService.lambdaQuery()
                        .in(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getPaperId, paperIds)
                        .eq(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getDeleted, false)
                        .orderByAsc(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getAuthorOrder)
                        .list();
                for (com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity a : allAuthors) {
                    authorMap.computeIfAbsent(a.getPaperId(), k -> new java.util.ArrayList<>())
                        .add(com.agileboot.domain.lab.paper.dto.PublicPaperAuthorDTO.fromEntity(a));
                }
            }
            if (!projectIds.isEmpty()) {
                List<LabProjectAuthorEntity> projectAuthors = projectAuthorService.getAuthorsByProjectIds(projectIds);
                java.util.Set<Long> projectIdsWithAuthors = new java.util.HashSet<>();
                for (LabProjectAuthorEntity author : projectAuthors) {
                    authorMap.computeIfAbsent(author.getProjectId(), k -> new java.util.ArrayList<>())
                        .add(convertProjectPublicAuthor(author));
                    projectIdsWithAuthors.add(author.getProjectId());
                }
                List<Long> legacyNeeded = projectIds.stream()
                    .filter(id -> !projectIdsWithAuthors.contains(id) || !authorMap.containsKey(id))
                    .collect(Collectors.toList());
                if (!legacyNeeded.isEmpty()) {
                    List<com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity> legacy = queryLegacyProjectAuthors(legacyNeeded);
                    for (com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity legacyAuthor : legacy) {
                        authorMap.computeIfAbsent(legacyAuthor.getPaperId(), k -> new java.util.ArrayList<>())
                            .add(com.agileboot.domain.lab.paper.dto.PublicPaperAuthorDTO.fromEntity(legacyAuthor));
                    }
                }
            }

            // 为每个成果设置作者列表
            for (com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO dto : dtoList) {
                dto.setAuthors(authorMap.getOrDefault(dto.getId(), java.util.Collections.emptyList()));
            }
        }

        return new PageDTO<>(dtoList, result.getTotal());
    }

    /**
     * 获取公开成果详情（所有未删除的成果）
     */
    public com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO getPublicAchievementDetail(Long id) {
        LabAchievementEntity entity = achievementService.lambdaQuery()
            .eq(LabAchievementEntity::getId, id)
            .eq(LabAchievementEntity::getDeleted, false)
            .one();

        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "成果");
        }

        com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO dto =
            com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO.fromEntity(entity);

        LabPaperEntity paper = null;
        LabProjectEntity project = null;
        if (Integer.valueOf(1).equals(entity.getType())) {
            paper = paperService.getById(entity.getId());
        } else if (Integer.valueOf(2).equals(entity.getType())) {
            project = projectService.getById(entity.getId());
        }
        mergeChildSnapshotIntoPublicDto(dto, paper, project);

        // 填充分类名称
        if (entity.getCategoryId() != null) {
            com.agileboot.domain.lab.category.db.LabAchievementCategoryEntity category =
                categoryService.getById(entity.getCategoryId());
            if (category != null) {
                dto.setCategoryName(category.getCategoryName());
            }
        }

        // 获取作者列表（显示所有作者，不过滤可见性）
        if (Integer.valueOf(1).equals(entity.getType())) {
            List<com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity> authors =
                authorService.lambdaQuery()
                    .eq(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getPaperId, id)
                    .eq(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getDeleted, false)
                    .orderByAsc(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getAuthorOrder)
                    .list();
            dto.setAuthors(authors.stream()
                .map(com.agileboot.domain.lab.paper.dto.PublicPaperAuthorDTO::fromEntity)
                .collect(Collectors.toList()));
        } else if (Integer.valueOf(2).equals(entity.getType())) {
            List<LabProjectAuthorEntity> authors = projectAuthorService.getAuthorsByProjectId(id);
            List<com.agileboot.domain.lab.paper.dto.PublicPaperAuthorDTO> projectAuthorDtos = authors.stream()
                .map(this::convertProjectPublicAuthor)
                .collect(Collectors.toList());
            if (projectAuthorDtos.isEmpty()) {
                List<com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity> legacy =
                    queryLegacyProjectAuthors(java.util.Collections.singletonList(id));
                projectAuthorDtos = legacy.stream()
                    .map(com.agileboot.domain.lab.paper.dto.PublicPaperAuthorDTO::fromEntity)
                    .collect(Collectors.toList());
            }
            dto.setAuthors(projectAuthorDtos);
        } else {
            dto.setAuthors(java.util.Collections.emptyList());
        }
        if (Integer.valueOf(1).equals(entity.getType())) {
            List<LabFundPaperRelEntity> rels = fundPaperRelService.lambdaQuery()
                .eq(LabFundPaperRelEntity::getPaperId, entity.getId())
                .list();
            if (rels == null || rels.isEmpty()) {
                dto.setFundAssociations(java.util.Collections.emptyList());
            } else {
                dto.setFundAssociations(rels.stream()
                    .map(rel -> {
                        LabFundAssociationDTO item = new LabFundAssociationDTO();
                        item.setFundId(rel.getFundId());
                        item.setAmount(rel.getAmount());
                        return item;
                    })
                    .collect(Collectors.toList()));
            }
        } else {
            dto.setFundAssociations(java.util.Collections.emptyList());
        }
        return dto;
    }

    /**
     * 为成果批量创建作者（私有方法，事务内调用）
     */
    private void createAuthorsForAchievement(Long achievementId, List<com.agileboot.domain.lab.achievement.command.CreateAuthorCommand> authors, boolean upsertInternal) {
        // 校验作者顺序唯一性
        Set<Integer> orderSet = new HashSet<>();
        for (com.agileboot.domain.lab.achievement.command.CreateAuthorCommand authorCmd : authors) {
            if (!orderSet.add(authorCmd.getAuthorOrder())) {
                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                    "作者顺序重复：" + authorCmd.getAuthorOrder());
            }
            // 校验外部作者必须有姓名
            if (authorCmd.getUserId() == null && (authorCmd.getName() == null || authorCmd.getName().trim().isEmpty())) {
                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                    "外部作者必须提供姓名");
            }
        }

        // 批量插入作者记录（自动绑定内部作者：userId > username/email/phone/studentNumber > 唯一姓名）
        java.util.Set<Long> internalUsersInThisList = new java.util.HashSet<>();
        List<com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity> authorEntities =
            authors.stream().map(authorCmd -> {
                // 自动绑定 userId（如果未提供）
                if (authorCmd.getUserId() == null) {
                    com.agileboot.domain.lab.user.db.LabUserEntity matched = null;
                    String uname = authorCmd.getUsername() == null ? null : authorCmd.getUsername().trim();
                    String mail = authorCmd.getEmail() == null ? null : authorCmd.getEmail().trim();

                    // 同时提供 username 与 email 时，要求二者一致才能绑定
                    if (uname != null && !uname.isEmpty() && mail != null && !mail.isEmpty()) {
                        com.agileboot.domain.lab.user.db.LabUserEntity byUsername = labUserService.getByUsername(uname);
                        if (byUsername != null && mail.equalsIgnoreCase(byUsername.getEmail())) {
                            matched = byUsername;
                        } else {
                            com.agileboot.domain.lab.user.db.LabUserEntity byEmail = labUserService.getByEmail(mail);
                            if (byEmail != null && uname.equalsIgnoreCase(byEmail.getUsername())) {
                                matched = byEmail;
                            } else {
                                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "用户名与邮箱不一致，无法唯一绑定内部作者");
                            }
                        }
                    }

                    // 仅提供其一时，按优先级逐项匹配
                    if (matched == null && uname != null && !uname.isEmpty()) {
                        matched = labUserService.getByUsername(uname);
                    }
                    if (matched == null && mail != null && !mail.isEmpty()) {
                        matched = labUserService.getByEmail(mail);
                    }
                    if (matched == null && authorCmd.getPhone() != null && !authorCmd.getPhone().trim().isEmpty()) {
                        matched = labUserService.getByPhone(authorCmd.getPhone().trim());
                    }
                    if (matched == null && authorCmd.getStudentNumber() != null && !authorCmd.getStudentNumber().trim().isEmpty()) {
                        matched = labUserService.getByStudentNumber(authorCmd.getStudentNumber().trim());
                    }
                    if (matched == null && authorCmd.getName() != null) {
                        matched = labUserService.getUniqueByRealName(authorCmd.getName());
                        if (matched == null) {
                            // 检查是否存在重名用户
                            List<com.agileboot.domain.lab.user.db.LabUserEntity> duplicateUsers = labUserService.lambdaQuery()
                                .eq(com.agileboot.domain.lab.user.db.LabUserEntity::getRealName, authorCmd.getName())
                                .eq(com.agileboot.domain.lab.user.db.LabUserEntity::getDeleted, false)
                                .list();
                            if (duplicateUsers.size() > 1) {
                                // 存在重名用户，需要提供邮箱来区分
                                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                                    "存在多个名为 \"" + authorCmd.getName() + "\" 的用户，请提供邮箱以确定具体是哪一个用户");
                            }
                        }
                    }
                    if (matched == null && authorCmd.getNameEn() != null) {
                        matched = labUserService.getUniqueByEnglishName(authorCmd.getNameEn());
                    }
                    if (matched != null) {
                        if (internalUsersInThisList.contains(matched.getId())) {
                            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "同一内部作者不能重复：" + authorCmd.getName() + " 自动绑定到 userId=" + matched.getId());
                        }

                        // 验证填写的邮箱是否与匹配用户的实际邮箱一致
                        if (mail != null && !mail.isEmpty() && matched.getEmail() != null) {
                            if (!mail.equalsIgnoreCase(matched.getEmail())) {
                                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                                    "作者 " + authorCmd.getName() + " 填写的邮箱与用户 " + matched.getRealName() + " 的实际邮箱不匹配，请确认后重新填写");
                            }
                        }

                        authorCmd.setUserId(matched.getId());
                        // 如果自动绑定成功，使用用户的真实邮箱而不是填写的邮箱
                        if (matched.getEmail() != null) {
                            authorCmd.setEmail(matched.getEmail());
                        }
                        internalUsersInThisList.add(matched.getId());
                    }
                } else {
                    if (internalUsersInThisList.contains(authorCmd.getUserId())) {
                        throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "同一内部作者不能重复：userId=" + authorCmd.getUserId());
                    }
                    internalUsersInThisList.add(authorCmd.getUserId());
                }

                com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity entity =
                    new com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity();
                entity.setPaperId(achievementId);
                entity.setUserId(authorCmd.getUserId());
                entity.setName(authorCmd.getName());
                entity.setNameEn(authorCmd.getNameEn());
                entity.setEmail(authorCmd.getEmail());
                entity.setAffiliation(authorCmd.getAffiliation());
                entity.setAuthorOrder(authorCmd.getAuthorOrder());
                entity.setIsCorresponding(Boolean.TRUE.equals(authorCmd.getIsCorresponding()));
                entity.setRole(authorCmd.getRole());
                entity.setVisible(Boolean.TRUE.equals(authorCmd.getVisible()));
                entity.setDeleted(false);
                entity.setCreateTime(new java.util.Date());
                entity.setUpdateTime(new java.util.Date());
                return entity;
            }).collect(Collectors.toList());

        // 统一使用 upsert 逻辑，避免唯一键冲突
        for (com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity e : authorEntities) {
            if (e.getUserId() != null) {
                // 内部作者：先尝试删除可能存在的记录，然后插入新记录
                try {
                    System.out.println("DEBUG: 准备处理内部作者 " + e.getName() + " (userId=" + e.getUserId() + ")");

                    // 物理删除所有该 (achievement_id, user_id) 的记录（包括软删的）
                    int deletedCount = authorService.hardDeleteByPaperIdAndUserId(achievementId, e.getUserId());
                    System.out.println("DEBUG: 删除了 " + deletedCount + " 条记录");

                    // 插入新记录
                    boolean saveResult = authorService.save(e);
                    System.out.println("DEBUG: 插入结果 " + saveResult);

                } catch (Exception ex) {
                    // 如果仍然失败，记录详细错误信息
                    System.err.println("Failed to save author: achievementId=" + achievementId + ", userId=" + e.getUserId() + ", error=" + ex.getMessage());
                    ex.printStackTrace();
                    throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                        "保存内部作者失败: " + e.getName() + " (userId=" + e.getUserId() + ") - " + ex.getMessage());
                }
            } else {
                // 外部作者：直接插入
                authorService.save(e);
            }
        }
    }

    private LabPaperAuthorDTO convertProjectAuthor(LabProjectAuthorEntity entity) {
        LabPaperAuthorDTO dto = new LabPaperAuthorDTO();
        dto.setId(entity.getId());
        dto.setPaperId(entity.getProjectId());
        dto.setUserId(entity.getUserId());
        dto.setName(entity.getName());
        dto.setNameEn(entity.getNameEn());
        dto.setEmail(entity.getEmail());
        dto.setAffiliation(entity.getAffiliation());
        dto.setAuthorOrder(entity.getAuthorOrder());
        dto.setIsCorresponding(Boolean.TRUE.equals(entity.getIsCorresponding()));
        dto.setRole(entity.getRole());
        dto.setVisible(Boolean.TRUE.equals(entity.getVisible()));
        dto.setIsInternal(entity.getUserId() != null);
        return dto;
    }

    private List<com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity> queryLegacyProjectAuthors(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return authorService.lambdaQuery()
            .in(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getPaperId, projectIds)
            .eq(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getDeleted, false)
            .orderByAsc(com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity::getAuthorOrder)
            .list();
    }

    private PublicPaperAuthorDTO convertProjectPublicAuthor(LabProjectAuthorEntity entity) {
        PublicPaperAuthorDTO dto = new PublicPaperAuthorDTO();
        dto.setName(entity.getName());
        dto.setNameEn(entity.getNameEn());
        dto.setAffiliation(entity.getAffiliation());
        dto.setCorresponding(Boolean.TRUE.equals(entity.getIsCorresponding()));
        dto.setOrder(entity.getAuthorOrder());
        return dto;
    }

    private boolean isAchievementAuthor(Long achievementId, Long userId) {
        if (userId == null) {
            return false;
        }
        if (authorService.isAuthor(achievementId, userId)) {
            return true;
        }
        return projectAuthorService.lambdaQuery()
            .eq(LabProjectAuthorEntity::getProjectId, achievementId)
            .eq(LabProjectAuthorEntity::getUserId, userId)
            .eq(LabProjectAuthorEntity::getDeleted, false)
            .count() > 0;
    }

}
