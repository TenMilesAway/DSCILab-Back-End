package com.agileboot.domain.lab.achievement.db;

import com.agileboot.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 实验室成果表（对外展示用）
 */
@Getter
@Setter
@TableName("lab_achievement")
@Schema(description = "实验室成果信息表")
public class LabAchievementEntity extends BaseEntity<LabAchievementEntity> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "成果ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "成果类型：1=论文,2=专利,3=项目,4=基金,5=软著,6=奖项")
    @TableField("type")
    private Integer type;

    @Schema(description = "标题")
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

    @Schema(description = "期刊/会议/发布渠道")
    @TableField("journal")
    private String journal;

    @Schema(description = "发表/公开日期")
    @TableField("publish_date")
    private LocalDate publishDate;

    @Schema(description = "作者列表（逗号分隔简化存储）")
    @TableField("authors")
    private String authors;

    @Schema(description = "封面图URL")
    @TableField("cover_url")
    private String coverUrl;

    @Schema(description = "外部链接URL")
    @TableField("link_url")
    private String linkUrl;

    @Schema(description = "是否对外发布")
    @TableField("published")
    private Boolean published;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}

