package com.agileboot.admin.controller.lab;

import com.agileboot.admin.customize.aop.accessLog.AccessLog;
import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.enums.common.BusinessTypeEnum;
import com.agileboot.domain.lab.category.LabAchievementCategoryApplicationService;
import com.agileboot.domain.lab.category.command.BatchUpdateSortCommand;
import com.agileboot.domain.lab.category.command.CreateCategoryCommand;
import com.agileboot.domain.lab.category.command.UpdateCategoryCommand;
import com.agileboot.domain.lab.category.dto.LabAchievementCategoryDTO;
import com.agileboot.domain.lab.category.query.CategoryQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 实验室成果类型管理控制器
 *
 * @author agileboot
 */
@Tag(name = "成果类型管理API", description = "成果类型增删改查接口")
@RestController
@RequestMapping("/lab/achievement-categories")
@RequiredArgsConstructor
public class LabAchievementCategoryController extends BaseController {

    private final LabAchievementCategoryApplicationService categoryApplicationService;

    // ==================== 查询功能 ====================

    /**
     * 获取类型树形结构
     */
    @Operation(summary = "获取类型树形结构", description = "获取成果类型的树形结构，支持包含未启用的类型")
    @GetMapping
    @PreAuthorize("@permission.has('lab:category:list') OR @labUserPermission.isAdmin()")
    public ResponseDTO<List<LabAchievementCategoryDTO>> getCategoryTree(
            @Parameter(description = "是否包含未启用的类型") @RequestParam(defaultValue = "false") boolean includeInactive) {
        List<LabAchievementCategoryDTO> tree = categoryApplicationService.getCategoryTree(includeInactive);
        return ResponseDTO.ok(tree);
    }

    /**
     * 分页查询类型列表
     */
    @Operation(summary = "分页查询类型列表", description = "支持多条件筛选的分页查询")
    @GetMapping("/list")
    @PreAuthorize("@permission.has('lab:category:list') OR @labUserPermission.isAdmin()")
    public ResponseDTO<PageDTO<LabAchievementCategoryDTO>> getCategoryList(@Validated CategoryQuery query) {
        PageDTO<LabAchievementCategoryDTO> result = categoryApplicationService.getCategoryList(query);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取类型详情
     */
    @Operation(summary = "获取类型详情", description = "根据ID获取类型的详细信息")
    @GetMapping("/{id}")
    @PreAuthorize("@permission.has('lab:category:query') OR @labUserPermission.isAdmin()")
    public ResponseDTO<LabAchievementCategoryDTO> getCategoryById(
            @Parameter(description = "类型ID") @PathVariable Long id) {
        LabAchievementCategoryDTO category = categoryApplicationService.getCategoryById(id);
        return ResponseDTO.ok(category);
    }

    // ==================== 管理功能 ====================

    /**
     * 创建类型
     */
    @Operation(summary = "创建类型", description = "创建新的成果类型")
    @PostMapping
    @PreAuthorize("@permission.has('lab:category:add') OR @labUserPermission.isAdmin()")
    @AccessLog(title = "成果类型管理", businessType = BusinessTypeEnum.ADD)
    public ResponseDTO<Long> createCategory(@Validated @RequestBody CreateCategoryCommand command) {
        Long categoryId = categoryApplicationService.createCategory(command);
        return ResponseDTO.ok(categoryId);
    }

    /**
     * 更新类型
     */
    @Operation(summary = "更新类型", description = "更新成果类型信息")
    @PutMapping("/{id}")
    @PreAuthorize("@permission.has('lab:category:edit') OR @labUserPermission.isAdmin()")
    @AccessLog(title = "成果类型管理", businessType = BusinessTypeEnum.MODIFY)
    public ResponseDTO<Void> updateCategory(
            @Parameter(description = "类型ID") @PathVariable Long id,
            @Validated @RequestBody UpdateCategoryCommand command) {
        categoryApplicationService.updateCategory(id, command);
        return ResponseDTO.ok();
    }

    /**
     * 删除类型
     */
    @Operation(summary = "删除类型", description = "删除成果类型（软删除）")
    @DeleteMapping("/{id}")
    @PreAuthorize("@permission.has('lab:category:remove') OR @labUserPermission.isAdmin()")
    @AccessLog(title = "成果类型管理", businessType = BusinessTypeEnum.DELETE)
    public ResponseDTO<Void> deleteCategory(
            @Parameter(description = "类型ID") @PathVariable Long id) {
        categoryApplicationService.deleteCategory(id);
        return ResponseDTO.ok();
    }

    /**
     * 批量更新排序
     */
    @Operation(summary = "批量更新排序", description = "批量更新类型的排序顺序")
    @PutMapping("/batch/sort")
    @PreAuthorize("@permission.has('lab:category:sort') OR @labUserPermission.isAdmin()")
    @AccessLog(title = "成果类型管理", businessType = BusinessTypeEnum.MODIFY)
    public ResponseDTO<Void> batchUpdateSort(@Validated @RequestBody BatchUpdateSortCommand command) {
        categoryApplicationService.batchUpdateSort(command);
        return ResponseDTO.ok();
    }

    /**
     * 启用/禁用类型
     */
    @Operation(summary = "启用/禁用类型", description = "切换类型的启用状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("@permission.has('lab:category:edit') OR @labUserPermission.isAdmin()")
    @AccessLog(title = "成果类型管理", businessType = BusinessTypeEnum.MODIFY)
    public ResponseDTO<Void> updateCategoryStatus(
            @Parameter(description = "类型ID") @PathVariable Long id,
            @Parameter(description = "是否启用") @RequestParam boolean active) {
        categoryApplicationService.updateCategoryStatus(id, active);
        return ResponseDTO.ok();
    }
}
