package com.agileboot.domain.lab.achievement;

import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.lab.achievement.command.CreateLabAchievementCommand;
import com.agileboot.domain.lab.achievement.db.LabAchievementEntity;
import com.agileboot.domain.lab.achievement.db.LabAchievementService;
import com.agileboot.domain.lab.achievement.db.LabAchievementAuthorMapper;
import com.agileboot.domain.lab.achievement.dto.LabAchievementDTO;
import com.agileboot.domain.lab.achievement.dto.LabAchievementAuthorDTO;
import com.agileboot.domain.lab.achievement.query.LabAchievementQuery;
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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成果管理应用服务
 */
@Service
@RequiredArgsConstructor
public class LabAchievementApplicationService {

    private final LabAchievementService achievementService;
    private static java.time.LocalDate parseYearToLocalDate(String yearStr) {
        if (yearStr == null || yearStr.trim().isEmpty()) return null;
        String y = yearStr.trim();
        if (!y.matches("\\d{4}")) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "发表年份格式应为yyyy");
        }
        return java.time.LocalDate.of(Integer.parseInt(y), 1, 1);
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

    private final com.agileboot.domain.lab.achievement.db.LabAchievementAuthorService authorService;
    private final LabUserService labUserService;
    private final CategoryCompatibilityService categoryCompatibilityService;
    private final com.agileboot.domain.lab.category.db.LabAchievementCategoryService categoryService;

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
            List<Long> byAuthorIds = authorService.lambdaQuery()
                .eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getUserId, query.getAuthorUserId())
                .eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getDeleted, false)
                .list()
                .stream()
                .map(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getAchievementId)
                .collect(Collectors.toList());
            if (byAuthorIds.isEmpty()) {
                return new PageDTO<LabAchievementDTO>(java.util.Collections.emptyList(), 0L);
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
            List<Long> ids = records.stream().map(LabAchievementEntity::getId).collect(Collectors.toList());
            List<com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity> authors = authorService.getAuthorsByAchievementIds(ids);
            java.util.Map<Long, List<LabAchievementAuthorDTO>> map = new java.util.HashMap<>();
            for (com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity a : authors) {
                map.computeIfAbsent(a.getAchievementId(), k -> new java.util.ArrayList<>())
                   .add(LabAchievementAuthorDTO.fromEntity(a));
            }
            for (LabAchievementDTO dto : dtoList) {
                dto.setAuthors(map.getOrDefault(dto.getId(), java.util.Collections.emptyList()));
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

        // 设置所有者姓名
        if (entity.getOwnerUserId() != null) {
            com.agileboot.domain.lab.user.db.LabUserEntity owner = labUserService.getById(entity.getOwnerUserId());
            if (owner != null) {
                dto.setOwnerUserName(owner.getRealName());
            }
        }

        // 填充作者（管理端：全部作者，不过滤）
        List<com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity> authors =
            authorService.getAuthorsByAchievementId(entity.getId());
        dto.setAuthors(authors.stream()
            .map(com.agileboot.domain.lab.achievement.dto.LabAchievementAuthorDTO::fromEntity)
            .collect(java.util.stream.Collectors.toList()));

        return dto;
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

        achievementService.update(updateWrapper);

        // 若提交了作者列表，则替换（全量覆盖）：先软删旧作者，再按新列表创建
        if (command.getAuthors() != null) {
            System.out.println("DEBUG: 开始处理作者列表，成果ID=" + id + "，作者数量=" + command.getAuthors().size());

            // 先清理历史上已被软删的作者（物理删除），避免唯一索引(achievement_id, author_order, deleted)冲突
            int hardDeletedCount = authorService.hardDeleteDeletedByAchievementId(id);
            System.out.println("DEBUG: 物理删除了 " + hardDeletedCount + " 条历史软删作者");

            // 再将当前未删除的作者标记为删除（精准条件）
            com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity> uw =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
            uw.eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getAchievementId, id)
              .eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getDeleted, false)
              .set(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getDeleted, true);
            boolean ok = authorService.update(uw);
            System.out.println("DEBUG: 软删除当前作者结果=" + ok);

            // 插入新作者
            System.out.println("DEBUG: 开始插入新作者列表");
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

        // 成果删除成功后，级联删除所有相关的作者关联记录
        // 因为成果已经不存在，这些关联记录就没有意义了，应该彻底清理
        authorService.hardDeleteAllByAchievementId(id);
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
            boolean isParticipant = authorService.isAuthor(id, currentUserId);
            boolean isOwner = achievementService.isOwner(id, currentUserId);
            if (!(isAdmin || (isTeacher && (isOwner || isParticipant)))) {
                throw new ApiException(ErrorCode.Business.PERMISSION_NOT_ALLOWED_TO_OPERATE);
            }
        }

        entity.setPublished(published);
        entity.setUpdateTime(new java.util.Date());
        achievementService.updateById(entity);
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

        // 设置旧字段默认值（兼容数据库NOT NULL约束）
        command.setType(3);  // 3表示其他成果类型
        command.setPaperType(null);
        command.setProjectType(null);
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
        List<Long> authorAchievementIds = authorService.lambdaQuery()
            .eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getUserId, currentUserId)
            .eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getDeleted, false)
            .list()
            .stream()
            .map(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getAchievementId)
            .collect(Collectors.toList());

        // 合并条件：我拥有的 OR 我参与的
        LambdaQueryWrapper<LabAchievementEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabAchievementEntity::getDeleted, false)
               .and(w -> {
                   w.eq(LabAchievementEntity::getOwnerUserId, currentUserId);
                   if (!authorAchievementIds.isEmpty()) {
                       w.or().in(LabAchievementEntity::getId, authorAchievementIds);
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
            java.util.List<com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity> nameMatched = authorService.lambdaQuery()
                .eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getDeleted, false)
                .and(w -> w.like(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getName, query.getAuthorName())
                         .or().like(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getNameEn, query.getAuthorName()))
                .list();
            nameMatched.forEach(a -> byAuthor.add(a.getAchievementId()));
            // 内部作者按 lab_user 姓名模糊 -> 反查成就ID
            java.util.List<Long> userIds = labUserService.lambdaQuery()
                .eq(com.agileboot.domain.lab.user.db.LabUserEntity::getDeleted, false)
                .and(w -> w.like(com.agileboot.domain.lab.user.db.LabUserEntity::getRealName, query.getAuthorName())
                         .or().like(com.agileboot.domain.lab.user.db.LabUserEntity::getEnglishName, query.getAuthorName()))
                .list().stream().map(com.agileboot.domain.lab.user.db.LabUserEntity::getId)
                .collect(java.util.stream.Collectors.toList());
            if (!userIds.isEmpty()) {
                java.util.List<Long> byUser = authorService.lambdaQuery()
                    .eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getDeleted, false)
                    .in(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getUserId, userIds)
                    .list().stream().map(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getAchievementId)
                    .collect(java.util.stream.Collectors.toList());
                byAuthor.addAll(byUser);
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
            java.util.List<Long> ids = result.getRecords().stream().map(LabAchievementEntity::getId).collect(java.util.stream.Collectors.toList());
            java.util.List<com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity> authors = authorService.getAuthorsByAchievementIds(ids);
            java.util.Map<Long, java.util.List<LabAchievementAuthorDTO>> map = new java.util.HashMap<>();
            java.util.Map<Long, Boolean> myVisibilityMap = new java.util.HashMap<>();

            for (com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity a : authors) {
                map.computeIfAbsent(a.getAchievementId(), k -> new java.util.ArrayList<>())
                   .add(LabAchievementAuthorDTO.fromEntity(a));

                // 记录当前用户在每个成果中的可见性状态
                if (currentUserId.equals(a.getUserId())) {
                    myVisibilityMap.put(a.getAchievementId(), a.getVisible());
                }
            }

            for (LabAchievementDTO dto : dtoList) {
                dto.setAuthors(map.getOrDefault(dto.getId(), java.util.Collections.emptyList()));
                // 设置当前用户在该成果中的可见性状态
                dto.setMyVisibility(myVisibilityMap.get(dto.getId()));
            }
        }


        return new PageDTO<>(dtoList, result.getTotal());
    }

    /**
     * 切换我在某成果中的个人页可见性（仅作者本人可操作自己的可见性）
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean toggleMyVisibilityInAchievement(Long achievementId, Boolean visible, Long currentUserId) {
        System.out.println("DEBUG: 切换可见性 - achievementId=" + achievementId + ", visible=" + visible + ", userId=" + currentUserId);

        // 检查成果是否存在
        LabAchievementEntity achievement = achievementService.getByIdNotDeleted(achievementId);
        if (achievement == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "成果");
        }

        // 查找我在该成果中的作者记录
        com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity authorRecord =
            authorService.getAuthorRecord(achievementId, currentUserId);
        if (authorRecord == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "您不是该成果的作者");
        }

        System.out.println("DEBUG: 找到作者记录 - authorId=" + authorRecord.getId() + ", 当前visible=" + authorRecord.getVisible());

        // 更新可见性
        authorRecord.setVisible(Boolean.TRUE.equals(visible));
        authorRecord.setUpdateTime(new java.util.Date());
        boolean updateResult = authorService.updateById(authorRecord);

        System.out.println("DEBUG: 更新结果=" + updateResult + ", 新visible=" + authorRecord.getVisible());

        return authorRecord.getVisible();
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
            List<Long> idsByUser = authorService.lambdaQuery()
                .eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getDeleted, false)
                .eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getUserId, query.getAuthorUserId())
                .list()
                .stream()
                .map(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getAchievementId)
                .collect(Collectors.toList());
            if (idsByUser.isEmpty()) {
                return new PageDTO<>(java.util.Collections.emptyList(), 0L);
            }
            wrapper.in(LabAchievementEntity::getId, idsByUser);
        } else if (StringUtils.hasText(query.getAuthorName())) {
            // 按作者姓名查询：只查询作者表，不进行用户表反向查询，避免重名问题
            Set<Long> matchedAchievementIds = new java.util.HashSet<>();

            // 查询作者表中姓名匹配的记录
            com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity> authorQuery =
                authorService.lambdaQuery()
                    .eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getDeleted, false)
                    .and(w -> w.like(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getName, query.getAuthorName())
                        .or().like(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getNameEn, query.getAuthorName()));

            // 如果提供了邮箱，则进一步筛选
            if (StringUtils.hasText(query.getAuthorEmail())) {
                authorQuery.eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getEmail, query.getAuthorEmail());
            }

            List<com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity> authorMatches = authorQuery.list();

            for (com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity author : authorMatches) {
                if (author.getUserId() != null) {
                    // 内部作者：必须验证邮箱一致性
                    com.agileboot.domain.lab.user.db.LabUserEntity user = labUserService.getById(author.getUserId());
                    if (user != null && !Boolean.TRUE.equals(user.getDeleted())) {
                        // 只有当作者表和用户表的邮箱一致时才匹配
                        if (author.getEmail() != null && user.getEmail() != null &&
                            author.getEmail().equalsIgnoreCase(user.getEmail())) {
                            matchedAchievementIds.add(author.getAchievementId());
                        }
                        // 其他情况都跳过，确保数据准确性
                    }
                } else {
                    // 外部作者：只有在提供邮箱参数且匹配时才包含
                    if (StringUtils.hasText(query.getAuthorEmail()) &&
                        author.getEmail() != null &&
                        author.getEmail().equalsIgnoreCase(query.getAuthorEmail())) {
                        matchedAchievementIds.add(author.getAchievementId());
                    }
                    // 没有提供邮箱参数时，跳过所有外部作者，避免重名混淆
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
            List<Long> achievementIds = dtoList.stream()
                .map(com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO::getId)
                .collect(Collectors.toList());

            List<com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity> allAuthors =
                authorService.lambdaQuery()
                    .in(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getAchievementId, achievementIds)
                    .eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getDeleted, false)
                    .orderByAsc(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getAuthorOrder)
                    .list();

            // 按成果ID分组
            Map<Long, List<com.agileboot.domain.lab.achievement.dto.PublicAuthorDTO>> authorMap = allAuthors.stream()
                .collect(Collectors.groupingBy(
                    com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getAchievementId,
                    Collectors.mapping(
                        com.agileboot.domain.lab.achievement.dto.PublicAuthorDTO::fromEntity,
                        Collectors.toList()
                    )
                ));

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

        // 填充分类名称
        if (entity.getCategoryId() != null) {
            com.agileboot.domain.lab.category.db.LabAchievementCategoryEntity category =
                categoryService.getById(entity.getCategoryId());
            if (category != null) {
                dto.setCategoryName(category.getCategoryName());
            }
        }

        // 获取作者列表（显示所有作者，不过滤可见性）
        List<com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity> authors =
            authorService.lambdaQuery()
                .eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getAchievementId, id)
                .eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getDeleted, false)
                .orderByAsc(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getAuthorOrder)
                .list();

        List<com.agileboot.domain.lab.achievement.dto.PublicAuthorDTO> authorDTOs = authors.stream()
            .map(com.agileboot.domain.lab.achievement.dto.PublicAuthorDTO::fromEntity)
            .collect(Collectors.toList());

        dto.setAuthors(authorDTOs);
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
        List<com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity> authorEntities =
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

                com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity entity =
                    new com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity();
                entity.setAchievementId(achievementId);
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
        for (com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity e : authorEntities) {
            if (e.getUserId() != null) {
                // 内部作者：先尝试删除可能存在的记录，然后插入新记录
                try {
                    System.out.println("DEBUG: 准备处理内部作者 " + e.getName() + " (userId=" + e.getUserId() + ")");

                    // 物理删除所有该 (achievement_id, user_id) 的记录（包括软删的）
                    int deletedCount = authorService.hardDeleteByAchievementIdAndUserId(achievementId, e.getUserId());
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
}
