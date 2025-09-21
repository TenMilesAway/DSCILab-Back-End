package com.agileboot.admin.controller.lab;

import com.agileboot.admin.customize.aop.accessLog.AccessLog;
import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.lab.category.CategoryCompatibilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 成果类型兼容性控制器
 * 用于处理新旧类型系统的兼容性和数据迁移
 *
 * @author agileboot
 */
@Tag(name = "成果类型兼容性API", description = "新旧类型系统兼容性接口")
@RestController
@RequestMapping("/lab/achievement-categories/compatibility")
@RequiredArgsConstructor
public class LabAchievementCategoryCompatibilityController extends BaseController {

    private final CategoryCompatibilityService compatibilityService;

    /**
     * 获取新旧类型映射关系
     */
    @Operation(summary = "获取新旧类型映射关系", description = "获取新旧类型系统的映射关系")
    @GetMapping("/mapping")
    @PreAuthorize("@permission.has('lab:category:list') OR @labUserPermission.isAdmin()")
    public ResponseDTO<Map<String, Object>> getTypeMappings() {
        Map<String, Object> mappings = compatibilityService.getTypeMappings();
        return ResponseDTO.ok(mappings);
    }

    /**
     * 检查数据迁移状态
     */
    @Operation(summary = "检查数据迁移状态", description = "检查数据迁移的完成状态")
    @GetMapping("/migrate")
    @PreAuthorize("@permission.has('lab:category:list') OR @labUserPermission.isAdmin()")
    public ResponseDTO<Map<String, Object>> checkMigrationStatus() {
        Map<String, Object> status = compatibilityService.checkMigrationStatus();
        return ResponseDTO.ok(status);
    }

    /**
     * 根据旧类型获取新类型ID
     */
    @Operation(summary = "根据旧类型获取新类型ID", description = "根据旧的type和subType获取新的categoryId")
    @GetMapping("/legacy-to-new")
    @PreAuthorize("@permission.has('lab:category:list') OR @labUserPermission.isAdmin()")
    public ResponseDTO<Long> getCategoryIdByLegacyType(
            @Parameter(description = "成果类型（1=论文，2=项目）") @RequestParam Integer type,
            @Parameter(description = "子类型") @RequestParam(required = false) Integer subType) {
        Long categoryId = compatibilityService.getCategoryIdByLegacyType(type, subType);
        return ResponseDTO.ok(categoryId);
    }

    /**
     * 根据新类型ID获取旧类型
     */
    @Operation(summary = "根据新类型ID获取旧类型", description = "根据categoryId获取旧的type和subType")
    @GetMapping("/new-to-legacy")
    @PreAuthorize("@permission.has('lab:category:list') OR @labUserPermission.isAdmin()")
    public ResponseDTO<CategoryCompatibilityService.LegacyTypeMapping> getLegacyTypeByCategory(
            @Parameter(description = "新的类型ID") @RequestParam Long categoryId) {
        CategoryCompatibilityService.LegacyTypeMapping mapping = compatibilityService.getLegacyTypeByCategory(categoryId);
        return ResponseDTO.ok(mapping);
    }
}
