package com.agileboot.admin.controller.lab;

import com.agileboot.admin.customize.aop.accessLog.AccessLog;
import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.lab.achievement.LabAchievementApplicationService;
import com.agileboot.domain.lab.achievement.command.CreateLabAchievementCommand;
import com.agileboot.domain.lab.achievement.command.CreateLabAchievementV2Command;
import com.agileboot.domain.lab.user.LabUserPermissionChecker;
import com.agileboot.domain.lab.user.db.LabUserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 成果管理 v2（仅叶子 categoryId，拒收 legacy 字段）
 */
@Tag(name = "成果管理 v2", description = "实验室成果管理接口 v2（仅 categoryId）")
@RestController
@RequestMapping("/v2/lab/achievements")
@RequiredArgsConstructor
public class LabAchievementV2Controller extends BaseController {

    private final LabAchievementApplicationService achievementApplicationService;
    private final LabUserPermissionChecker labUserPermission;

    @Operation(summary = "创建成果 v2", description = "仅接受叶子 categoryId；拒收 type/paperType/projectType")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "创建成功",
            content = @Content(schema = @Schema(implementation = Long.class))),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping
    @PreAuthorize("@permission.has('lab:achievement:add')")
    @AccessLog(title = "成果管理 v2")
    public ResponseDTO<Long> createV2(@Validated @RequestBody CreateLabAchievementV2Command cmd) {
        LabUserEntity current = labUserPermission.getCurrentLabUser();
        if (current == null) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION, "/v2/lab/achievements");
        }
        // 映射到旧命令对象（服务层会按 categoryId 推导并覆盖旧字段）
        CreateLabAchievementCommand legacy = toLegacy(cmd);
        Long id = achievementApplicationService.createAchievement(legacy, current.getId());
        return ResponseDTO.ok(id);
    }

    @Operation(summary = "更新成果 v2", description = "仅接受叶子 categoryId；拒收 type/paperType/projectType")
    @PutMapping("/{id}")
    @PreAuthorize("@permission.has('lab:achievement:edit')")
    @AccessLog(title = "成果管理 v2")
    public ResponseDTO<Void> updateV2(@PathVariable Long id, @Validated @RequestBody CreateLabAchievementV2Command cmd) {
        LabUserEntity current = labUserPermission.getCurrentLabUser();
        if (current == null) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION, "/v2/lab/achievements");
        }
        CreateLabAchievementCommand legacy = toLegacy(cmd);
        boolean isAdmin = labUserPermission.isAdmin();
        achievementApplicationService.updateAchievement(id, legacy, current.getId(), isAdmin);
        return ResponseDTO.ok();
    }

    private CreateLabAchievementCommand toLegacy(CreateLabAchievementV2Command v2) {
        CreateLabAchievementCommand c = new CreateLabAchievementCommand();
        c.setTitle(v2.getTitle());
        c.setTitleEn(v2.getTitleEn());
        c.setDescription(v2.getDescription());
        c.setKeywords(v2.getKeywords());
        c.setCategoryId(v2.getCategoryId());
        c.setVenue(v2.getVenue());
        c.setPublishDate(v2.getPublishDate());
        c.setProjectStartDate(v2.getProjectStartDate());
        c.setProjectEndDate(v2.getProjectEndDate());
        c.setReference(v2.getReference());
        c.setLinkUrl(v2.getLinkUrl());
        c.setGitUrl(v2.getGitUrl());
        c.setHomepageUrl(v2.getHomepageUrl());
        c.setPdfUrl(v2.getPdfUrl());
        c.setDoi(v2.getDoi());
        c.setFundingAmount(v2.getFundingAmount());
        c.setPublished(v2.getPublished());
        c.setExtra(v2.getExtra());
        c.setAuthors(v2.getAuthors());
        // 不设置 legacy 字段，由服务层按 categoryId 自动推导
        return c;
    }
}

