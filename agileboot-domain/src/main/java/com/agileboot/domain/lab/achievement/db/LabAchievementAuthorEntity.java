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

/**
 * 成果作者实体（内外部作者统一）
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("lab_achievement_author")
@Schema(description = "成果作者")
public class LabAchievementAuthorEntity extends BaseEntity<LabAchievementAuthorEntity> {

    @Schema(description = "作者ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "成果ID")
    @TableField("achievement_id")
    private Long achievementId;

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
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;

    @Override
    public Serializable pkVal() {
        return this.id;
    }

    /**
     * 是否为内部作者
     */
    public boolean isInternal() {
        return userId != null;
    }

    /**
     * 是否为外部作者
     */
    public boolean isExternal() {
        return userId == null;
    }
}
