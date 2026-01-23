package com.agileboot.domain.lab.paper.author;

import com.agileboot.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 论文作者实体（内外部作者统一）
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("lab_paper_author")
@Schema(description = "论文作者")
public class LabPaperAuthorEntity extends BaseEntity<LabPaperAuthorEntity> {

    @Schema(description = "作者ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "论文ID")
    @TableField("paper_id")
    private Long paperId;

    @Schema(description = "内部作者user_id；NULL为外部作者")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "作者姓名（内部作者可冗余）")
    @TableField("name")
    private String name;

    @Schema(description = "英文姓名")
    @TableField("name_en")
    private String nameEn;

    @Schema(description = "邮箱（用于确认作者身份）")
    @TableField("email")
    private String email;

    @Schema(description = "单位/机构")
    @TableField("affiliation")
    private String affiliation;

    @Schema(description = "作者顺序（>0）")
    @TableField("author_order")
    private Integer authorOrder;

    @Schema(description = "是否通讯作者")
    @TableField("is_corresponding")
    private Boolean isCorresponding;

    @Schema(description = "作者角色/贡献")
    @TableField("role")
    private String role;

    @Schema(description = "仅对内部作者生效：是否在该用户个人页可见")
    @TableField("visible")
    private Boolean visible;

    @Schema(description = "是否删除")
    @TableField("deleted")
    private Boolean deleted;

    @Override
    public Serializable pkVal() {
        return this.id;
    }

    public boolean isInternal() {
        return userId != null;
    }

    public boolean isExternal() {
        return userId == null;
    }
}
