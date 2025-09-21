package com.agileboot.domain.lab.category.dto;

import com.agileboot.domain.lab.category.db.LabAchievementCategoryEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 实验室成果类型DTO
 *
 * @author agileboot
 */
@Data
@Schema(description = "成果类型信息")
public class LabAchievementCategoryDTO {

    @Schema(description = "类型ID")
    private Long id;

    @Schema(description = "父类型ID")
    private Long parentId;

    @Schema(description = "类型编码")
    private String categoryCode;

    @Schema(description = "类型名称")
    private String categoryName;

    @Schema(description = "英文名称")
    private String categoryNameEn;

    @Schema(description = "类型描述")
    private String description;

    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @Schema(description = "是否系统内置类型")
    private Boolean isSystem;

    @Schema(description = "是否启用")
    private Boolean isActive;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "颜色标识")
    private String color;

    @Schema(description = "父类型名称")
    private String parentName;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "子类型列表")
    private List<LabAchievementCategoryDTO> children;

    @Schema(description = "是否可以删除")
    private Boolean canDelete;

    @Schema(description = "是否可以编辑")
    private Boolean canEdit;

    @Schema(description = "是否有子类型")
    private Boolean hasChildren;

    @Schema(description = "是否被成果使用")
    private Boolean isUsed;

    /**
     * 从实体转换为DTO
     */
    public LabAchievementCategoryDTO(LabAchievementCategoryEntity entity) {
        if (entity == null) {
            return;
        }

        this.id = entity.getId();
        this.parentId = entity.getParentId();
        this.categoryCode = entity.getCategoryCode();
        this.categoryName = entity.getCategoryName();
        this.categoryNameEn = entity.getCategoryNameEn();
        this.description = entity.getDescription();
        this.sortOrder = entity.getSortOrder();
        this.isSystem = entity.getIsSystem();
        this.isActive = entity.getIsActive();
        this.icon = entity.getIcon();
        this.color = entity.getColor();
        this.parentName = entity.getParentName();
        this.createTime = entity.getCreateTime();
        this.updateTime = entity.getUpdateTime();
        this.canDelete = entity.canDelete();
        this.canEdit = entity.canEdit();

        // 转换子类型
        if (entity.getChildren() != null) {
            this.children = entity.getChildren().stream()
                    .map(LabAchievementCategoryDTO::new)
                    .collect(Collectors.toList());
        }

        // 设置是否有子类型
        this.hasChildren = entity.getChildren() != null && !entity.getChildren().isEmpty();
    }

    /**
     * 默认构造函数
     */
    public LabAchievementCategoryDTO() {
    }

    /**
     * 获取层级深度
     */
    public int getLevel() {
        return parentId == null ? 1 : 2;
    }

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
     * 获取完整路径名称
     */
    public String getFullPath() {
        if (parentName != null) {
            return parentName + " > " + categoryName;
        }
        return categoryName;
    }
}
