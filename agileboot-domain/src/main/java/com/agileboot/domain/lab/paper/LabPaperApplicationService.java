package com.agileboot.domain.lab.paper;

import cn.hutool.core.util.StrUtil;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.lab.achievement.dto.LabFundAssociationDTO;
import com.agileboot.domain.lab.category.CategoryCompatibilityService;
import com.agileboot.domain.lab.category.db.LabAchievementCategoryEntity;
import com.agileboot.domain.lab.category.db.LabAchievementCategoryService;
import com.agileboot.domain.lab.common.dto.RelatedProjectDTO;
import com.agileboot.domain.lab.fund.db.LabFundPaperRelEntity;
import com.agileboot.domain.lab.fund.db.LabFundPaperRelService;
import com.agileboot.domain.lab.paper.author.LabPaperAuthorEntity;
import com.agileboot.domain.lab.paper.author.LabPaperAuthorService;
import com.agileboot.domain.lab.paper.command.CreatePaperCommand;
import com.agileboot.domain.lab.paper.command.UpdatePaperCommand;
import com.agileboot.domain.lab.paper.db.LabPaperEntity;
import com.agileboot.domain.lab.paper.db.LabPaperService;
import com.agileboot.domain.lab.paper.dto.LabPaperAuthorDTO;
import com.agileboot.domain.lab.paper.dto.LabPaperDTO;
import com.agileboot.domain.lab.paper.query.LabPaperQuery;
import com.agileboot.domain.lab.project.db.LabProjectEntity;
import com.agileboot.domain.lab.project.db.LabProjectService;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabPaperApplicationService {

    private static final DateTimeFormatter PUBLISH_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final LabPaperService paperService;
    private final LabPaperAuthorService paperAuthorService;
    private final LabFundPaperRelService fundPaperRelService;
    private final LabProjectPaperRelService projectPaperRelService;
    private final LabProjectService projectService;
    private final LabUserService labUserService;
    private final CategoryCompatibilityService categoryCompatibilityService;
    private final LabAchievementCategoryService categoryService;

    /**
     * 查询论文分页列表
     */
    public PageDTO<LabPaperDTO> getPaperList(LabPaperQuery query) {
        QueryWrapper<LabPaperEntity> wrapper = query.toQueryWrapper();
        applyCategoryFilter(query, wrapper);
        IPage<LabPaperEntity> page = paperService.page(query.toPage(), wrapper);
        return buildPaperPage(page);
    }

    public PageDTO<LabPaperDTO> getMyPaperList(LabPaperQuery query, Long currentUserId) {
        QueryWrapper<LabPaperEntity> wrapper = query.toQueryWrapper();
        applyCategoryFilter(query, wrapper);
        applyPaperAccessFilter(wrapper, currentUserId);
        IPage<LabPaperEntity> page = paperService.page(query.toPage(), wrapper);
        return buildPaperPage(page);
    }

    public PageDTO<LabPaperDTO> getPublicPaperList(LabPaperQuery query) {
        query.setPublished(true);
        query.setVerified(null);
        return getPaperList(query);
    }

    public LabPaperDTO getPaperDetail(Long paperId) {
        LabPaperEntity entity = paperService.getById(paperId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
            return null;
        }
        LabPaperDTO dto = LabPaperDTO.fromEntity(entity);
        fillAuthors(Collections.singletonList(dto));
        fillFunds(Collections.singletonList(dto));
        fillRelatedProjects(Collections.singletonList(dto));
        fillTypeInfo(Collections.singletonList(dto));
        return dto;
    }

    public List<LabFundAssociationDTO> getPaperFunds(Long paperId) {
        LabPaperEntity entity = paperService.getById(paperId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "论文");
        }
        return fundPaperRelService.lambdaQuery()
            .eq(LabFundPaperRelEntity::getPaperId, paperId)
            .list()
            .stream()
            .map(rel -> {
                LabFundAssociationDTO dto = new LabFundAssociationDTO();
                dto.setFundId(rel.getFundId());
                dto.setAmount(rel.getAmount());
                return dto;
            }).collect(Collectors.toList());
    }

    private void fillAuthors(List<LabPaperDTO> dtoList) {
        if (dtoList.isEmpty()) {
            return;
        }
        List<Long> ids = dtoList.stream().map(LabPaperDTO::getId).collect(Collectors.toList());
        Map<Long, List<LabPaperAuthorDTO>> authorMap = paperAuthorService.getAuthorsByPaperIds(ids)
            .stream()
            .collect(Collectors.groupingBy(
                LabPaperAuthorEntity::getPaperId,
                Collectors.mapping(LabPaperAuthorDTO::fromEntity, Collectors.toList())
            ));
        dtoList.forEach(dto -> dto.setAuthors(authorMap.getOrDefault(dto.getId(), Collections.emptyList())));
    }

    private void fillFunds(List<LabPaperDTO> dtoList) {
        if (dtoList.isEmpty()) {
            return;
        }
        List<Long> ids = dtoList.stream().map(LabPaperDTO::getId).collect(Collectors.toList());
        Map<Long, List<LabFundAssociationDTO>> fundMap = fundPaperRelService.lambdaQuery()
            .in(LabFundPaperRelEntity::getPaperId, ids)
            .list()
            .stream()
            .collect(Collectors.groupingBy(
                LabFundPaperRelEntity::getPaperId,
                Collectors.mapping(rel -> {
                    LabFundAssociationDTO dto = new LabFundAssociationDTO();
                    dto.setFundId(rel.getFundId());
                    dto.setAmount(rel.getAmount());
                    return dto;
                }, Collectors.toList())
            ));
        dtoList.forEach(dto -> dto.setFundAssociations(fundMap.getOrDefault(dto.getId(), Collections.emptyList())));
    }

    private void fillPaperEntity(LabPaperEntity entity, CreatePaperCommand command, Long ownerUserId) {
        entity.setTitle(command.getTitle());
        entity.setTitleEn(command.getTitleEn());
        entity.setDescription(command.getDescription());
        entity.setKeywords(command.getKeywords());
        Long categoryId = command.getCategoryId() != null ? command.getCategoryId() : entity.getCategoryId();
        if (categoryId != null) {
            Long leafId = categoryCompatibilityService.resolveWritableLeafCategoryId(categoryId);
            if (leafId != null) {
                categoryId = leafId;
            }
        }
        if (categoryId == null && command.getPaperTypeId() != null) {
            categoryId = categoryCompatibilityService.getCategoryIdByLegacyType(1, command.getPaperTypeId());
        }
        entity.setCategoryId(categoryId);
        Integer paperTypeId = command.getPaperTypeId() != null ? command.getPaperTypeId() : entity.getPaperTypeId();
        if ((paperTypeId == null || paperTypeId <= 0) && categoryId != null) {
            CategoryCompatibilityService.LegacyTypeMapping mapping =
                categoryCompatibilityService.getLegacyTypeByCategory(categoryId);
            if (mapping != null && Integer.valueOf(1).equals(mapping.getType())) {
                paperTypeId = mapping.getSubType();
            }
        }
        if (paperTypeId != null) {
            entity.setPaperTypeId(paperTypeId);
        }
        entity.setPublication(command.getPublication());
        entity.setConferenceName(command.getConferenceName());
        entity.setIssueNumber(command.getIssueNumber());
        entity.setPublicationPlace(command.getPublicationPlace());
        entity.setPublisher(command.getPublisher());
        entity.setPageRange(command.getPageRange());
        LocalDate publishDate = parseDate(command.getPublishDate());
        entity.setPublishDate(publishDate);
        if (publishDate != null) {
            entity.setPublishDateDisplay(publishDate.format(PUBLISH_DATE_FORMATTER));
            entity.setPublishDatePrecision(3);
        } else {
            entity.setPublishDateDisplay(null);
            entity.setPublishDatePrecision(null);
        }
        entity.setReference(command.getReference());
        entity.setDoi(command.getDoi());
        entity.setLinkUrl(command.getLinkUrl());
        entity.setExtraUrl(command.getLinkUrl());
        entity.setGitUrl(command.getGitUrl());
        entity.setHomepageUrl(command.getHomepageUrl());
        entity.setPdfUrl(command.getPdfUrl());
        entity.setNotes(command.getNotes());
        entity.setOwnerUserId(ownerUserId);
        entity.setPublished(Boolean.TRUE.equals(command.getPublished()));
        entity.setIsVerified(false);
        entity.setDeleted(false);
        entity.setCreatorId(ownerUserId);
        entity.setUpdaterId(ownerUserId);
        entity.setExtra(null);
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), PUBLISH_DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                "发表日期格式必须为 yyyy-MM-dd，例如 2024-01-08");
        }
    }

    private void saveAuthors(Long paperId, List<CreatePaperCommand.CreatePaperAuthorCommand> authors, Long operatorId) {
        if (authors == null) {
            return;
        }
        paperAuthorService.hardDeleteAllByPaperId(paperId);
        if (authors.isEmpty()) {
            return;
        }
        Map<Long, LabUserEntity> resolvedUsers = new java.util.HashMap<>();
        for (CreatePaperCommand.CreatePaperAuthorCommand cmd : authors) {
            if (cmd.getUserId() == null) {
                LabUserEntity matched = matchUserByName(cmd.getName(), cmd.getNameEn());
                if (matched != null) {
                    cmd.setUserId(matched.getId());
                    resolvedUsers.put(matched.getId(), matched);
                }
            }
        }
        List<Long> userIds = authors.stream()
            .map(CreatePaperCommand.CreatePaperAuthorCommand::getUserId)
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
        final Map<Long, LabUserEntity> finalUserMap = userMap;
        authors.forEach(cmd -> {
            LabPaperAuthorEntity author = new LabPaperAuthorEntity();
            author.setPaperId(paperId);
            Long resolvedUserId = cmd.getUserId();
            LabUserEntity user = resolvedUserId != null ? finalUserMap.get(resolvedUserId) : null;
            if (user == null) {
                user = matchUserByName(cmd.getName(), cmd.getNameEn());
                if (user != null) {
                    resolvedUserId = user.getId();
                }
            }
            author.setUserId(resolvedUserId);
            String name = cmd.getName();
            String nameEn = cmd.getNameEn();
            String email = cmd.getEmail();
            String affiliation = cmd.getAffiliation();
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
            author.setName(name);
            author.setNameEn(nameEn);
            author.setEmail(email);
            author.setAffiliation(affiliation);
            author.setAuthorOrder(cmd.getAuthorOrder());
            author.setIsCorresponding(Boolean.TRUE.equals(cmd.getCorresponding()));
            author.setRole(cmd.getRole());
            author.setVisible(Boolean.TRUE.equals(cmd.getVisible()));
            author.setDeleted(false);
            paperAuthorService.save(author);
        });
        backfillPaperAuthorUserIds(paperId, operatorId);
    }

    private void saveFunds(Long paperId, List<CreatePaperCommand.PaperFundCommand> funds) {

        fundPaperRelService.lambdaUpdate()
            .eq(LabFundPaperRelEntity::getPaperId, paperId)
            .remove();
        if (funds == null || funds.isEmpty()) {
            return;
        }
        funds.forEach(cmd -> {
            LabFundPaperRelEntity rel = new LabFundPaperRelEntity();
            rel.setPaperId(paperId);
            rel.setFundId(cmd.getFundId());
            rel.setAmount(cmd.getAmount());
            fundPaperRelService.save(rel);
        });
    }

    private void saveProjectRelations(Long paperId, List<Long> projectIds, Long operatorId) {
        projectPaperRelService.hardDeleteByPaperId(paperId);
        if (projectIds == null || projectIds.isEmpty()) {
            return;
        }
        List<Long> normalized = projectIds.stream()
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        if (normalized.isEmpty()) {
            return;
        }
        Set<Long> exists = projectService.listByIds(normalized).stream()
            .filter(Objects::nonNull)
            .map(LabProjectEntity::getId)
            .collect(Collectors.toSet());
        if (exists.isEmpty()) {
            return;
        }
        Date now = new Date();
        for (Long projectId : normalized) {
            if (!exists.contains(projectId)) {
                continue;
            }
            LabProjectPaperRelEntity rel = new LabProjectPaperRelEntity();
            rel.setProjectId(projectId);
            rel.setPaperId(paperId);
            rel.setCreatorId(operatorId);
            rel.setUpdaterId(operatorId);
            rel.setCreateTime(now);
            rel.setUpdateTime(now);
            rel.setDeleted(false);
            projectPaperRelService.save(rel);
        }
    }

    private void fillRelatedProjects(List<LabPaperDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        List<Long> paperIds = dtoList.stream()
            .map(LabPaperDTO::getId)
            .collect(Collectors.toList());
        if (paperIds.isEmpty()) {
            return;
        }
        List<LabProjectPaperRelEntity> relations = projectPaperRelService.lambdaQuery()
            .in(LabProjectPaperRelEntity::getPaperId, paperIds)
            .list();
        if (relations.isEmpty()) {
            dtoList.forEach(dto -> {
                dto.setRelatedProjects(Collections.emptyList());
                dto.setProjectIds(Collections.emptyList());
            });
            return;
        }
        Map<Long, List<LabProjectPaperRelEntity>> relMap = relations.stream()
            .collect(Collectors.groupingBy(LabProjectPaperRelEntity::getPaperId));
        List<Long> projectIds = relations.stream()
            .map(LabProjectPaperRelEntity::getProjectId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, LabProjectEntity> projectMap = projectService.listByIds(projectIds).stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(LabProjectEntity::getId, p -> p));
        Map<Long, Long> projectCategoryMap = new java.util.HashMap<>();
        java.util.Set<Long> projectCategoryIds = new java.util.HashSet<>();
        for (LabProjectEntity project : projectMap.values()) {
            Long categoryId = resolveProjectCategoryId(project);
            if (categoryId != null) {
                projectCategoryMap.put(project.getId(), categoryId);
                projectCategoryIds.add(categoryId);
            }
        }
        Map<Long, LabAchievementCategoryEntity> projectCategoryEntityMap = projectCategoryIds.isEmpty()
            ? Collections.emptyMap()
            : categoryService.listByIds(new java.util.ArrayList<>(projectCategoryIds)).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(LabAchievementCategoryEntity::getId, c -> c));
        for (LabPaperDTO dto : dtoList) {
            List<LabProjectPaperRelEntity> rels = relMap.get(dto.getId());
            if (rels == null || rels.isEmpty()) {
                dto.setRelatedProjects(Collections.emptyList());
                dto.setProjectIds(Collections.emptyList());
                continue;
            }
            List<Long> currentProjectIds = rels.stream()
                .map(LabProjectPaperRelEntity::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            List<RelatedProjectDTO> related = currentProjectIds.stream()
                .map(rel -> {
                    LabProjectEntity project = projectMap.get(rel);
                    if (project == null) {
                        return null;
                    }
                    RelatedProjectDTO item = new RelatedProjectDTO();
                    item.setId(project.getId());
                    item.setTitle(project.getTitle());
                    item.setProjectTypeId(project.getProjectTypeId());
                    item.setProjectTypeDesc(resolveProjectTypeDesc(project.getProjectTypeId()));
                    item.setProjectStartDate(project.getProjectStartDate());
                    item.setProjectEndDate(project.getProjectEndDate());
                    Long projectCategoryId = projectCategoryMap.get(project.getId());
                    if (projectCategoryId != null) {
                        item.setCategoryId(projectCategoryId);
                        LabAchievementCategoryEntity category = projectCategoryEntityMap.get(projectCategoryId);
                        if (category != null) {
                            item.setCategoryName(category.getCategoryName());
                        }
                    }
                    return item;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            dto.setRelatedProjects(related);
            dto.setProjectIds(currentProjectIds);
        }
    }

    private PageDTO<LabPaperDTO> buildPaperPage(IPage<LabPaperEntity> page) {
        List<LabPaperEntity> records = page.getRecords();
        List<LabPaperDTO> dtoList = records.stream()
            .map(LabPaperDTO::fromEntity)
            .collect(Collectors.toList());

        fillAuthors(dtoList);
        fillFunds(dtoList);
        fillRelatedProjects(dtoList);
        fillTypeInfo(dtoList);

        return new PageDTO<>(dtoList, page.getTotal());
    }

    private void applyPaperAccessFilter(QueryWrapper<LabPaperEntity> wrapper, Long currentUserId) {
        if (currentUserId == null) {
            return;
        }
        List<Long> participantPaperIds = paperAuthorService.lambdaQuery()
            .eq(LabPaperAuthorEntity::getUserId, currentUserId)
            .eq(LabPaperAuthorEntity::getDeleted, false)
            .list()
            .stream()
            .map(LabPaperAuthorEntity::getPaperId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        if (participantPaperIds.isEmpty()) {
            wrapper.eq("owner_user_id", currentUserId);
        } else {
            wrapper.and(w -> w.eq("owner_user_id", currentUserId)
                .or()
                .in("id", participantPaperIds));
        }
    }

    private void applyCategoryFilter(LabPaperQuery query, QueryWrapper<LabPaperEntity> wrapper) {
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

    private String resolveProjectTypeDesc(Integer typeId) {
        if (typeId == null) {
            return null;
        }
        for (com.agileboot.domain.lab.achievement.db.LabAchievementEntity.ProjectType type
            : com.agileboot.domain.lab.achievement.db.LabAchievementEntity.ProjectType.values()) {
            if (type.getCode().equals(typeId)) {
                return type.getDesc();
            }
        }
        return null;
    }

    private void backfillPaperAuthorUserIds(Long paperId, Long operatorId) {
        if (paperId == null) {
            return;
        }
        List<LabPaperAuthorEntity> pending = paperAuthorService.lambdaQuery()
            .eq(LabPaperAuthorEntity::getPaperId, paperId)
            .isNull(LabPaperAuthorEntity::getUserId)
            .eq(LabPaperAuthorEntity::getDeleted, false)
            .list();
        if (pending.isEmpty()) {
            return;
        }
        Date now = new Date();
        for (LabPaperAuthorEntity author : pending) {
            LabUserEntity matched = matchUserByName(author.getName(), author.getNameEn());
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
            author.setUpdateTime(now);
            author.setUpdaterId(operatorId);
            paperAuthorService.updateById(author);
        }
    }

    private LabUserEntity matchUserByName(String name, String nameEn) {
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

    private Long resolveProjectCategoryId(LabProjectEntity project) {
        if (project == null) {
            return null;
        }
        Long categoryId = project.getCategoryId();
        if (categoryId != null) {
            Long leafId = categoryCompatibilityService.resolveWritableLeafCategoryId(categoryId);
            return leafId != null ? leafId : categoryId;
        }
        if (project.getProjectTypeId() == null) {
            return null;
        }
        return categoryCompatibilityService.getCategoryIdByLegacyType(2, project.getProjectTypeId());
    }

    private void fillTypeInfo(List<LabPaperDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        Set<Long> categoryIds = new java.util.HashSet<>();
        for (LabPaperDTO dto : dtoList) {
            Long categoryId = dto.getCategoryId();
            if (categoryId == null && dto.getPaperTypeId() != null) {
                categoryId = categoryCompatibilityService.getCategoryIdByLegacyType(1, dto.getPaperTypeId());
                dto.setCategoryId(categoryId);
            }
            if (categoryId != null) {
                Long leafId = categoryCompatibilityService.resolveWritableLeafCategoryId(categoryId);
                if (leafId != null) {
                    categoryId = leafId;
                    dto.setCategoryId(categoryId);
                }
                categoryIds.add(categoryId);
            }
            if (dto.getTypeInfo() != null) {
                Integer legacySubType = dto.getPaperTypeId();
                if ((legacySubType == null || legacySubType <= 0) && categoryId != null) {
                    CategoryCompatibilityService.LegacyTypeMapping mapping =
                        categoryCompatibilityService.getLegacyTypeByCategory(categoryId);
                    if (mapping != null && Integer.valueOf(1).equals(mapping.getType())) {
                        legacySubType = mapping.getSubType();
                    }
                }
                dto.getTypeInfo().setLegacyType(1);
                dto.getTypeInfo().setLegacySubType(legacySubType);
                dto.getTypeInfo().setLegacySubTypeDesc(resolvePaperTypeDesc(legacySubType));
                dto.getTypeInfo().setCategoryId(categoryId);
            }
        }
        if (categoryIds.isEmpty()) {
            return;
        }
        List<LabAchievementCategoryEntity> categories = categoryService.listByIds(new java.util.ArrayList<>(categoryIds));
        Map<Long, LabAchievementCategoryEntity> catMap = categories.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(LabAchievementCategoryEntity::getId, c -> c));
        Set<Long> parentIds = categories.stream()
            .map(LabAchievementCategoryEntity::getParentId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, LabAchievementCategoryEntity> parentMap = parentIds.isEmpty()
            ? Collections.emptyMap()
            : categoryService.listByIds(new java.util.ArrayList<>(parentIds)).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(LabAchievementCategoryEntity::getId, c -> c));
        for (LabPaperDTO dto : dtoList) {
            Long categoryId = dto.getCategoryId();
            if (categoryId == null) {
                continue;
            }
            LabAchievementCategoryEntity current = catMap.get(categoryId);
            if (current == null) {
                continue;
            }
            dto.setCategoryName(current.getCategoryName());
            LabAchievementCategoryEntity parent = current.getParentId() != null ? parentMap.get(current.getParentId()) : null;
            String fullPath;
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

    @Transactional(rollbackFor = Exception.class)
    public Long createPaper(CreatePaperCommand command, Long ownerUserId) {
        LabPaperEntity entity = new LabPaperEntity();
        fillPaperEntity(entity, command, ownerUserId);
        entity.setCreateTime(new java.util.Date());
        entity.setUpdateTime(new java.util.Date());
        paperService.save(entity);
        Long paperId = entity.getId();
        saveAuthors(paperId, command.getAuthors(), ownerUserId);
        saveFunds(paperId, command.getFunds());
        saveProjectRelations(paperId, command.getProjectIds(), ownerUserId);
        return paperId;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePaper(Long paperId, UpdatePaperCommand command, Long operatorId, boolean isAdmin) {
        LabPaperEntity entity = paperService.getById(paperId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "论文");
        }
        if (!canOperatePaper(entity, operatorId, isAdmin)) {
            throw new ApiException(ErrorCode.Business.PERMISSION_NOT_ALLOWED_TO_OPERATE);
        }
        fillPaperEntity(entity, command, entity.getOwnerUserId());
        entity.setUpdateTime(new java.util.Date());
        paperService.updateById(entity);
        saveAuthors(paperId, command.getAuthors(), operatorId);
        saveFunds(paperId, command.getFunds());
        saveProjectRelations(paperId, command.getProjectIds(), operatorId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePaperFunds(Long paperId, List<CreatePaperCommand.PaperFundCommand> funds,
                                 Long operatorId, boolean isAdmin) {
        LabPaperEntity entity = paperService.getById(paperId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "论文");
        }
        if (!canOperatePaper(entity, operatorId, isAdmin)) {
            throw new ApiException(ErrorCode.Business.PERMISSION_NOT_ALLOWED_TO_OPERATE);
        }
        saveFunds(paperId, funds);
        paperService.lambdaUpdate()
            .eq(LabPaperEntity::getId, paperId)
            .set(LabPaperEntity::getUpdateTime, new java.util.Date())
            .update();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePaper(Long paperId, Long operatorId, boolean isAdmin) {
        LabPaperEntity entity = paperService.getById(paperId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
            return;
        }
        if (!canOperatePaper(entity, operatorId, isAdmin)) {
            throw new ApiException(ErrorCode.Business.PERMISSION_NOT_ALLOWED_TO_OPERATE);
        }
        paperService.removeById(paperId);
        paperAuthorService.lambdaUpdate()
            .eq(LabPaperAuthorEntity::getPaperId, paperId)
            .remove();
        fundPaperRelService.lambdaUpdate()
            .eq(LabFundPaperRelEntity::getPaperId, paperId)
            .remove();
        projectPaperRelService.hardDeleteByPaperId(paperId);
    }

    private boolean canOperatePaper(LabPaperEntity entity, Long operatorId, boolean isAdmin) {
        if (isAdmin) {
            return true;
        }
        if (operatorId == null) {
            return false;
        }
        if (entity.getOwnerUserId() != null && entity.getOwnerUserId().equals(operatorId)) {
            return true;
        }
        return paperAuthorService.isAuthor(entity.getId(), operatorId);
    }
}
