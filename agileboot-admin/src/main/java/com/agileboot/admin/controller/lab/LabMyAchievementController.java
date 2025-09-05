package com.agileboot.admin.controller.lab;

import com.agileboot.admin.customize.aop.accessLog.AccessLog;
import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.lab.achievement.LabAchievementApplicationService;
import com.agileboot.domain.lab.achievement.dto.LabAchievementDTO;
import com.agileboot.domain.lab.achievement.query.MyAchievementQuery;
import com.agileboot.domain.lab.user.LabUserPermissionChecker;
import com.agileboot.domain.lab.user.db.LabUserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 我的成果管理（用户自助端）
 */
@Tag(name = "我的成果", description = "用户自助成果管理接口")
@RestController
@RequestMapping("/lab/my-achievements")
@RequiredArgsConstructor
public class LabMyAchievementController extends BaseController {

    private final LabAchievementApplicationService achievementApplicationService;
    private final LabUserPermissionChecker labUserPermission;

    private LabUserEntity getCurrentLabUser() {
        LabUserEntity current = labUserPermission.getCurrentLabUser();
        if (current == null) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION, "/lab/my-achievements");
        }
        return current;
    }

    @Operation(summary = "我的成果列表", description = "获取当前用户的成果列表，包括我拥有的成果和我参与的成果")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(schema = @Schema(implementation = PageDTO.class))),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping
    @PreAuthorize("@permission.has('lab:achievement:query')")
    @AccessLog(title = "我的成果")
    public ResponseDTO<PageDTO<LabAchievementDTO>> list(
        @Parameter(description = "查询条件") MyAchievementQuery query) {
        LabUserEntity current = getCurrentLabUser();
        PageDTO<LabAchievementDTO> pageDTO = achievementApplicationService.getMyAchievementList(query, current.getId());
        return ResponseDTO.ok(pageDTO);
    }

    @Operation(summary = "切换个人页可见性", description = "切换我在指定成果中的个人页可见性，仅作者本人可操作")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "操作成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足"),
        @ApiResponse(responseCode = "404", description = "成果不存在或您不是该成果的作者")
    })
    @PutMapping("/{achievementId}/visibility")
    @PreAuthorize("@permission.has('lab:achievement:edit')")
    @AccessLog(title = "我的成果")
    public ResponseDTO<Void> toggleVisibility(
        @Parameter(description = "成果ID", required = true) @PathVariable Long achievementId,
        @Parameter(description = "是否可见", required = true) @RequestParam Boolean visible) {
        LabUserEntity current = getCurrentLabUser();
        achievementApplicationService.toggleMyVisibilityInAchievement(achievementId, visible, current.getId());
        return ResponseDTO.ok();
    }
}
