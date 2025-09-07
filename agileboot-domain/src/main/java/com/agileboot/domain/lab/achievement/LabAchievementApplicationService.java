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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
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

        // 类型过滤
        wrapper.eq(query.getType() != null, LabAchievementEntity::getType, query.getType());
        wrapper.eq(query.getPaperType() != null, LabAchievementEntity::getPaperType, query.getPaperType());
        wrapper.eq(query.getProjectType() != null, LabAchievementEntity::getProjectType, query.getProjectType());

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
        entity.setVenue(command.getVenue());
        entity.setPublishDate(parseYearToLocalDate(command.getPublishDate()));
        entity.setProjectStartDate(parseYearMonthToLocalDate(command.getProjectStartDate()));
        entity.setProjectEndDate(parseYearMonthToLocalDate(command.getProjectEndDate()));
        entity.setCoverUrl(command.getCoverUrl());
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
            entity.setPublishDate(parseYearToLocalDate(command.getPublishDate()));
            entity.setProjectStartDate(parseYearMonthToLocalDate(command.getProjectStartDate()));
            entity.setProjectEndDate(parseYearMonthToLocalDate(command.getProjectEndDate()));
            entity.setDoi(command.getDoi());
        }

        entity.setVenue(command.getVenue());
        entity.setCoverUrl(command.getCoverUrl());
        entity.setLinkUrl(command.getLinkUrl());
        entity.setGitUrl(command.getGitUrl());
        entity.setHomepageUrl(command.getHomepageUrl());
        entity.setPdfUrl(command.getPdfUrl());
        entity.setFundingAmount(command.getFundingAmount());
        entity.setExtra(command.getExtra());
        entity.setUpdateTime(new java.util.Date());

        // 先更新成果主体
        achievementService.updateById(entity);

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

        // 权限检查：管理员 或 拥有者 或 第一作者(内部且author_order=1) 可删除
        if (!isAdmin) {
            boolean isOwner = currentUserId.equals(entity.getOwnerUserId());
            boolean isFirstAuthor = false;
            com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity rec = authorService.getAuthorRecord(id, currentUserId);
            if (rec != null && Boolean.FALSE.equals(rec.getDeleted()) && Integer.valueOf(1).equals(rec.getAuthorOrder())) {
                isFirstAuthor = true;
            }
            if (!(isOwner || isFirstAuthor)) {
                throw new ApiException(ErrorCode.Business.PERMISSION_NOT_ALLOWED_TO_OPERATE);
            }
        }

        // 软删除（显式更新deleted与更新时间）
        boolean ok = achievementService.lambdaUpdate()
            .eq(LabAchievementEntity::getId, id)
            .set(LabAchievementEntity::getDeleted, true)
            .set(LabAchievementEntity::getUpdateTime, new java.util.Date())
            .update();

        // 作者清理：先硬删历史软删作者，再软删当前有效作者，保持与更新流程一致
        authorService.hardDeleteDeletedByAchievementId(id);
        authorService.lambdaUpdate()
            .eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getAchievementId, id)
            .eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getDeleted, false)
            .set(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getDeleted, true)
            .update();

        if (!ok) {
            throw new ApiException(ErrorCode.Business.COMMON_UNSUPPORTED_OPERATION, "成果删除失败");
        }
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
     * 校验成果命令
     */
    private void validateAchievementCommand(CreateLabAchievementCommand command) {
        if (command.getType() == 1) {
            // 论文校验
            if (command.getPaperType() == null) {
                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "论文类型不能为空");
            }
            if (command.getPublishDate() == null) {
                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "论文发表日期不能为空");
            }
            if (command.getProjectType() != null || command.getProjectStartDate() != null || command.getProjectEndDate() != null) {
                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "论文不能设置项目相关字段");
            }
        } else if (command.getType() == 2) {
            // 项目校验
            if (command.getProjectType() == null) {
                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "项目类型不能为空");
            }
            if (command.getProjectStartDate() == null) {
                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "项目开始日期不能为空");
            }
            if (StringUtils.hasText(command.getProjectEndDate())) {
                java.time.LocalDate start = parseYearMonthToLocalDate(command.getProjectStartDate());
                java.time.LocalDate end = parseYearMonthToLocalDate(command.getProjectEndDate());
                if (end != null && start != null && end.isBefore(start)) {
                    throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "项目结束日期不能早于开始日期");
                }
            }
            if (command.getPaperType() != null || command.getPublishDate() != null) {
                throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "项目不能设置论文相关字段");
            }
        }
    }

    /**
     * 获取我的成果列表（我拥有的 + 我参与的）
     */
    public PageDTO<LabAchievementDTO> getMyAchievementList(com.agileboot.domain.lab.achievement.query.MyAchievementQuery query, Long currentUserId) {
        // 查询我参与的成果ID（作为作者）
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
        wrapper.eq(query.getType() != null, LabAchievementEntity::getType, query.getType());
        wrapper.eq(query.getPaperType() != null, LabAchievementEntity::getPaperType, query.getPaperType());
        wrapper.eq(query.getProjectType() != null, LabAchievementEntity::getProjectType, query.getProjectType());
        wrapper.eq(query.getPublished() != null, LabAchievementEntity::getPublished, query.getPublished());
        wrapper.eq(query.getIsVerified() != null, LabAchievementEntity::getIsVerified, query.getIsVerified());

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
     * 切换我在某成果中的个人页可见性（仅作者本人可操作自己的可见性）
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleMyVisibilityInAchievement(Long achievementId, Boolean visible, Long currentUserId) {
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

        // 更新可见性
        authorRecord.setVisible(Boolean.TRUE.equals(visible));
        authorRecord.setUpdateTime(new java.util.Date());
        authorService.updateById(authorRecord);
    }
    /**
     * 获取公开成果列表（仅已发布且已审核）
     */
    public PageDTO<com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO> getPublicAchievementList(com.agileboot.domain.lab.achievement.query.PublicAchievementQuery query) {
        LambdaQueryWrapper<LabAchievementEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabAchievementEntity::getDeleted, false)
               .eq(LabAchievementEntity::getPublished, true)
               .eq(LabAchievementEntity::getIsVerified, true);

        // 应用筛选条件
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(LabAchievementEntity::getTitle, query.getKeyword())
                .or().like(LabAchievementEntity::getKeywords, query.getKeyword()));
        }
        wrapper.eq(query.getType() != null, LabAchievementEntity::getType, query.getType());
        wrapper.eq(query.getPaperType() != null, LabAchievementEntity::getPaperType, query.getPaperType());
        wrapper.eq(query.getProjectType() != null, LabAchievementEntity::getProjectType, query.getProjectType());

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

        return new PageDTO<>(dtoList, result.getTotal());
    }

    /**
     * 获取公开成果详情（仅已发布且已审核）
     */
    public com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO getPublicAchievementDetail(Long id) {
        LabAchievementEntity entity = achievementService.lambdaQuery()
            .eq(LabAchievementEntity::getId, id)
            .eq(LabAchievementEntity::getDeleted, false)
            .eq(LabAchievementEntity::getPublished, true)
            .eq(LabAchievementEntity::getIsVerified, true)
            .one();

        if (entity == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "成果");
        }

        com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO dto =
            com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO.fromEntity(entity);

        // 获取作者列表（过滤可见性：外部作者全显示，内部作者仅显示visible=true）
        List<com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity> authors =
            authorService.lambdaQuery()
                .eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getAchievementId, id)
                .eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getDeleted, false)
                .and(w -> w.isNull(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getUserId)
                          .or().eq(com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity::getVisible, true))
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
                    if (authorCmd.getUsername() != null && !authorCmd.getUsername().trim().isEmpty()) {
                        matched = labUserService.getByUsername(authorCmd.getUsername().trim());
                    }
                    if (matched == null && authorCmd.getEmail() != null && !authorCmd.getEmail().trim().isEmpty()) {
                        matched = labUserService.getByEmail(authorCmd.getEmail().trim());
                    }
                    if (matched == null && authorCmd.getPhone() != null && !authorCmd.getPhone().trim().isEmpty()) {
                        matched = labUserService.getByPhone(authorCmd.getPhone().trim());
                    }
                    if (matched == null && authorCmd.getStudentNumber() != null && !authorCmd.getStudentNumber().trim().isEmpty()) {
                        matched = labUserService.getByStudentNumber(authorCmd.getStudentNumber().trim());
                    }
                    if (matched == null && authorCmd.getName() != null) {
                        matched = labUserService.getUniqueByRealName(authorCmd.getName());
                        if (matched != null) {
                            System.out.println("DEBUG: 自动绑定 " + authorCmd.getName() + " -> userId=" + matched.getId());
                        }
                    }
                    if (matched == null && authorCmd.getNameEn() != null) {
                        matched = labUserService.getUniqueByEnglishName(authorCmd.getNameEn());
                        if (matched != null) {
                            System.out.println("DEBUG: 自动绑定英文名 " + authorCmd.getNameEn() + " -> userId=" + matched.getId());
                        }
                    }
                    if (matched != null) {
                        if (internalUsersInThisList.contains(matched.getId())) {
                            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "同一内部作者不能重复：" + authorCmd.getName() + " 自动绑定到 userId=" + matched.getId());
                        }
                        authorCmd.setUserId(matched.getId());
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
