package com.agileboot.admin.controller.lab;

import com.agileboot.admin.customize.aop.accessLog.AccessLog;
import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.core.dto.ResponseDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import com.agileboot.domain.lab.achievement.LabAchievementApplicationService;
import com.agileboot.domain.lab.achievement.command.CreateLabAchievementCommand;
import com.agileboot.domain.lab.achievement.command.FundAssociationCommand;
import com.agileboot.domain.lab.achievement.dto.LabAchievementDTO;
import com.agileboot.domain.lab.achievement.dto.LabFundAssociationDTO;
import com.agileboot.domain.lab.achievement.query.LabAchievementQuery;
import com.agileboot.domain.lab.achievement.query.MyAchievementQuery;
import com.agileboot.domain.lab.paper.LabPaperApplicationService;
import com.agileboot.domain.lab.user.LabUserPermissionChecker;
import com.agileboot.domain.lab.user.db.LabUserEntity;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 成果管理控制器（管理员端）
 */
@Tag(name = "成果管理", description = "实验室成果管理接口")
@RestController
@RequestMapping("/lab/achievements")
@RequiredArgsConstructor


public class LabAchievementController extends BaseController {

    private final LabAchievementApplicationService achievementApplicationService;
    private final LabPaperApplicationService labPaperApplicationService;
    private final LabUserPermissionChecker labUserPermission;


    @Operation(summary = "成果列表", description = "分页查询成果列表，支持关键词、类型、状态等筛选")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(schema = @Schema(implementation = PageDTO.class))),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping
    @PreAuthorize("@permission.has('lab:achievement:list')")
    @AccessLog(title = "成果管理")
    public ResponseDTO<PageDTO<LabAchievementDTO>> list(
        @Parameter(description = "查询条件") LabAchievementQuery query,
        @RequestParam(value = "excludeProject", required = false) String excludeProject,
        @RequestParam(value = "parentCategoryId", required = false) Long parentCategoryId,
        @RequestParam(value = "categoryId", required = false) Long categoryId) {

        if (excludeProject != null) {
            query.setExcludeProject(Boolean.parseBoolean(excludeProject));
        }
        // 显式赋值，避免极端情况下的数据绑定丢失
        if (parentCategoryId != null) {
            query.setParentCategoryId(parentCategoryId);
        }
        if (categoryId != null) {
            query.setCategoryId(categoryId);
        }

        // 检查当前用户是否为管理员
        if (labUserPermission.isAdmin()) {
            // 管理员可以查看所有成果
            PageDTO<LabAchievementDTO> pageDTO = achievementApplicationService.getAchievementList(query);
            return ResponseDTO.ok(pageDTO);
        } else {
            // 普通用户只能查看自己参与的成果
            LabUserEntity currentUser = labUserPermission.getCurrentLabUser();
            if (currentUser == null) {
                throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION, "未找到当前用户信息");
            }

            // 将LabAchievementQuery转换为MyAchievementQuery（保留公共筛选 + 分类筛选）
            MyAchievementQuery myQuery = new MyAchievementQuery();
            myQuery.setPageNum(query.getPageNum());
            myQuery.setPageSize(query.getPageSize());
            myQuery.setKeyword(query.getKeyword());
            myQuery.setType(query.getType());
            myQuery.setPaperType(query.getPaperType());
            myQuery.setProjectType(query.getProjectType());
            myQuery.setExcludeProject(query.getExcludeProject());
            myQuery.setCategoryId(query.getCategoryId());
            myQuery.setParentCategoryId(query.getParentCategoryId());
            myQuery.setPublished(query.getPublished());
            myQuery.setIsVerified(query.getIsVerified());
            myQuery.setDateStart(query.getDateStart());
            myQuery.setDateEnd(query.getDateEnd());

            PageDTO<LabAchievementDTO> pageDTO = achievementApplicationService.getMyAchievementList(myQuery, currentUser.getId());
            return ResponseDTO.ok(pageDTO);
        }
    }

    @Operation(summary = "成果详情", description = "根据ID获取成果详细信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(schema = @Schema(implementation = LabAchievementDTO.class))),
        @ApiResponse(responseCode = "404", description = "成果不存在"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @GetMapping("/{id}")
    @PreAuthorize("@permission.has('lab:achievement:query')")
    @AccessLog(title = "成果管理")
    public ResponseDTO<LabAchievementDTO> getDetail(
        @Parameter(description = "成果ID", required = true) @PathVariable Long id) {
        LabAchievementDTO dto = achievementApplicationService.getAchievementDetail(id);
        return ResponseDTO.ok(dto);
    }

    @Operation(summary = "创建成果", description = "创建新的成果记录，支持批量附带作者")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "创建成功，返回成果ID",
            content = @Content(schema = @Schema(implementation = Long.class))),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PostMapping
    @PreAuthorize("@permission.has('lab:achievement:add')")
    @AccessLog(title = "成果管理")
    public ResponseDTO<Long> create(
        @Parameter(description = "成果创建信息", required = true)
        @Validated @RequestBody CreateLabAchievementCommand command) {
        LabUserEntity current = labUserPermission.getCurrentLabUser();
        if (current == null) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION, "/lab/achievements");
        }
        Long achievementId = achievementApplicationService.createAchievement(command, current.getId());
        return ResponseDTO.ok(achievementId);
    }

    @Operation(summary = "更新成果", description = "更新成果信息")
    @PutMapping("/{id}")
    @PreAuthorize("@permission.has('lab:achievement:edit')")
    @AccessLog(title = "成果管理")
    public ResponseDTO<Void> update(@PathVariable Long id, @Validated @RequestBody CreateLabAchievementCommand command) {
        LabUserEntity current = labUserPermission.getCurrentLabUser();
        if (current == null) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION, "/lab/achievements");
        }
        boolean isAdmin = labUserPermission.isAdmin();
        achievementApplicationService.updateAchievement(id, command, current.getId(), isAdmin);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除成果", description = "删除成果")
    @DeleteMapping("/{id}")
    @PreAuthorize("@permission.has('lab:achievement:remove')")
    @AccessLog(title = "成果管理")
    public ResponseDTO<Void> delete(@PathVariable Long id) {
        LabUserEntity current = labUserPermission.getCurrentLabUser();
        if (current == null) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION, "/lab/achievements");
        }
        boolean isAdmin = labUserPermission.isAdmin();
        achievementApplicationService.deleteAchievement(id, current.getId(), isAdmin);
        return ResponseDTO.ok();
    }

    @Operation(summary = "发布成果", description = "发布/取消发布成果")
    @PutMapping("/{id}/publish")
    @PreAuthorize("isAuthenticated()")
    @AccessLog(title = "成果管理")
    public ResponseDTO<Void> publish(@PathVariable Long id, @RequestParam Boolean published) {
        LabUserEntity current = labUserPermission.getCurrentLabUser();
        if (current == null) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION, "/lab/achievements");
        }
        boolean isAdmin = labUserPermission.isAdmin();
        achievementApplicationService.publishAchievement(id, published, current.getId(), isAdmin);
        return ResponseDTO.ok();
    }

    @Operation(summary = "审核成果", description = "审核通过/取消审核成果")
    @PutMapping("/{id}/verify")
    @PreAuthorize("@permission.has('lab:achievement:verify')")
    @AccessLog(title = "成果管理")
    public ResponseDTO<Void> verify(@PathVariable Long id, @RequestParam Boolean verified) {
        LabUserEntity current = labUserPermission.getCurrentLabUser();
        if (current == null) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION, "/lab/achievements");
        }
        boolean isAdmin = labUserPermission.isAdmin();
        achievementApplicationService.verifyAchievement(id, verified, current.getId(), isAdmin);
        return ResponseDTO.ok();
    }

    @Operation(summary = "查询论文基金关联", description = "查看指定论文成果的基金关联信息")
    @GetMapping("/{id}/funds")
    @PreAuthorize("@permission.has('lab:achievement:query')")
    @AccessLog(title = "成果管理")
    public ResponseDTO<List<LabFundAssociationDTO>> getFunds(@PathVariable Long id) {
        return ResponseDTO.ok(labPaperApplicationService.getPaperFunds(id));
    }

    @Operation(summary = "更新论文基金关联", description = "替换指定论文成果的基金关联列表")
    @PutMapping("/{id}/funds")
    @PreAuthorize("@permission.has('lab:achievement:edit')")
    @AccessLog(title = "成果管理")
    public ResponseDTO<Void> updateFunds(@PathVariable Long id,
                                         @RequestBody @Valid List<FundAssociationCommand> associations) {
        LabUserEntity current = labUserPermission.getCurrentLabUser();
        if (current == null) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION, "/lab/achievements");
        }
        boolean isAdmin = labUserPermission.isAdmin();
        labPaperApplicationService.updatePaperFunds(
            id,
            convertFundCommands(associations),
            current.getId(),
            isAdmin);
        return ResponseDTO.ok();
    }

    private List<com.agileboot.domain.lab.paper.command.CreatePaperCommand.PaperFundCommand> convertFundCommands(
        List<FundAssociationCommand> associations) {
        if (associations == null) {
            return java.util.Collections.emptyList();
        }
        return associations.stream().map(cmd -> {
            com.agileboot.domain.lab.paper.command.CreatePaperCommand.PaperFundCommand pf =
                new com.agileboot.domain.lab.paper.command.CreatePaperCommand.PaperFundCommand();
            pf.setFundId(cmd.getFundId());
            pf.setAmount(cmd.getAmount());
            return pf;
        }).collect(java.util.stream.Collectors.toList());
    }
}
