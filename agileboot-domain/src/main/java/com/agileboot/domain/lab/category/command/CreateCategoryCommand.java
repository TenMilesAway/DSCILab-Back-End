package com.agileboot.domain.lab.category.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 创建成果类型命令
 *
 * @author agileboot
 */
@Data
@Schema(description = "创建成果类型命令")
public class CreateCategoryCommand {

    @Schema(description = "父类型ID，null表示一级类型")
    private Long parentId;

    @Size(max = 50, message = "类型编码长度不能超过50个字符")
    @Schema(description = "类型编码（唯一，可选；未提供则后端按名称自动生成）", required = false)
    private String categoryCode;

    @NotBlank(message = "类型名称不能为空")
    @Size(max = 100, message = "类型名称长度不能超过100个字符")
    @Schema(description = "类型名称", required = true)
    private String categoryName;

    @Size(max = 100, message = "英文名称长度不能超过100个字符")
    @Schema(description = "英文名称")
    private String categoryNameEn;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    @Schema(description = "类型描述")
    private String description;

    @Schema(description = "排序顺序，不传则自动分配")
    private Integer sortOrder;

    @Schema(description = "是否启用", example = "true")
    private Boolean isActive = true;

    @Size(max = 100, message = "图标长度不能超过100个字符")
    @Schema(description = "图标")
    private String icon;

    @Size(max = 20, message = "颜色标识长度不能超过20个字符")
    @Schema(description = "颜色标识", example = "#1890ff")
    private String color;

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
}
