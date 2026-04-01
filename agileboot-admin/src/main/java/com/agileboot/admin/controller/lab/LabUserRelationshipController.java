package com.agileboot.admin.controller.lab;

import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.lab.relationship.LabUserRelationshipApplicationService;
import com.agileboot.domain.lab.relationship.command.CreateUserRelationshipCommand;
import com.agileboot.domain.lab.relationship.db.LabUserRelationshipEntity;
import com.agileboot.domain.lab.relationship.dto.UserRelationshipDTO;
import com.agileboot.admin.customize.aop.accessLog.AccessLog;
import com.agileboot.common.enums.common.BusinessTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 师生关系管理控制器
 *
 * @author agileboot
 */
@Tag(name = "师生关系管理")
@RestController
@RequestMapping("/lab/users/relationships")
@RequiredArgsConstructor
@Validated
public class LabUserRelationshipController extends BaseController {

    private final LabUserRelationshipApplicationService relationshipApplicationService;

    @Operation(summary = "创建师生关系")
    @AccessLog(title = "师生关系管理", businessType = BusinessTypeEnum.ADD)
    @PostMapping
    @PreAuthorize("@permission.has('lab:user:relationship:create')")
    public ResponseDTO<Long> createRelationship(@Valid @RequestBody CreateUserRelationshipCommand command) {
        Long relationshipId = relationshipApplicationService.createRelationship(command);
        return ResponseDTO.ok(relationshipId);
    }

    @Operation(summary = "解除师生关系")
    @AccessLog(title = "师生关系管理", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/teacher/{teacherId}/student/{studentId}")
    @PreAuthorize("@permission.has('lab:user:relationship:delete')")
    public ResponseDTO<Void> endRelationship(
            @Parameter(description = "导师ID") @PathVariable @NotNull Long teacherId,
            @Parameter(description = "学生ID") @PathVariable @NotNull Long studentId) {
        relationshipApplicationService.endRelationship(teacherId, studentId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "获取导师的学生列表")
    @GetMapping("/teacher/{teacherId}/students")
    @PreAuthorize("@permission.has('lab:user:relationship:query')")
    public ResponseDTO<List<UserRelationshipDTO>> getStudentsByTeacherId(
            @Parameter(description = "导师ID") @PathVariable @NotNull Long teacherId,
            @Parameter(description = "关系状态：1=活跃,2=已结束，不传查询所有") @RequestParam(required = false) Integer status) {
        List<UserRelationshipDTO> students = relationshipApplicationService.getStudentsByTeacherId(teacherId, status);
        return ResponseDTO.ok(students);
    }

    @Operation(summary = "获取学生的导师信息")
    @GetMapping("/student/{studentId}/supervisor")
    @PreAuthorize("@permission.has('lab:user:relationship:query')")
    public ResponseDTO<UserRelationshipDTO> getSupervisorByStudentId(
            @Parameter(description = "学生ID") @PathVariable @NotNull Long studentId,
            @Parameter(description = "关系状态：1=活跃,2=已结束，不传查询所有") @RequestParam(required = false) Integer status) {
        UserRelationshipDTO supervisor = relationshipApplicationService.getSupervisorByStudentId(studentId, status);
        return ResponseDTO.ok(supervisor);
    }

    @Operation(summary = "更换学生的导师")
    @AccessLog(title = "师生关系管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/student/{studentId}/supervisor/{newTeacherId}")
    @PreAuthorize("@permission.has('lab:user:relationship:update')")
    public ResponseDTO<Long> changeSupervisor(
            @Parameter(description = "学生ID") @PathVariable @NotNull Long studentId,
            @Parameter(description = "新导师ID") @PathVariable @NotNull Long newTeacherId,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        Long relationshipId = relationshipApplicationService.changeSupervisor(studentId, newTeacherId, remark);
        return ResponseDTO.ok(relationshipId);
    }

    @Operation(summary = "为导师添加学生（快捷方式）")
    @AccessLog(title = "师生关系管理", businessType = BusinessTypeEnum.ADD)
    @PostMapping("/teacher/{teacherId}/student/{studentId}")
    @PreAuthorize("@permission.has('lab:user:relationship:create')")
    public ResponseDTO<Long> addStudentToTeacher(
            @Parameter(description = "导师ID") @PathVariable @NotNull Long teacherId,
            @Parameter(description = "学生ID") @PathVariable @NotNull Long studentId,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {

        CreateUserRelationshipCommand command = new CreateUserRelationshipCommand();
        command.setTeacherId(teacherId);
        command.setStudentId(studentId);
        command.setRemark(remark);

        Long relationshipId = relationshipApplicationService.createRelationship(command);
        return ResponseDTO.ok(relationshipId);
    }

    @Operation(summary = "获取活跃的导师学生列表")
    @GetMapping("/teacher/{teacherId}/active-students")
    @PreAuthorize("@permission.has('lab:user:relationship:query')")
    public ResponseDTO<List<UserRelationshipDTO>> getActiveStudentsByTeacherId(
            @Parameter(description = "导师ID") @PathVariable @NotNull Long teacherId) {
        List<UserRelationshipDTO> students = relationshipApplicationService.getStudentsByTeacherId(
            teacherId, LabUserRelationshipEntity.Status.ACTIVE.getCode());
        return ResponseDTO.ok(students);
    }

    @Operation(summary = "获取学生的活跃导师")
    @GetMapping("/student/{studentId}/active-supervisor")
    @PreAuthorize("@permission.has('lab:user:relationship:query')")
    public ResponseDTO<UserRelationshipDTO> getActiveSupervisorByStudentId(
            @Parameter(description = "学生ID") @PathVariable @NotNull Long studentId) {
        UserRelationshipDTO supervisor = relationshipApplicationService.getSupervisorByStudentId(
            studentId, LabUserRelationshipEntity.Status.ACTIVE.getCode());
        return ResponseDTO.ok(supervisor);
    }
}
