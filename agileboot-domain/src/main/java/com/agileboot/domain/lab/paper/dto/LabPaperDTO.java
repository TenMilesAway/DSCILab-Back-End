package com.agileboot.domain.lab.paper.dto;

import com.agileboot.domain.lab.achievement.dto.LabFundAssociationDTO;
import com.agileboot.domain.lab.common.dto.RelatedProjectDTO;
import com.agileboot.domain.lab.common.dto.AchievementTypeInfoDTO;
import com.agileboot.domain.lab.paper.db.LabPaperEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "论文详情")
public class LabPaperDTO {

    private Long id;
    private String title;
    private String titleEn;
    private String description;
    private String keywords;
    private Integer paperTypeId;
    private String publication;
    private String conferenceName;
    private String issueNumber;
    private String publicationPlace;
    private String publisher;
    private String pageRange;
    private LocalDate publishDate;
    private String publishDateDisplay;
    private Integer publishDatePrecision;
    private String reference;
    private String doi;
    private String linkUrl;
    private String extraUrl;
    private String gitUrl;
    private String homepageUrl;
    private String pdfUrl;
    private String notes;
    private Long ownerUserId;
    private Boolean published;
    private Boolean verified;
    private String extra;
    private Date createTime;
    private Date updateTime;
    private List<LabPaperAuthorDTO> authors = Collections.emptyList();
    private List<LabFundAssociationDTO> fundAssociations = Collections.emptyList();
    private List<RelatedProjectDTO> relatedProjects = Collections.emptyList();
    private List<Long> projectIds = Collections.emptyList();
    private Long categoryId;
    private String categoryName;
    private String categoryFullPath;
    private AchievementTypeInfoDTO typeInfo = new AchievementTypeInfoDTO();

    public static LabPaperDTO fromEntity(LabPaperEntity entity) {
        LabPaperDTO dto = new LabPaperDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setTitleEn(entity.getTitleEn());
        dto.setDescription(entity.getDescription());
        dto.setKeywords(entity.getKeywords());
        dto.setPaperTypeId(entity.getPaperTypeId());
        dto.setPublication(entity.getPublication());
        dto.setConferenceName(entity.getConferenceName());
        dto.setIssueNumber(entity.getIssueNumber());
        dto.setPublicationPlace(entity.getPublicationPlace());
        dto.setPublisher(entity.getPublisher());
        dto.setPageRange(entity.getPageRange());
        dto.setPublishDate(entity.getPublishDate());
        dto.setPublishDateDisplay(entity.getPublishDateDisplay());
        dto.setPublishDatePrecision(entity.getPublishDatePrecision());
        dto.setReference(entity.getReference());
        dto.setDoi(entity.getDoi());
        dto.setLinkUrl(entity.getLinkUrl());
        dto.setExtraUrl(entity.getExtraUrl());
        dto.setGitUrl(entity.getGitUrl());
        dto.setHomepageUrl(entity.getHomepageUrl());
        dto.setPdfUrl(entity.getPdfUrl());
        dto.setNotes(entity.getNotes());
        dto.setOwnerUserId(entity.getOwnerUserId());
        dto.setPublished(entity.getPublished());
        dto.setVerified(entity.getIsVerified());
        dto.setExtra(entity.getExtra());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        dto.setCategoryId(entity.getCategoryId());
        if (dto.getTypeInfo() != null) {
            dto.getTypeInfo().setLegacyType(1);
            dto.getTypeInfo().setLegacySubType(entity.getPaperTypeId());
            dto.getTypeInfo().setCategoryId(entity.getCategoryId());
        }
        if (dto.getTypeInfo() != null) {
            dto.getTypeInfo().setLegacySubTypeDesc(null);
            dto.getTypeInfo().setCategoryName(null);
            dto.getTypeInfo().setCategoryFullPath(null);
        }
        return dto;
    }
}
