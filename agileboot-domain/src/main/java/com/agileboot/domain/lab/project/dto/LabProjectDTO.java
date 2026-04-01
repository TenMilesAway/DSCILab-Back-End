package com.agileboot.domain.lab.project.dto;

import com.agileboot.domain.lab.project.db.LabProjectEntity;
import com.agileboot.domain.lab.paper.dto.LabPaperAuthorDTO;
import com.agileboot.domain.lab.common.dto.AchievementTypeInfoDTO;
import com.agileboot.domain.lab.common.dto.RelatedPaperDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "项目详情")
public class LabProjectDTO {

    private Long id;
    private String title;
    private String titleEn;
    private String description;
    private String keywords;
    private Integer projectTypeId;
    private String projectNumber;
    private String supporter;
    private LocalDate projectStartDate;
    private LocalDate projectEndDate;
    private BigDecimal fundingAmount;
    private String amountDisplay;
    private String supportCn;
    private String supportEn;
    private String requirement;
    private String descText;
    private String reference;
    private String linkUrl;
    private String gitUrl;
    private String homepageUrl;
    private String pdfUrl;
    private Long categoryId;
    private String categoryName;
    private String categoryFullPath;
    private Long ownerUserId;
    private Boolean published;
    private Boolean verified;
    private String extra;
    private Date createTime;
    private Date updateTime;
    private List<LabPaperAuthorDTO> authors = Collections.emptyList();
    private AchievementTypeInfoDTO typeInfo = new AchievementTypeInfoDTO();
    private List<RelatedPaperDTO> relatedPapers = Collections.emptyList();

    public static LabProjectDTO fromEntity(LabProjectEntity entity) {
        LabProjectDTO dto = new LabProjectDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setTitleEn(entity.getTitleEn());
        dto.setDescription(entity.getDescription());
        dto.setKeywords(entity.getKeywords());
        dto.setProjectTypeId(entity.getProjectTypeId());
        dto.setProjectNumber(entity.getProjectNumber());
        dto.setSupporter(entity.getSupporter());
        dto.setProjectStartDate(entity.getProjectStartDate());
        dto.setProjectEndDate(entity.getProjectEndDate());
        dto.setFundingAmount(entity.getFundingAmount());
        dto.setAmountDisplay(entity.getAmountDisplay());
        dto.setSupportCn(entity.getSupportCn());
        dto.setSupportEn(entity.getSupportEn());
        dto.setRequirement(entity.getRequirement());
        dto.setDescText(entity.getDescText());
        dto.setReference(entity.getReference());
        dto.setLinkUrl(entity.getLinkUrl());
        dto.setGitUrl(entity.getGitUrl());
        dto.setHomepageUrl(entity.getHomepageUrl());
        dto.setPdfUrl(entity.getPdfUrl());
        dto.setCategoryId(entity.getCategoryId());
        dto.getTypeInfo().setLegacyType(2);
        dto.getTypeInfo().setLegacySubType(entity.getProjectTypeId());
        dto.getTypeInfo().setCategoryId(entity.getCategoryId());
        dto.setOwnerUserId(entity.getOwnerUserId());
        dto.setPublished(entity.getPublished());
        dto.setVerified(entity.getIsVerified());
        dto.setExtra(entity.getExtra());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        dto.getTypeInfo().setLegacySubTypeDesc(getProjectTypeDesc(entity.getProjectTypeId()));
        dto.setAuthors(java.util.Collections.emptyList());
        return dto;
    }

    private static String getProjectTypeDesc(Integer projectTypeId) {
        if (projectTypeId == null) {
            return null;
        }
        for (com.agileboot.domain.lab.achievement.db.LabAchievementEntity.ProjectType type
            : com.agileboot.domain.lab.achievement.db.LabAchievementEntity.ProjectType.values()) {
            if (type.getCode().equals(projectTypeId)) {
                return type.getDesc();
            }
        }
        return null;
    }
}
