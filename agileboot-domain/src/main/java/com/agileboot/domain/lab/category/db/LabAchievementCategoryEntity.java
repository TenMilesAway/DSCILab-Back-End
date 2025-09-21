package com.agileboot.domain.lab.category.db;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 实验室成果类型实体
 *
 * @author agileboot
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("lab_achievement_category")
public class LabAchievementCategoryEntity {

    /**
     * 类型ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父类型ID，NULL表示一级类型
     */
    private Long parentId;

    /**
     * 类型编码（用于兼容现有逻辑）
     */
    private String categoryCode;

    /**
     * 类型名称
     */
    private String categoryName;

    /**
     * 英文名称
     */
    private String categoryNameEn;

    /**
     * 类型描述
     */
    private String description;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 是否系统内置类型（不可删除）
     */
    private Boolean isSystem;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 图标
     */
    private String icon;

    /**
     * 颜色标识
     */
    private String color;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新者ID
     */
    private Long updaterId;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Boolean deleted;

    /**
     * 子类型列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<LabAchievementCategoryEntity> children;

    /**
     * 父类型名称（非数据库字段）
     */
    @TableField(exist = false)
    private String parentName;

    /**
     * 是否为一级类型
     */
    public boolean isTopLevel() {
        return parentId == null;
    }

    /**
     * 是否为二级类型
     */
    public boolean isSecondLevel() {
        return parentId != null;
    }

    /**
     * 是否可以删除
     */
    public boolean canDelete() {
        // 系统内置类型不可删除
        return !Boolean.TRUE.equals(isSystem);
    }

    /**
     * 是否可以编辑
     */
    public boolean canEdit() {
        // 所有类型都可以编辑（包括系统内置类型的名称和描述）
        return true;
    }
}
