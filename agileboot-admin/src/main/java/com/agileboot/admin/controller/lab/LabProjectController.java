package com.agileboot.admin.controller.lab;

import com.agileboot.admin.customize.aop.accessLog.AccessLog;
import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.core.validation.CreateGroup;
import com.agileboot.common.core.validation.UpdateGroup;
import com.agileboot.domain.lab.project.LabProjectApplicationService;
import com.agileboot.domain.lab.project.command.CreateProjectCommand;
import com.agileboot.domain.lab.project.command.UpdateProjectCommand;
import com.agileboot.domain.lab.project.dto.LabProjectDTO;
import com.agileboot.domain.lab.project.query.LabProjectQuery;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "实验室项目", description = "项目管理接口")
@RestController
@RequestMapping("/lab/projects")
@RequiredArgsConstructor
public class LabProjectController extends BaseController {

    private final LabProjectApplicationService projectApplicationService;
    private final LabUserPermissionChecker labUserPermission;

    @Operation(summary = "项目列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(schema = @Schema(implementation = PageDTO.class)))
    })
    @GetMapping
    @PreAuthorize("@permission.has('lab:project:query') or isAuthenticated()")
    @AccessLog(title = "项目管理")
    public ResponseDTO<PageDTO<LabProjectDTO>> list(
        @Parameter(description = "查询条件") LabProjectQuery query) {
        if (labUserPermission.isAdmin()) {
            return ResponseDTO.ok(projectApplicationService.getProjectList(query));
        }
        Long currentUserId = getCurrentLabUserId();
        query.setOwnerUserId(null);
        return ResponseDTO.ok(projectApplicationService.getMyProjectList(query, currentUserId));
    }

    @Operation(summary = "项目详情")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(schema = @Schema(implementation = LabProjectDTO.class))),
        @ApiResponse(responseCode = "404", description = "项目不存在")
    })
    @GetMapping("/{id}")
    @PreAuthorize("@permission.has('lab:project:query') or isAuthenticated()")
    @AccessLog(title = "项目管理")
    public ResponseDTO<LabProjectDTO> detail(@PathVariable Long id) {
        LabProjectDTO dto = projectApplicationService.getProjectDetail(id);
        if (dto == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "项目");
        }
        return ResponseDTO.ok(dto);
    }

    @Operation(summary = "创建项目")
    @PostMapping
    @PreAuthorize("@permission.has('lab:project:add') or isAuthenticated()")
    @AccessLog(title = "项目管理")
    public ResponseDTO<Long> create(@Validated(CreateGroup.class) @RequestBody CreateProjectCommand command) {
        Long ownerId = getCurrentLabUserId();
        Long id = projectApplicationService.createProject(command, ownerId);
        return ResponseDTO.ok(id);
    }

    @Operation(summary = "更新项目")
    @PutMapping("/{id}")
    @PreAuthorize("@permission.has('lab:project:edit') or isAuthenticated()")
    @AccessLog(title = "项目管理")
    public ResponseDTO<Void> update(@PathVariable Long id,
                                    @Validated(UpdateGroup.class) @RequestBody UpdateProjectCommand command) {
        projectApplicationService.updateProject(id, command, getCurrentLabUserId(), labUserPermission.isAdmin());
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除项目")
    @DeleteMapping("/{id}")
    @PreAuthorize("@permission.has('lab:project:remove') or isAuthenticated()")
    @AccessLog(title = "项目管理")
    public ResponseDTO<Void> delete(@PathVariable Long id) {
        projectApplicationService.deleteProject(id, getCurrentLabUserId(), labUserPermission.isAdmin());
        return ResponseDTO.ok();
    }

    @Operation(summary = "发布项目")
    @PutMapping("/{id}/publish")
    @PreAuthorize("@permission.has('lab:project:edit') or isAuthenticated()")
    @AccessLog(title = "项目管理")
    public ResponseDTO<Void> publish(@PathVariable Long id, @RequestParam Boolean published) {
        projectApplicationService.updatePublishStatus(id, published, getCurrentLabUserId(), labUserPermission.isAdmin());
        return ResponseDTO.ok();
    }

    @Operation(summary = "审核项目")
    @PutMapping("/{id}/verify")
    @PreAuthorize("@permission.has('lab:project:verify')")
    @AccessLog(title = "项目管理")
    public ResponseDTO<Void> verify(@PathVariable Long id, @RequestParam Boolean verified) {
        projectApplicationService.updateVerifyStatus(id, verified, getCurrentLabUserId(), labUserPermission.isAdmin());
        return ResponseDTO.ok();
    }

    private Long getCurrentLabUserId() {
        LabUserEntity current = labUserPermission.getCurrentLabUser();
        if (current == null) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION, "/lab/projects");
        }
        return current.getId();
    }
}
