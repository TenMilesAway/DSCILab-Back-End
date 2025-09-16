package com.agileboot.domain.lab.achievement.db;

import com.agileboot.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 实验室成果实体
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("lab_achievement")
@Schema(description = "实验室成果")
public class LabAchievementEntity extends BaseEntity<LabAchievementEntity> {

    @Schema(description = "成果ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "成果标题")
    @TableField("title")
    private String title;

    @Schema(description = "英文标题")
    @TableField("title_en")
    private String titleEn;

    @Schema(description = "摘要/描述")
    @TableField("description")
    private String description;

    @Schema(description = "关键词")
    @TableField("keywords")
    private String keywords;

    @Schema(description = "成果类型：1=论文,2=项目")
    @TableField("type")
    private Integer type;

    @Schema(description = "论文类型：1=期刊论文,2=会议论文,3=书籍章节,4=专利,5=标准,6=研究报告,7=其他论文")
    @TableField("paper_type")
    private Integer paperType;

    @Schema(description = "项目类型：1=国家重点项目,2=国家一般项目,3=省部级项目,4=企业合作项目,5=国际合作项目,6=青年基金,7=博士后基金,8=其他项目")
    @TableField("project_type")
    private Integer projectType;

    @Schema(description = "期刊/会议/发布渠道")
    @TableField("venue")
    private String venue;

    @Schema(description = "发表/公开日期（论文用）")
    @TableField("publish_date")
    private LocalDate publishDate;

    @Schema(description = "项目开始日期（项目用）")
    @TableField("project_start_date")
    private LocalDate projectStartDate;

    @Schema(description = "项目结束日期（项目用，可空=进行中）")
    @TableField("project_end_date")
    private LocalDate projectEndDate;

    @Schema(description = "参考文献/引用信息")
    @TableField("reference")
    private String reference;

    @Schema(description = "外部链接URL")
    @TableField("link_url")
    private String linkUrl;

    @Schema(description = "Git 仓库地址")
    @TableField("git_url")
    private String gitUrl;

    @Schema(description = "主页/展示页地址")
    @TableField("homepage_url")
    private String homepageUrl;

    @Schema(description = "PDF 下载地址")
    @TableField("pdf_url")
    private String pdfUrl;

    @Schema(description = "论文 DOI")
    @TableField("doi")
    private String doi;

    @Schema(description = "项目经费（万元）")
    @TableField("funding_amount")
    private BigDecimal fundingAmount;

    @Schema(description = "成果所有者（创建者）")
    @TableField("owner_user_id")
    private Long ownerUserId;

    @Schema(description = "是否对外发布")
    @TableField("published")
    private Boolean published;

    @Schema(description = "是否已审核")
    @TableField("is_verified")
    private Boolean isVerified;

    @Schema(description = "扩展信息（JSON）")
    @TableField("extra")
    private String extra;

    @Schema(description = "是否删除")
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;




    @Override
    public Serializable pkVal() {
        return this.id;
    }

    /**
     * 成果类型枚举
     */
    public enum AchievementType {
        PAPER(1, "论文"),
        PROJECT(2, "项目");

        private final Integer code;
        private final String desc;

        AchievementType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 论文类型枚举
     */
    public enum PaperType {
        JOURNAL_PAPER(1, "期刊论文"),
        CONFERENCE_PAPER(2, "会议论文"),
        BOOK_CHAPTER(3, "书籍章节"),
        PATENT(4, "专利"),
        STANDARD(5, "标准"),
        REPORT(6, "研究报告"),
        OTHER_PAPER(7, "其他论文");

        private final Integer code;
        private final String desc;

        PaperType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 项目类型枚举
     */
    public enum ProjectType {
        NATIONAL_KEY(1, "国家重点项目"),
        NATIONAL_GENERAL(2, "国家一般项目"),
        PROVINCIAL(3, "省部级项目"),
        ENTERPRISE_COOPERATION(4, "企业合作项目"),
        INTERNATIONAL_COOPERATION(5, "国际合作项目"),
        YOUTH_FUND(6, "青年基金"),
        POSTDOC_FUND(7, "博士后基金"),
        OTHER_PROJECT(8, "其他项目");

        private final Integer code;
        private final String desc;

        ProjectType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}

