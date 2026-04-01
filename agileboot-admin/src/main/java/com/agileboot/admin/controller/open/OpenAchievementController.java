package com.agileboot.admin.controller.open;

import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.domain.lab.achievement.LabAchievementApplicationService;
import com.agileboot.domain.lab.achievement.dto.PublicAchievementDTO;
import com.agileboot.domain.lab.achievement.query.PublicAchievementQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 公开成果接口（无需登录）
 */
@Tag(name = "公开成果", description = "公开成果展示接口")
@RestController
@RequestMapping("/open/achievements")
@RequiredArgsConstructor
public class OpenAchievementController extends BaseController {

    private final LabAchievementApplicationService achievementApplicationService;

    @Operation(summary = "公开成果列表", description = "获取公开成果列表，返回所有成果，支持关键词、类型、分类、日期范围、作者姓名筛选")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(schema = @Schema(implementation = PageDTO.class)))
    })
    @GetMapping
    public ResponseDTO<PageDTO<PublicAchievementDTO>> list(
        @Parameter(description = "查询条件") PublicAchievementQuery query,
        @RequestParam(value = "parentCategoryId", required = false) Long parentCategoryId,
        @RequestParam(value = "categoryId", required = false) Long categoryId) {
        // 明确映射，避免个别场景下的绑定缺失
        if (parentCategoryId != null) {
            query.setParentCategoryId(parentCategoryId);
        }
        if (categoryId != null) {
            query.setCategoryId(categoryId);
        }
        PageDTO<PublicAchievementDTO> pageDTO = achievementApplicationService.getPublicAchievementList(query);
        return ResponseDTO.ok(pageDTO);
    }

    @Operation(summary = "公开成果详情", description = "获取公开成果详情，返回所有成果，包含过滤后的作者列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(schema = @Schema(implementation = PublicAchievementDTO.class))),
        @ApiResponse(responseCode = "404", description = "成果不存在")
    })
    @GetMapping("/{id}")
    public ResponseDTO<PublicAchievementDTO> getDetail(
        @Parameter(description = "成果ID", required = true) @PathVariable Long id) {
        PublicAchievementDTO dto = achievementApplicationService.getPublicAchievementDetail(id);
        return ResponseDTO.ok(dto);
    }


}
