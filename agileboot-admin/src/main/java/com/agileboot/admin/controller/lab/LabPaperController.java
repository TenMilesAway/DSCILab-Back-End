package com.agileboot.admin.controller.lab;

import com.agileboot.admin.customize.aop.accessLog.AccessLog;
import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.core.validation.CreateGroup;
import com.agileboot.common.core.validation.UpdateGroup;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.lab.paper.LabPaperApplicationService;
import com.agileboot.domain.lab.paper.command.CreatePaperCommand;
import com.agileboot.domain.lab.paper.command.UpdatePaperCommand;
import com.agileboot.domain.lab.paper.dto.LabPaperDTO;
import com.agileboot.domain.lab.paper.query.LabPaperQuery;
import com.agileboot.domain.lab.achievement.dto.LabFundAssociationDTO;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 后台论文管理接口
 */
@Tag(name = "实验室论文", description = "论文管理接口")
@RestController
@RequestMapping("/lab/papers")
@RequiredArgsConstructor
public class LabPaperController extends BaseController {

    private final LabPaperApplicationService labPaperApplicationService;
    private final LabUserPermissionChecker labUserPermission;

    @Operation(summary = "论文列表", description = "分页查询论文列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(schema = @Schema(implementation = PageDTO.class)))
    })
    @GetMapping
    @PreAuthorize("@permission.has('lab:paper:query') or isAuthenticated()")
    @AccessLog(title = "论文管理")
    public ResponseDTO<PageDTO<LabPaperDTO>> list(
        @Parameter(description = "查询条件") LabPaperQuery query) {
        if (labUserPermission.isAdmin()) {
            return ResponseDTO.ok(labPaperApplicationService.getPaperList(query));
        }
        Long currentUserId = getCurrentLabUserId();
        query.setOwnerUserId(null);
        return ResponseDTO.ok(labPaperApplicationService.getMyPaperList(query, currentUserId));
    }

    @Operation(summary = "论文基金关联", description = "查看论文关联的基金")
    @GetMapping("/{id}/funds")
    @PreAuthorize("@permission.has('lab:paper:query') or isAuthenticated()")
    @AccessLog(title = "论文管理")
    public ResponseDTO<List<LabFundAssociationDTO>> funds(@PathVariable Long id) {
        return ResponseDTO.ok(labPaperApplicationService.getPaperFunds(id));
    }

    @Operation(summary = "更新论文基金关联", description = "替换论文关联的基金列表")
    @PutMapping("/{id}/funds")
    @PreAuthorize("@permission.has('lab:paper:edit') or isAuthenticated()")
    @AccessLog(title = "论文管理")
    public ResponseDTO<Void> updateFunds(
        @PathVariable Long id,
        @RequestBody @Valid List<CreatePaperCommand.PaperFundCommand> funds) {
        labPaperApplicationService.updatePaperFunds(id, funds, getCurrentLabUserId(), labUserPermission.isAdmin());
        return ResponseDTO.ok();
    }

    @Operation(summary = "论文详情", description = "获取单篇论文的详细信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(schema = @Schema(implementation = LabPaperDTO.class))),
        @ApiResponse(responseCode = "404", description = "论文不存在")
    })
    @GetMapping("/{id}")
    @PreAuthorize("@permission.has('lab:paper:query')")
    @AccessLog(title = "论文管理")
    public ResponseDTO<LabPaperDTO> detail(
        @Parameter(description = "论文ID", required = true) @PathVariable Long id) {
        LabPaperDTO dto = labPaperApplicationService.getPaperDetail(id);
        if (dto == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "论文");
        }
        return ResponseDTO.ok(dto);
    }

    @Operation(summary = "创建论文")
    @PostMapping
    @PreAuthorize("@permission.has('lab:paper:add') or isAuthenticated()")
    @AccessLog(title = "论文管理")
    public ResponseDTO<Long> create(@Validated(CreateGroup.class) @RequestBody CreatePaperCommand command) {
        Long ownerId = getCurrentLabUserId();
        Long id = labPaperApplicationService.createPaper(command, ownerId);
        return ResponseDTO.ok(id);
    }

    @Operation(summary = "更新论文")
    @PutMapping("/{id}")
    @PreAuthorize("@permission.has('lab:paper:edit') or isAuthenticated()")
    @AccessLog(title = "论文管理")
    public ResponseDTO<Void> update(
        @PathVariable Long id,
        @Validated(UpdateGroup.class) @RequestBody UpdatePaperCommand command) {
        labPaperApplicationService.updatePaper(id, command, getCurrentLabUserId(), labUserPermission.isAdmin());
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除论文")
    @DeleteMapping("/{id}")
    @PreAuthorize("@permission.has('lab:paper:remove') or isAuthenticated()")
    @AccessLog(title = "论文管理")
    public ResponseDTO<Void> delete(
        @Parameter(description = "论文ID", required = true) @PathVariable Long id) {
        labPaperApplicationService.deletePaper(id, getCurrentLabUserId(), labUserPermission.isAdmin());
        return ResponseDTO.ok();
    }

    private Long getCurrentLabUserId() {
        LabUserEntity current = labUserPermission.getCurrentLabUser();
        if (current == null) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION, "/lab/papers");
        }
        return current.getId();
    }
}
