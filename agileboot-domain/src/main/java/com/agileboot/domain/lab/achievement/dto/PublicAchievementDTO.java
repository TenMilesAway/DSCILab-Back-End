package com.agileboot.domain.lab.achievement.dto;

import com.agileboot.domain.lab.achievement.db.LabAchievementEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * 公开成果DTO（精简版，去除内部管理字段）
 */
@Data
@Schema(description = "公开成果DTO")
public class PublicAchievementDTO {

    @Schema(description = "成果ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "英文标题")
    private String titleEn;

    @Schema(description = "描述/摘要")
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

    @Schema(description = "发表场所/期刊/会议")
    private String venue;

    @Schema(description = "论文发表日期")
    private LocalDate publishDate;

    @Schema(description = "项目开始日期")
    private LocalDate projectStartDate;

    @Schema(description = "项目结束日期")
    private LocalDate projectEndDate;

    @Schema(description = "封面图片URL")
    private String coverUrl;

    @Schema(description = "链接URL")
    private String linkUrl;

    @Schema(description = "Git仓库URL")
    private String gitUrl;

    @Schema(description = "项目主页URL")
    private String homepageUrl;

    @Schema(description = "PDF文件URL")
    private String pdfUrl;

    @Schema(description = "DOI")
    private String doi;

    @Schema(description = "项目经费（万元）")
    private BigDecimal fundingAmount;

    @Schema(description = "是否对外发布")
    private Boolean published;

    @Schema(description = "作者列表")
    private List<PublicAuthorDTO> authors;

    @Schema(description = "扩展信息")
    private String extra;

    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 从实体转换为公开DTO
     */
    public static PublicAchievementDTO fromEntity(LabAchievementEntity entity) {
        if (entity == null) {
            return null;
        }

        PublicAchievementDTO dto = new PublicAchievementDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setTitleEn(entity.getTitleEn());
        dto.setDescription(entity.getDescription());
        dto.setKeywords(entity.getKeywords());
        dto.setType(entity.getType());
        dto.setPaperType(entity.getPaperType());
        dto.setProjectType(entity.getProjectType());
        dto.setVenue(entity.getVenue());
        dto.setPublishDate(entity.getPublishDate());
        dto.setProjectStartDate(entity.getProjectStartDate());
        dto.setProjectEndDate(entity.getProjectEndDate());
        dto.setCoverUrl(entity.getCoverUrl());
        dto.setLinkUrl(entity.getLinkUrl());
        dto.setGitUrl(entity.getGitUrl());
        dto.setHomepageUrl(entity.getHomepageUrl());
        dto.setPdfUrl(entity.getPdfUrl());
        dto.setDoi(entity.getDoi());
        dto.setFundingAmount(entity.getFundingAmount());
        dto.setPublished(entity.getPublished());
        dto.setExtra(entity.getExtra());
        dto.setCreateTime(entity.getCreateTime());

        // 设置类型描述
        if (entity.getType() != null) {
            dto.setTypeDesc(entity.getType() == 1 ? "论文" : "项目");
        }
        if (entity.getPaperType() != null) {
            dto.setPaperTypeDesc(getPaperTypeDesc(entity.getPaperType()));
        }
        if (entity.getProjectType() != null) {
            dto.setProjectTypeDesc(getProjectTypeDesc(entity.getProjectType()));
        }

        return dto;
    }

    private static String getPaperTypeDesc(Integer paperType) {
        switch (paperType) {
            case 1: return "期刊论文";
            case 2: return "会议论文";
            case 3: return "专利";
            case 4: return "软件著作权";
            case 5: return "标准";
            case 6: return "报告";
            case 7: return "其他";
            default: return "未知";
        }
    }

    private static String getProjectTypeDesc(Integer projectType) {
        switch (projectType) {
            case 1: return "国家级项目";
            case 2: return "省部级项目";
            case 3: return "市厅级项目";
            case 4: return "企业合作项目";
            case 5: return "国际合作项目";
            case 6: return "校内项目";
            case 7: return "其他纵向项目";
            case 8: return "其他横向项目";
            default: return "未知";
        }
    }
}
