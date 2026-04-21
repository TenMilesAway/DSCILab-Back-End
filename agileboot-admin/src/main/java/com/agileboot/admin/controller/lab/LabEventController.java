package com.agileboot.admin.controller.lab;

import com.agileboot.admin.customize.aop.accessLog.AccessLog;
import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.lab.event.LabEventApplicationService;
import com.agileboot.domain.lab.event.command.CreateEventCommand;
import com.agileboot.domain.lab.event.command.UpdateEventCommand;
import com.agileboot.domain.lab.event.dto.LabEventDTO;
import com.agileboot.domain.lab.event.dto.LabEventListDTO;
import com.agileboot.domain.lab.event.query.LabEventQuery;
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

@Tag(name = "实验室活动", description = "活动管理接口")
@RestController
@RequestMapping("/lab/events")
@RequiredArgsConstructor
public class LabEventController extends BaseController {

    private final LabEventApplicationService eventApplicationService;
    private final LabUserPermissionChecker labUserPermission;

    @Operation(summary = "活动列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(schema = @Schema(implementation = PageDTO.class)))
    })
    @GetMapping
    @PreAuthorize("@permission.has('lab:event:query') or isAuthenticated()")
    @AccessLog(title = "活动管理")
    public ResponseDTO<PageDTO<LabEventListDTO>> list(@Parameter(description = "查询条件") LabEventQuery query) {
        if (labUserPermission.isAdmin()) {
            return ResponseDTO.ok(eventApplicationService.getEventList(query));
        }
        Long currentUserId = getCurrentLabUserId();
        query.setOwnerUserId(null);
        return ResponseDTO.ok(eventApplicationService.getMyEventList(query, currentUserId));
    }

    @Operation(summary = "活动详情")
    @GetMapping("/{id}")
    @PreAuthorize("@permission.has('lab:event:query') or isAuthenticated()")
    @AccessLog(title = "活动管理")
    public ResponseDTO<LabEventDTO> detail(@PathVariable Long id) {
        LabEventDTO dto = eventApplicationService.getEventDetail(id);
        if (dto == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "活动");
        }
        return ResponseDTO.ok(dto);
    }

    @Operation(summary = "创建活动")
    @PostMapping
    @PreAuthorize("@permission.has('lab:event:add') or isAuthenticated()")
    @AccessLog(title = "活动管理", isSaveRequestData = false)
    public ResponseDTO<Long> create(@Validated @RequestBody CreateEventCommand command) {
        Long ownerId = getCurrentLabUserId();
        Long id = eventApplicationService.createEvent(command, ownerId);
        return ResponseDTO.ok(id);
    }

    @Operation(summary = "更新活动")
    @PutMapping("/{id}")
    @PreAuthorize("@permission.has('lab:event:edit') or isAuthenticated()")
    @AccessLog(title = "活动管理", isSaveRequestData = false)
    public ResponseDTO<Void> update(@PathVariable Long id, @Validated @RequestBody UpdateEventCommand command) {
        eventApplicationService.updateEvent(id, command, getCurrentLabUserId(), labUserPermission.isAdmin());
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除活动")
    @DeleteMapping("/{id}")
    @PreAuthorize("@permission.has('lab:event:remove') or isAuthenticated()")
    @AccessLog(title = "活动管理")
    public ResponseDTO<Void> delete(@PathVariable Long id) {
        eventApplicationService.deleteEvent(id, getCurrentLabUserId(), labUserPermission.isAdmin());
        return ResponseDTO.ok();
    }

    @Operation(summary = "发布活动")
    @PutMapping("/{id}/publish")
    @PreAuthorize("@permission.has('lab:event:edit') or isAuthenticated()")
    @AccessLog(title = "活动管理")
    public ResponseDTO<Void> publish(@PathVariable Long id, @RequestParam Boolean published) {
        eventApplicationService.updatePublishStatus(id, published, getCurrentLabUserId(), labUserPermission.isAdmin());
        return ResponseDTO.ok();
    }

    private Long getCurrentLabUserId() {
        LabUserEntity current = labUserPermission.getCurrentLabUser();
        if (current == null) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION, "/lab/events");
        }
        return current.getId();
    }
}
