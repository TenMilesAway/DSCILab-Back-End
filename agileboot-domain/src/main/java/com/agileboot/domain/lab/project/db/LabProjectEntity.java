package com.agileboot.domain.lab.project.db;

import com.agileboot.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
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
import java.util.Date;

/**
 * 实验室项目实体
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("lab_achievement_project")
@Schema(description = "实验室项目")
public class LabProjectEntity extends BaseEntity<LabProjectEntity> {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "项目ID")
    private Long id;

    @TableField("title")
    @Schema(description = "项目名称")
    private String title;

    @TableField(value = "title_en", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "英文名称")
    private String titleEn;

    @TableField(value = "description", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "摘要/描述")
    private String description;

    @TableField(value = "keywords", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "关键词")
    private String keywords;

    @TableField(value = "project_type_id", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "项目类型（lab_type.FUNDTYPE）")
    private Integer projectTypeId;

    @TableField(value = "category_id", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "成果分类ID（lab_achievement_category）")
    private Long categoryId;

    @TableField(value = "project_number", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "项目号")
    private String projectNumber;

    @TableField(value = "supporter", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "资助单位/主办方")
    private String supporter;

    @TableField(value = "project_start_date", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "项目开始日期")
    private LocalDate projectStartDate;

    @TableField(value = "project_end_date", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "项目结束日期（可空=进行中）")
    private LocalDate projectEndDate;

    @TableField(value = "funding_amount", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "项目经费（万元）")
    private BigDecimal fundingAmount;

    @TableField(value = "amount_display", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "金额展示字符串")
    private String amountDisplay;

    @TableField(value = "support_cn", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "资助说明（中文）")
    private String supportCn;

    @TableField(value = "support_en", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "资助说明（英文）")
    private String supportEn;

    @TableField(value = "requirement", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "结项成果要求")
    private String requirement;

    @TableField(value = "desc_text", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "其它信息/备注")
    private String descText;

    @TableField(value = "reference", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "项目参考/简介")
    private String reference;

    @TableField(value = "link_url", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "外部链接URL")
    private String linkUrl;

    @TableField(value = "git_url", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "Git 仓库地址")
    private String gitUrl;

    @TableField(value = "homepage_url", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "主页/展示页地址")
    private String homepageUrl;

    @TableField(value = "pdf_url", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "项目文档/PDF")
    private String pdfUrl;

    @TableField("owner_user_id")
    @Schema(description = "成果所有者ID")
    private Long ownerUserId;

    @TableField("published")
    @Schema(description = "是否对外发布")
    private Boolean published;

    @TableField("is_verified")
    @Schema(description = "是否已审核")
    private Boolean isVerified;

    @TableField("extra")
    @Schema(description = "扩展信息（JSON）")
    private String extra;

    @TableField("creator_id")
    @Schema(description = "创建者ID")
    private Long creatorId;

    @TableField("updater_id")
    @Schema(description = "更新者ID")
    private Long updaterId;

    @TableLogic
    @TableField("deleted")
    @Schema(description = "是否删除")
    private Boolean deleted;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}
