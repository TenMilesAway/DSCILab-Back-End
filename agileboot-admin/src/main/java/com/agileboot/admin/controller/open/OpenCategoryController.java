package com.agileboot.admin.controller.open;

import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.lab.category.LabAchievementCategoryApplicationService;
import com.agileboot.domain.lab.category.dto.LabAchievementCategoryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 公开的成果分类接口（无需登录）
 */
@Tag(name = "开放接口 - 成果分类", description = "对外展示用公开分类API（无需登录）")
@RestController
@RequestMapping("/open/achievement-categories")
@RequiredArgsConstructor
public class OpenCategoryController extends BaseController {

    private final LabAchievementCategoryApplicationService categoryApplicationService;

    @Operation(summary = "分类树", description = "获取启用的成果分类树（默认仅启用项）。支持仅返回二级分类列表以供前端筛选使用。")
    @GetMapping
    public ResponseEntity<ResponseDTO<List<LabAchievementCategoryDTO>>> getCategoryTree(
            @Parameter(description = "是否包含未启用分类") @RequestParam(defaultValue = "false") boolean includeInactive,
            @Parameter(description = "是否仅返回二级(叶子)分类列表") @RequestParam(defaultValue = "false") boolean onlyLeaf
    ) {
        List<LabAchievementCategoryDTO> tree = categoryApplicationService.getCategoryTree(includeInactive);
        List<LabAchievementCategoryDTO> bodyData = onlyLeaf ? extractSecondLevel(tree) : tree;
        ResponseDTO<List<LabAchievementCategoryDTO>> body = ResponseDTO.ok(bodyData);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(120, TimeUnit.SECONDS).cachePublic())
                .body(body);
    }

    @Operation(summary = "获取某一级分类下的二级分类", description = "根据parentId返回其直接子分类列表（通常为二级分类）")
    @GetMapping("/children")
    public ResponseEntity<ResponseDTO<List<LabAchievementCategoryDTO>>> getChildren(
            @Parameter(description = "父分类ID", required = true) @RequestParam Long parentId,
            @Parameter(description = "是否包含未启用分类") @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        // 复用树接口，避免直接依赖底层Service
        List<LabAchievementCategoryDTO> tree = categoryApplicationService.getCategoryTree(includeInactive);
        List<LabAchievementCategoryDTO> children = findChildren(tree, parentId);
        ResponseDTO<List<LabAchievementCategoryDTO>> body = ResponseDTO.ok(children);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(120, TimeUnit.SECONDS).cachePublic())
                .body(body);
    }

    private List<LabAchievementCategoryDTO> extractSecondLevel(List<LabAchievementCategoryDTO> tree) {
        List<LabAchievementCategoryDTO> result = new ArrayList<>();
        if (tree == null) return result;
        for (LabAchievementCategoryDTO top : tree) {
            if (top.getChildren() != null) {
                result.addAll(top.getChildren());
            }
        }
        return result;
    }

    private List<LabAchievementCategoryDTO> findChildren(List<LabAchievementCategoryDTO> tree, Long parentId) {
        if (tree == null || parentId == null) return new ArrayList<>();
        for (LabAchievementCategoryDTO top : tree) {
            if (parentId.equals(top.getId())) {
                return top.getChildren() != null ? top.getChildren() : new ArrayList<>();
            }
            if (top.getChildren() != null) {
                for (LabAchievementCategoryDTO child : top.getChildren()) {
                    if (parentId.equals(child.getId())) {
                        return child.getChildren() != null ? child.getChildren() : new ArrayList<>();
                    }
                }
            }
        }
        return new ArrayList<>();
    }
}

