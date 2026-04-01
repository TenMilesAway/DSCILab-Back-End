package com.agileboot.domain.lab.achievement.dto;

import com.agileboot.domain.lab.achievement.db.LabAchievementEntity;
import com.agileboot.domain.lab.common.dto.AchievementTypeInfoDTO;
import com.agileboot.domain.lab.paper.dto.LabPaperAuthorDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * 成果详情 DTO（后台使用）
 */
@Data
@Schema(description = "成果详情")
public class LabAchievementDTO {

    @Schema(description = "成果ID")
    private Long id;

    @Schema(description = "成果标题")
    private String title;

    @Schema(description = "英文标题")
    private String titleEn;

    @Schema(description = "摘要/描述")
    private String description;

    @Schema(description = "关键词")
    private String keywords;

    @Schema(description = "成果类型：1=论文,2=项目")
    private Integer type;

    @Schema(description = "成果类型描述")
    private String typeDesc;

    @Schema(description = "论文类型：1-7")
    private Integer paperType;

    @Schema(description = "论文类型描述")
    private String paperTypeDesc;

    @Schema(description = "项目类型：1-8")
    private Integer projectType;

    @Schema(description = "项目类型描述")
    private String projectTypeDesc;

    @Schema(description = "成果类型ID（新类型系统）")
    private Long categoryId;

    @Schema(description = "成果类型名称（新类型系统）")
    private String categoryName;

    @Schema(description = "成果类型完整路径（新类型系统）")
    private String categoryFullPath;

    @Schema(description = "统一的成果类型信息")
    private AchievementTypeInfoDTO typeInfo = new AchievementTypeInfoDTO();

    @Schema(description = "期刊/会议/发布渠道")
    private String venue;

    @Schema(description = "发表/公开日期（论文用）")
    private LocalDate publishDate;

    @Schema(description = "项目开始日期（项目用）")
    private LocalDate projectStartDate;

    @Schema(description = "项目结束日期（项目用）")
    private LocalDate projectEndDate;

    @Schema(description = "参考文献/引用信息")
    private String reference;

    @Schema(description = "外部链接URL")
    private String linkUrl;

    @Schema(description = "Git 仓库地址")
    private String gitUrl;

    @Schema(description = "主页/展示页地址")
    private String homepageUrl;

    @Schema(description = "PDF 下载地址")
    private String pdfUrl;

    @Schema(description = "论文 DOI")
    private String doi;

    @Schema(description = "项目经费（万元）")
    private BigDecimal fundingAmount;

    @Schema(description = "成果所有者ID")
    private Long ownerUserId;

    @Schema(description = "成果所有者姓名")
    private String ownerUserName;

    @Schema(description = "是否对外发布")
    private Boolean published;

    @Schema(description = "是否已审核")
    private Boolean isVerified;

    @Schema(description = "作者列表")
    private List<LabPaperAuthorDTO> authors = new java.util.ArrayList<>();

    @Schema(description = "关联基金ID列表")
    private List<Long> fundIds = new java.util.ArrayList<>();

    @Schema(description = "基金关联明细列表")
    private List<LabFundAssociationDTO> fundAssociations = new java.util.ArrayList<>();

    @Schema(description = "当前用户在该成果中的可见性状态（仅在我的成果列表中有效）")
    private Boolean myVisibility;

    @Schema(description = "扩展信息")
    private String extra;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    /**
     * 从实体转换
     */
    public static LabAchievementDTO fromEntity(LabAchievementEntity entity) {
        if (entity == null) {
            return null;
        }

        LabAchievementDTO dto = new LabAchievementDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setTitleEn(entity.getTitleEn());
        dto.setDescription(entity.getDescription());
        dto.setKeywords(entity.getKeywords());
        dto.setType(entity.getType());
        dto.setPaperType(entity.getPaperType());
        dto.setProjectType(entity.getProjectType());
        dto.setCategoryId(entity.getCategoryId());
        dto.getTypeInfo().setLegacyType(entity.getType());
        dto.getTypeInfo().setLegacySubType(
            entity.getType() != null && entity.getType() == 1 ? entity.getPaperType() : entity.getProjectType());
        dto.setVenue(entity.getVenue());
        dto.setPublishDate(entity.getPublishDate());
        dto.setProjectStartDate(entity.getProjectStartDate());
        dto.setProjectEndDate(entity.getProjectEndDate());
        dto.setReference(entity.getReference());
        dto.setLinkUrl(entity.getLinkUrl());
        dto.setGitUrl(entity.getGitUrl());
        dto.setHomepageUrl(entity.getHomepageUrl());
        dto.setPdfUrl(entity.getPdfUrl());
        dto.setDoi(entity.getDoi());
        dto.setFundingAmount(entity.getFundingAmount());
        dto.setOwnerUserId(entity.getOwnerUserId());
        dto.setPublished(entity.getPublished());
        dto.setIsVerified(entity.getIsVerified());
        dto.setExtra(entity.getExtra());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());

        // 设置类型描述
        if (entity.getType() != null) {
            for (LabAchievementEntity.AchievementType type : LabAchievementEntity.AchievementType.values()) {
                if (type.getCode().equals(entity.getType())) {
                    dto.setTypeDesc(type.getDesc());
                    break;
                }
            }
        }

        // 设置论文类型描述
        if (entity.getPaperType() != null) {
            for (LabAchievementEntity.PaperType paperType : LabAchievementEntity.PaperType.values()) {
                if (paperType.getCode().equals(entity.getPaperType())) {
                    dto.setPaperTypeDesc(paperType.getDesc());
                    dto.getTypeInfo().setLegacySubTypeDesc(paperType.getDesc());
                    break;
                }
            }
        }

        // 设置项目类型描述
        if (entity.getProjectType() != null) {
            for (LabAchievementEntity.ProjectType projectType : LabAchievementEntity.ProjectType.values()) {
                if (projectType.getCode().equals(entity.getProjectType())) {
                    dto.setProjectTypeDesc(projectType.getDesc());
                    dto.getTypeInfo().setLegacySubTypeDesc(projectType.getDesc());
                    break;
                }
            }
        }

        return dto;
    }
}
