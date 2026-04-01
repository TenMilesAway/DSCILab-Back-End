package com.agileboot.domain.lab.paper.db;

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
import java.time.LocalDate;
import java.util.Date;

/**
 * 实验室论文实体
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("lab_achievement_paper")
@Schema(description = "实验室论文")
public class LabPaperEntity extends BaseEntity<LabPaperEntity> {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "论文ID")
    private Long id;

    @TableField("title")
    @Schema(description = "论文标题")
    private String title;

    @TableField(value = "title_en", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "英文标题")
    private String titleEn;

    @TableField(value = "description", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "摘要/描述")
    private String description;

    @TableField(value = "keywords", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "关键词")
    private String keywords;

    @TableField(value = "paper_type_id", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "论文类型（lab_type.PAPERTYPE）")
    private Integer paperTypeId;

    @TableField(value = "category_id", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "成果分类ID（lab_achievement_category）")
    private Long categoryId;

    @TableField(value = "author_text", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "作者原始字符串")
    private String authorText;

    @TableField(value = "publication", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "期刊/会议/出版物名称")
    private String publication;

    @TableField(value = "conference_name", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "会议名称")
    private String conferenceName;

    @TableField(value = "issue_number", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "卷(期)/编号")
    private String issueNumber;

    @TableField(value = "publication_place", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "会议地点")
    private String publicationPlace;

    @TableField(value = "publisher", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "出版社/授权单位")
    private String publisher;

    @TableField(value = "page_range", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "页码")
    private String pageRange;

    @TableField(value = "publish_date", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "发表/公开日期")
    private LocalDate publishDate;

    @TableField(value = "publish_date_display", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "展示日期（年/年月/年月日）")
    private String publishDateDisplay;

    @TableField(value = "publish_date_precision", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "日期精度：1=年、2=年月、3=年月日")
    private Integer publishDatePrecision;

    @TableField(value = "reference", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "参考文献/引用信息")
    private String reference;

    @TableField(value = "doi", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "论文DOI")
    private String doi;

    @TableField(value = "link_url", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "外部链接URL")
    private String linkUrl;

    @TableField(value = "extra_url", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "其它链接")
    private String extraUrl;

    @TableField(value = "notes", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "其它信息/备注")
    private String notes;

    @TableField(value = "git_url", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "Git 仓库地址")
    private String gitUrl;

    @TableField(value = "homepage_url", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "主页/展示页地址")
    private String homepageUrl;

    @TableField(value = "pdf_url", updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "PDF 下载地址")
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
