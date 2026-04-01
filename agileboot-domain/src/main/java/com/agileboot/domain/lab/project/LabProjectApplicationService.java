package com.agileboot.domain.lab.project;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.lab.achievement.db.LabAchievementEntity;
import com.agileboot.domain.lab.category.CategoryCompatibilityService;
import com.agileboot.domain.lab.category.db.LabAchievementCategoryEntity;
import com.agileboot.domain.lab.category.db.LabAchievementCategoryService;
import com.agileboot.domain.lab.common.dto.RelatedPaperDTO;
import com.agileboot.domain.lab.paper.db.LabPaperEntity;
import com.agileboot.domain.lab.paper.db.LabPaperService;
import com.agileboot.domain.lab.paper.dto.LabPaperAuthorDTO;
import com.agileboot.domain.lab.project.command.CreateProjectCommand;
import com.agileboot.domain.lab.project.command.UpdateProjectCommand;
import com.agileboot.domain.lab.project.db.LabProjectEntity;
import com.agileboot.domain.lab.project.db.LabProjectService;
import com.agileboot.domain.lab.project.dto.LabProjectDTO;
import com.agileboot.domain.lab.project.query.LabProjectQuery;
import com.agileboot.domain.lab.project.author.LabProjectAuthorEntity;
import com.agileboot.domain.lab.project.author.LabProjectAuthorService;
import com.agileboot.domain.lab.project.relation.LabProjectPaperRelEntity;
import com.agileboot.domain.lab.project.relation.LabProjectPaperRelService;
import com.agileboot.domain.lab.user.db.LabUserEntity;
import com.agileboot.domain.lab.user.db.LabUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabProjectApplicationService {

    private final LabProjectService projectService;
    private final LabProjectAuthorService projectAuthorService;
    private final LabUserService labUserService;
    private final CategoryCompatibilityService categoryCompatibilityService;
    private final LabAchievementCategoryService categoryService;
    private final LabProjectPaperRelService projectPaperRelService;
    private final LabPaperService paperService;

    public PageDTO<LabProjectDTO> getProjectList(LabProjectQuery query) {
        QueryWrapper<LabProjectEntity> wrapper = query.addQueryCondition();
        applyCategoryFilter(query, wrapper);
        IPage<LabProjectEntity> page = projectService.page(query.toPage(), wrapper);
        return buildProjectPage(page);
    }

    public PageDTO<LabProjectDTO> getMyProjectList(LabProjectQuery query, Long currentUserId) {
        QueryWrapper<LabProjectEntity> wrapper = query.addQueryCondition();
        applyCategoryFilter(query, wrapper);
        applyProjectAccessFilter(wrapper, currentUserId);
        IPage<LabProjectEntity> page = projectService.page(query.toPage(), wrapper);
        return buildProjectPage(page);
    }

    public LabProjectDTO getProjectDetail(Long id) {
        LabProjectEntity entity = projectService.getById(id);
        if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
            return null;
        }
        LabProjectDTO dto = LabProjectDTO.fromEntity(entity);
        Map<Long, Long> storedCategory = new java.util.HashMap<>(1);
        storedCategory.put(entity.getId(), readCategoryIdFromExtra(entity.getExtra()));
        fillAuthors(java.util.Collections.singletonList(dto));
        fillCategoryInfo(java.util.Collections.singletonList(dto), storedCategory);
        fillRelatedPapers(java.util.Collections.singletonList(dto));
        return dto;
    }

    public PageDTO<LabProjectDTO> getPublicProjectList(LabProjectQuery query) {
        query.setPublished(true);
        query.setVerified(null);
        return getProjectList(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createProject(CreateProjectCommand command, Long ownerUserId) {
        LabProjectEntity entity = new LabProjectEntity();
        fillEntity(entity, command, ownerUserId);
        entity.setCreateTime(new java.util.Date());
        entity.setUpdateTime(new java.util.Date());
        projectService.save(entity);
        Long projectId = entity.getId();
        saveProjectAuthors(projectId, command.getAuthors(), command.getMemberIds(), ownerUserId);
        return projectId;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProject(Long projectId, UpdateProjectCommand command, Long operatorId, boolean isAdmin) {
        LabProjectEntity entity = projectService.getById(projectId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "项目");
        }
        if (!canOperateProject(entity, operatorId, isAdmin)) {
            throw new ApiException(ErrorCode.Business.PERMISSION_NOT_ALLOWED_TO_OPERATE);
        }
        fillEntity(entity, command, entity.getOwnerUserId());
        entity.setUpdateTime(new java.util.Date());
        projectService.updateById(entity);
        saveProjectAuthors(projectId, command.getAuthors(), command.getMemberIds(), operatorId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long projectId, Long operatorId, boolean isAdmin) {
        LabProjectEntity entity = projectService.getById(projectId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
            return;
        }
        if (!canOperateProject(entity, operatorId, isAdmin)) {
            throw new ApiException(ErrorCode.Business.PERMISSION_NOT_ALLOWED_TO_OPERATE);
        }
        projectService.removeById(projectId);
        projectPaperRelService.hardDeleteByProjectId(projectId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePublishStatus(Long projectId, Boolean published, Long operatorId, boolean isAdmin) {
        LabProjectEntity entity = requireProject(projectId);
        if (!isAdmin && (entity.getOwnerUserId() == null || !entity.getOwnerUserId().equals(operatorId))) {
            throw new ApiException(ErrorCode.Business.PERMISSION_NOT_ALLOWED_TO_OPERATE);
        }
        entity.setPublished(Boolean.TRUE.equals(published));
        entity.setUpdateTime(new java.util.Date());
        projectService.updateById(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateVerifyStatus(Long projectId, Boolean verified, Long operatorId, boolean isAdmin) {
        LabProjectEntity entity = requireProject(projectId);
        if (!isAdmin && (entity.getOwnerUserId() == null || !entity.getOwnerUserId().equals(operatorId))) {
            throw new ApiException(ErrorCode.Business.PERMISSION_NOT_ALLOWED_TO_OPERATE);
        }
        entity.setIsVerified(Boolean.TRUE.equals(verified));
        entity.setUpdateTime(new java.util.Date());
        projectService.updateById(entity);
    }

    private void fillEntity(LabProjectEntity entity, CreateProjectCommand command, Long ownerUserId) {
        entity.setTitle(command.getTitle());
        entity.setTitleEn(command.getTitleEn());
        entity.setDescription(command.getDescription());
        entity.setKeywords(command.getKeywords());
        Long categoryId = determineProjectCategoryId(command.getCategoryId(), command.getProjectTypeId(),
            entity.getCategoryId(), entity.getProjectTypeId());
        entity.setCategoryId(categoryId);
        entity.setProjectTypeId(resolveProjectTypeId(command.getProjectTypeId(), categoryId));
        entity.setProjectNumber(command.getProjectNumber());
        entity.setSupporter(command.getSupporter());
        entity.setProjectStartDate(parseDate(command.getProjectStartDate()));
        entity.setProjectEndDate(parseDate(command.getProjectEndDate()));
        entity.setFundingAmount(command.getFundingAmount());
        entity.setAmountDisplay(command.getAmountDisplay());
        entity.setSupportCn(command.getSupportCn());
        entity.setSupportEn(command.getSupportEn());
        entity.setRequirement(command.getRequirement());
        entity.setDescText(command.getDescText());
        entity.setReference(command.getReference());
        entity.setLinkUrl(command.getLinkUrl());
        entity.setGitUrl(command.getGitUrl());
        entity.setHomepageUrl(command.getHomepageUrl());
        entity.setPdfUrl(command.getPdfUrl());
        entity.setExtra(updateCategoryExtra(entity.getExtra(), categoryId));
        entity.setOwnerUserId(ownerUserId);
        entity.setPublished(Boolean.TRUE.equals(command.getPublished()));
        entity.setIsVerified(false);
        entity.setDeleted(false);
        entity.setCreatorId(ownerUserId);
        entity.setUpdaterId(ownerUserId);
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(date.trim());
    }

    private LabProjectEntity requireProject(Long projectId) {
        LabProjectEntity entity = projectService.getById(projectId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "项目");
        }
        return entity;
    }

    private void saveProjectAuthors(Long projectId, List<CreateProjectCommand.ProjectAuthorCommand> authors,
                                    List<Long> memberIds, Long operatorId) {
        projectAuthorService.lambdaUpdate()
            .eq(LabProjectAuthorEntity::getProjectId, projectId)
            .remove();
        if (authors != null && !authors.isEmpty()) {
            Map<Long, LabUserEntity> resolvedUsers = new java.util.HashMap<>();
            for (CreateProjectCommand.ProjectAuthorCommand authorCmd : authors) {
                if (authorCmd.getUserId() == null) {
                    LabUserEntity matched = findUserByName(authorCmd.getName(), authorCmd.getNameEn());
                    if (matched != null) {
                        authorCmd.setUserId(matched.getId());
                        resolvedUsers.put(matched.getId(), matched);
                    }
                }
            }
            List<Long> userIds = authors.stream()
                .map(CreateProjectCommand.ProjectAuthorCommand::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
            Map<Long, LabUserEntity> userMap = userIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : labUserService.listByIds(userIds).stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(LabUserEntity::getId, u -> u));
            if (!resolvedUsers.isEmpty()) {
                Map<Long, LabUserEntity> merged = new java.util.HashMap<>(userMap);
                merged.putAll(resolvedUsers);
                userMap = merged;
            }
            int fallbackOrder = 1;
            for (CreateProjectCommand.ProjectAuthorCommand authorCmd : authors) {
                LabProjectAuthorEntity entity = new LabProjectAuthorEntity();
                entity.setProjectId(projectId);
                entity.setAuthorOrder(authorCmd.getAuthorOrder() != null ? authorCmd.getAuthorOrder() : fallbackOrder++);
                Long resolvedUserId = authorCmd.getUserId();
                LabUserEntity user = resolvedUserId != null ? userMap.get(resolvedUserId) : null;
                if (user == null) {
                    user = findUserByName(authorCmd.getName(), authorCmd.getNameEn());
                    if (user != null) {
                        resolvedUserId = user.getId();
                    }
                }
                entity.setUserId(resolvedUserId);
                String name = authorCmd.getName();
                String nameEn = authorCmd.getNameEn();
                String email = authorCmd.getEmail();
                String affiliation = authorCmd.getAffiliation();
                if (user != null) {
                    if (name == null || name.trim().isEmpty()) {
                        name = user.getRealName();
                    }
                    if (nameEn == null || nameEn.trim().isEmpty()) {
                        nameEn = user.getEnglishName();
                    }
                    if (email == null || email.trim().isEmpty()) {
                        email = user.getEmail();
                    }
                    if (affiliation == null || affiliation.trim().isEmpty()) {
                        affiliation = user.getResearchArea();
                    }
                }
                entity.setName(name);
                entity.setNameEn(nameEn);
                entity.setEmail(email);
                entity.setAffiliation(affiliation);
                entity.setIsCorresponding(Boolean.TRUE.equals(authorCmd.getCorresponding()));
                entity.setRole(authorCmd.getRole());
                entity.setVisible(authorCmd.getVisible() == null ? true : authorCmd.getVisible());
                entity.setDeleted(false);
                entity.setCreateTime(new java.util.Date());
                entity.setUpdateTime(new java.util.Date());
                entity.setCreatorId(operatorId);
                entity.setUpdaterId(operatorId);
                projectAuthorService.save(entity);
            }
            backfillAuthorUserIds(projectId, operatorId);
            return;
        }
        if (memberIds != null && !memberIds.isEmpty()) {
            List<Long> normalized = memberIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
            if (normalized.isEmpty()) {
                return;
            }
            Map<Long, LabUserEntity> userMap = labUserService.listByIds(normalized).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(LabUserEntity::getId, u -> u));
            int order = 1;
            for (Long memberId : normalized) {
                LabUserEntity user = userMap.get(memberId);
                if (user == null) {
                    continue;
                }
                LabProjectAuthorEntity author = new LabProjectAuthorEntity();
                author.setProjectId(projectId);
                author.setUserId(memberId);
                author.setName(user.getRealName());
                author.setNameEn(user.getEnglishName());
                author.setEmail(user.getEmail());
                author.setAffiliation(user.getResearchArea());
                author.setAuthorOrder(order++);
                author.setIsCorresponding(false);
                author.setRole(null);
                author.setVisible(true);
                author.setDeleted(false);
                author.setCreateTime(new java.util.Date());
                author.setUpdateTime(new java.util.Date());
                author.setCreatorId(operatorId);
                author.setUpdaterId(operatorId);
                projectAuthorService.save(author);
            }
        }
    }

    private void backfillAuthorUserIds(Long projectId, Long operatorId) {
        if (projectId == null) {
            return;
        }
        List<LabProjectAuthorEntity> pending = projectAuthorService.lambdaQuery()
            .eq(LabProjectAuthorEntity::getProjectId, projectId)
            .isNull(LabProjectAuthorEntity::getUserId)
            .eq(LabProjectAuthorEntity::getDeleted, false)
            .list();
        if (pending.isEmpty()) {
            return;
        }
        for (LabProjectAuthorEntity author : pending) {
            LabUserEntity matched = findUserByName(author.getName(), author.getNameEn());
            if (matched == null) {
                continue;
            }
            author.setUserId(matched.getId());
            if (author.getName() == null || author.getName().trim().isEmpty()) {
                author.setName(matched.getRealName());
            }
            if (author.getNameEn() == null || author.getNameEn().trim().isEmpty()) {
                author.setNameEn(matched.getEnglishName());
            }
            if (author.getEmail() == null || author.getEmail().trim().isEmpty()) {
                author.setEmail(matched.getEmail());
            }
            if (author.getAffiliation() == null || author.getAffiliation().trim().isEmpty()) {
                author.setAffiliation(matched.getResearchArea());
            }
            author.setUpdaterId(operatorId);
            author.setUpdateTime(new java.util.Date());
            projectAuthorService.updateById(author);
        }
    }

    private Integer resolveProjectTypeId(Integer requestedProjectTypeId, Long categoryId) {
        if (requestedProjectTypeId != null) {
            return requestedProjectTypeId;
        }
        if (categoryId == null) {
            return null;
        }
        CategoryCompatibilityService.LegacyTypeMapping mapping =
            categoryCompatibilityService.getLegacyTypeByCategory(categoryId);
        if (mapping == null || mapping.getType() == null || mapping.getType() != 2) {
            return null;
        }
        if (mapping.getSubType() != null) {
            return mapping.getSubType();
        }
        Long leafId = categoryCompatibilityService.resolveWritableLeafCategoryId(categoryId);
        if (leafId != null && !leafId.equals(categoryId)) {
            CategoryCompatibilityService.LegacyTypeMapping leafMapping =
                categoryCompatibilityService.getLegacyTypeByCategory(leafId);
            if (leafMapping != null && Integer.valueOf(2).equals(leafMapping.getType())
                && leafMapping.getSubType() != null) {
                return leafMapping.getSubType();
            }
        }
        return LabAchievementEntity.ProjectType.OTHER_PROJECT.getCode();
    }

    private Long determineProjectCategoryId(Long requestedCategoryId, Integer requestedProjectTypeId,
                                            Long currentCategoryId, Integer currentProjectTypeId) {
        Long categoryId = requestedCategoryId != null ? requestedCategoryId : currentCategoryId;
        if (categoryId != null) {
            Long leafId = categoryCompatibilityService.resolveWritableLeafCategoryId(categoryId);
            if (leafId != null) {
                categoryId = leafId;
            }
            return categoryId;
        }
        Integer fallbackType = requestedProjectTypeId != null ? requestedProjectTypeId : currentProjectTypeId;
        if (fallbackType == null) {
            return null;
        }
        return categoryCompatibilityService.getCategoryIdByLegacyType(2, fallbackType);
    }

    private Long resolveCategoryId(Integer projectTypeId) {
        if (projectTypeId == null) {
            return null;
        }
        return categoryCompatibilityService.getCategoryIdByLegacyType(2, projectTypeId);
    }

    private void fillCategoryInfo(List<LabProjectDTO> dtoList, Map<Long, Long> storedCategoryMap) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        dtoList.forEach(dto -> {
            Long categoryId = dto.getCategoryId();
            if (categoryId == null && storedCategoryMap != null) {
                categoryId = storedCategoryMap.get(dto.getId());
            }
            if (categoryId == null) {
                categoryId = resolveCategoryId(dto.getProjectTypeId());
            }
            dto.setCategoryId(categoryId);
            if (dto.getTypeInfo() != null) {
                dto.getTypeInfo().setCategoryId(categoryId);
            }
        });
        List<Long> ids = dtoList.stream()
            .map(LabProjectDTO::getCategoryId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }
        List<LabAchievementCategoryEntity> categories = categoryService.listByIds(ids);
        Map<Long, LabAchievementCategoryEntity> catMap = categories.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(LabAchievementCategoryEntity::getId, c -> c));
        java.util.Set<Long> parentIds = categories.stream()
            .map(LabAchievementCategoryEntity::getParentId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, LabAchievementCategoryEntity> parentMap = parentIds.isEmpty()
            ? java.util.Collections.emptyMap()
            : categoryService.listByIds(parentIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(LabAchievementCategoryEntity::getId, c -> c));
        for (LabProjectDTO dto : dtoList) {
            Long categoryId = dto.getCategoryId();
            if (categoryId == null) {
                continue;
            }
            LabAchievementCategoryEntity current = catMap.get(categoryId);
            if (current == null) {
                continue;
            }
            dto.setCategoryName(current.getCategoryName());
            String fullPath;
            LabAchievementCategoryEntity parent = current.getParentId() != null ? parentMap.get(current.getParentId()) : null;
            if (parent != null && parent.getCategoryName() != null) {
                fullPath = parent.getCategoryName() + " / " + current.getCategoryName();
            } else {
                fullPath = current.getCategoryName();
            }
            dto.setCategoryFullPath(fullPath);
            if (dto.getTypeInfo() != null) {
                dto.getTypeInfo().setCategoryName(current.getCategoryName());
                dto.getTypeInfo().setCategoryFullPath(fullPath);
            }
        }
    }

    private void fillAuthors(List<LabProjectDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        List<Long> ids = dtoList.stream().map(LabProjectDTO::getId).collect(Collectors.toList());
        Map<Long, List<LabPaperAuthorDTO>> authorMap = projectAuthorService.getAuthorsByProjectIds(ids)
            .stream()
            .collect(Collectors.groupingBy(
                LabProjectAuthorEntity::getProjectId,
                Collectors.mapping(this::convertAuthor, Collectors.toList())
            ));
        dtoList.forEach(dto -> dto.setAuthors(authorMap.getOrDefault(dto.getId(), java.util.Collections.emptyList())));
    }

    private LabPaperAuthorDTO convertAuthor(LabProjectAuthorEntity entity) {
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

    private void fillRelatedPapers(List<LabProjectDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        List<Long> projectIds = dtoList.stream()
            .map(LabProjectDTO::getId)
            .collect(Collectors.toList());
        if (projectIds.isEmpty()) {
            return;
        }
        List<LabProjectPaperRelEntity> relations = projectPaperRelService.lambdaQuery()
            .in(LabProjectPaperRelEntity::getProjectId, projectIds)
            .list();
        if (relations.isEmpty()) {
            dtoList.forEach(dto -> dto.setRelatedPapers(Collections.emptyList()));
            return;
        }
        Map<Long, List<LabProjectPaperRelEntity>> relMap = relations.stream()
            .collect(Collectors.groupingBy(LabProjectPaperRelEntity::getProjectId));
        List<Long> paperIds = relations.stream()
            .map(LabProjectPaperRelEntity::getPaperId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, LabPaperEntity> paperMap = paperService.listByIds(paperIds).stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(LabPaperEntity::getId, p -> p));
        Map<Long, Long> paperCategoryMap = new java.util.HashMap<>();
        java.util.Set<Long> categoryIds = new java.util.HashSet<>();
        for (LabPaperEntity paper : paperMap.values()) {
            Long categoryId = resolvePaperCategoryId(paper);
            if (categoryId != null) {
                paperCategoryMap.put(paper.getId(), categoryId);
                categoryIds.add(categoryId);
            }
        }
        Map<Long, LabAchievementCategoryEntity> paperCategoryEntityMap = categoryIds.isEmpty()
            ? java.util.Collections.emptyMap()
            : categoryService.listByIds(new java.util.ArrayList<>(categoryIds)).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(LabAchievementCategoryEntity::getId, c -> c));
        for (LabProjectDTO dto : dtoList) {
            List<LabProjectPaperRelEntity> rels = relMap.get(dto.getId());
            if (rels == null || rels.isEmpty()) {
                dto.setRelatedPapers(Collections.emptyList());
                continue;
            }
            List<RelatedPaperDTO> related = rels.stream()
                .map(rel -> {
                    LabPaperEntity paper = paperMap.get(rel.getPaperId());
                    if (paper == null) {
                        return null;
                    }
                    RelatedPaperDTO item = new RelatedPaperDTO();
                    item.setId(paper.getId());
                    item.setTitle(paper.getTitle());
                    item.setPaperTypeId(paper.getPaperTypeId());
                    item.setPaperTypeDesc(resolvePaperTypeDesc(paper.getPaperTypeId()));
                    item.setPublishDate(paper.getPublishDate());
                    Long categoryId = paperCategoryMap.get(paper.getId());
                    if (categoryId != null) {
                        item.setCategoryId(categoryId);
                        LabAchievementCategoryEntity category = paperCategoryEntityMap.get(categoryId);
                        if (category != null) {
                            item.setCategoryName(category.getCategoryName());
                        }
                    }
                    return item;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            dto.setRelatedPapers(related);
        }
    }

    private boolean canOperateProject(LabProjectEntity entity, Long operatorId, boolean isAdmin) {
        if (isAdmin) {
            return true;
        }
        if (operatorId == null) {
            return false;
        }
        if (entity.getOwnerUserId() != null && entity.getOwnerUserId().equals(operatorId)) {
            return true;
        }
        return projectAuthorService.isMember(entity.getId(), operatorId);
    }

    private PageDTO<LabProjectDTO> buildProjectPage(IPage<LabProjectEntity> page) {
        Map<Long, Long> storedCategoryMap = new java.util.HashMap<>();
        for (LabProjectEntity record : page.getRecords()) {
            if (record != null && record.getId() != null) {
                storedCategoryMap.put(record.getId(), readCategoryIdFromExtra(record.getExtra()));
            }
        }
        List<LabProjectDTO> list = page.getRecords().stream()
            .map(LabProjectDTO::fromEntity)
            .collect(Collectors.toList());
        fillAuthors(list);
        fillCategoryInfo(list, storedCategoryMap);
        fillRelatedPapers(list);
        return new PageDTO<>(list, page.getTotal());
    }

    private void applyProjectAccessFilter(QueryWrapper<LabProjectEntity> wrapper, Long currentUserId) {
        if (currentUserId == null) {
            return;
        }
        List<Long> participantProjectIds = projectAuthorService.lambdaQuery()
            .eq(LabProjectAuthorEntity::getUserId, currentUserId)
            .eq(LabProjectAuthorEntity::getDeleted, false)
            .list()
            .stream()
            .map(LabProjectAuthorEntity::getProjectId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        if (participantProjectIds.isEmpty()) {
            wrapper.eq("owner_user_id", currentUserId);
        } else {
            wrapper.and(w -> w.eq("owner_user_id", currentUserId)
                .or()
                .in("id", participantProjectIds));
        }
    }

    private void applyCategoryFilter(LabProjectQuery query, QueryWrapper<LabProjectEntity> wrapper) {
        if (query == null) {
            return;
        }
        Long categoryId = query.getCategoryId();
        Long parentCategoryId = query.getParentCategoryId();
        if (categoryId == null && parentCategoryId == null) {
            return;
        }
        List<Long> categoryIds = new java.util.ArrayList<>();
        if (categoryId != null) {
            Long leafId = categoryCompatibilityService.resolveWritableLeafCategoryId(categoryId);
            categoryIds.add(leafId != null ? leafId : categoryId);
        } else {
            List<LabAchievementCategoryEntity> children = categoryService.lambdaQuery()
                .eq(LabAchievementCategoryEntity::getParentId, parentCategoryId)
                .eq(LabAchievementCategoryEntity::getDeleted, false)
                .list();
            if (children != null && !children.isEmpty()) {
                categoryIds.addAll(children.stream()
                    .map(LabAchievementCategoryEntity::getId)
                    .collect(Collectors.toList()));
            }
            categoryIds.add(parentCategoryId);
        }
        categoryIds = categoryIds.stream()
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        if (categoryIds.isEmpty()) {
            wrapper.eq("1", 0);
            return;
        }
        if (categoryIds.size() == 1) {
            wrapper.eq("category_id", categoryIds.get(0));
        } else {
            wrapper.in("category_id", categoryIds);
        }
    }

    private String resolvePaperTypeDesc(Integer paperTypeId) {
        if (paperTypeId == null) {
            return null;
        }
        for (com.agileboot.domain.lab.achievement.db.LabAchievementEntity.PaperType type
            : com.agileboot.domain.lab.achievement.db.LabAchievementEntity.PaperType.values()) {
            if (type.getCode().equals(paperTypeId)) {
                return type.getDesc();
            }
        }
        return null;
    }

    private Long resolvePaperCategoryId(LabPaperEntity paper) {
        if (paper == null) {
            return null;
        }
        Long categoryId = paper.getCategoryId();
        if (categoryId != null) {
            Long leafId = categoryCompatibilityService.resolveWritableLeafCategoryId(categoryId);
            return leafId != null ? leafId : categoryId;
        }
        if (paper.getPaperTypeId() == null) {
            return null;
        }
        return categoryCompatibilityService.getCategoryIdByLegacyType(1, paper.getPaperTypeId());
    }

    private String updateCategoryExtra(String extra, Long categoryId) {
        JSONObject jsonObject = StrUtil.isBlank(extra) ? new JSONObject() : JSONUtil.parseObj(extra, true, true);
        if (categoryId == null) {
            jsonObject.remove("categoryId");
        } else {
            jsonObject.set("categoryId", categoryId);
        }
        return jsonObject.isEmpty() ? null : jsonObject.toString();
    }

    private Long readCategoryIdFromExtra(String extra) {
        if (StrUtil.isBlank(extra)) {
            return null;
        }
        try {
            JSONObject jsonObject = JSONUtil.parseObj(extra, true, true);
            return jsonObject.getLong("categoryId");
        } catch (Exception e) {
            return null;
        }
    }

    private LabUserEntity findUserByName(String name, String nameEn) {
        String cn = StrUtil.trimToNull(name);
        if (cn != null) {
            LabUserEntity user = labUserService.findBestByRealName(cn);
            if (user != null) {
                return user;
            }
        }
        String en = StrUtil.trimToNull(nameEn);
        if (en != null) {
            return labUserService.findBestByEnglishName(en);
        }
        return null;
    }
}
