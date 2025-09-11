package com.agileboot.admin.controller.lab;

import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.domain.lab.user.LabUserCrudApplicationService;
import com.agileboot.domain.lab.user.command.*;
import com.agileboot.domain.lab.user.dto.LabUserProfileDTO;
import com.agileboot.domain.lab.user.query.LabUserQuery;
import com.agileboot.common.constant.Constants.UploadSubDir;
import com.agileboot.common.utils.file.FileUploadUtils;
import com.agileboot.domain.common.dto.UploadFileDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 实验室用户CRUD控制器
 *
 * @author agileboot
 */
@Tag(name = "实验室用户CRUD API", description = "实验室用户增删改查接口")
@RestController
@RequestMapping("/lab/users/crud")
@RequiredArgsConstructor
public class LabUserCrudController extends BaseController {

    private final LabUserCrudApplicationService labUserCrudApplicationService;

    // ==================== 创建功能 ====================

    /**
     * 创建用户
     */
    @Operation(summary = "创建用户", description = "管理员创建新用户")
    @PreAuthorize("@permission.has('lab:user:add') OR @labUserPermission.isAdmin()")
    @PostMapping
    public ResponseDTO<Long> createUser(@Validated @RequestBody CreateLabUserCommand command) {
        Long userId = labUserCrudApplicationService.createUser(command);
        return ResponseDTO.ok(userId);
    }

    // ==================== 更新功能 ====================

    /**
     * 更新用户信息（管理员）
     */
    @Operation(summary = "更新用户信息", description = "管理员更新用户信息")
    @PreAuthorize("@permission.has('lab:user:edit') OR @labUserPermission.isAdmin()")
    @PutMapping("/{userId}")
    public ResponseDTO<Void> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Validated @RequestBody UpdateLabUserCommand command) {
        command.setId(userId);
        labUserCrudApplicationService.updateUser(command);
        return ResponseDTO.ok();
    }

    /**
     * 更新个人信息
     */
    @Operation(summary = "更新个人信息", description = "用户更新自己的个人信息")
    @PutMapping("/profile")
    public ResponseDTO<Void> updateProfile(@Validated @RequestBody UpdateProfileCommand command) {
        labUserCrudApplicationService.updateProfile(command);
        return ResponseDTO.ok();
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码", description = "用户修改自己的密码")
    @PutMapping("/password")
    public ResponseDTO<Void> changePassword(@Validated @RequestBody ChangePasswordCommand command) {
        labUserCrudApplicationService.changePassword(command);
        return ResponseDTO.ok();
    }

    /**
     * 管理员重置用户密码（无需旧密码）
     */
    @Operation(summary = "管理员重置用户密码", description = "管理员重置用户密码，密码规则与创建用户一致（6-20位）")
    @PreAuthorize("@permission.has('lab:user:edit') OR @labUserPermission.isAdmin()")
    @PutMapping("/{userId}/password")
    public ResponseDTO<Void> resetPasswordByAdmin(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Validated @RequestBody ResetLabUserPasswordCommand command) {
        command.setUserId(userId);
        labUserCrudApplicationService.resetPasswordByAdmin(command);
        return ResponseDTO.ok();
    }


    // ==================== 删除功能 ====================

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户", description = "管理员删除用户（硬删除）")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @PreAuthorize("@permission.has('lab:user:remove') OR @labUserPermission.isAdmin()")
    @DeleteMapping("/{userId}")
    public ResponseDTO<Void> deleteUser(@PathVariable Long userId) {
        labUserCrudApplicationService.deleteUser(userId);
        return ResponseDTO.ok();
    }

    // ==================== 查询功能 ====================

    /**
     * 分页查询用户列表
     */
    @Operation(summary = "分页查询用户列表", description = "分页查询用户列表，支持多条件筛选")
    @PreAuthorize("@permission.has('lab:user:list') OR @labUserPermission.isAdmin()")
    @GetMapping("/list")
    public ResponseDTO<PageDTO<LabUserProfileDTO>> getUserList(LabUserQuery query) {
        PageDTO<LabUserProfileDTO> result = labUserCrudApplicationService.getUserList(query);
        return ResponseDTO.ok(result);
    }

    /**
     * 搜索用户
     */
    @Operation(summary = "搜索用户", description = "根据关键词搜索用户")
    @Parameter(name = "keyword", description = "搜索关键词", required = true)
    @GetMapping("/search")
    public ResponseDTO<List<LabUserProfileDTO>> searchUsers(@RequestParam String keyword) {
        List<LabUserProfileDTO> result = labUserCrudApplicationService.searchUsers(keyword);
        return ResponseDTO.ok(result);
    }

    // ==================== 批量操作 ====================

    /**
     * 批量删除用户
     */
    @Operation(summary = "批量删除用户", description = "管理员批量删除用户（硬删除）")
    @PreAuthorize("@permission.has('lab:user:remove') OR @labUserPermission.isAdmin()")
    @DeleteMapping("/batch")
    public ResponseDTO<Void> batchDeleteUsers(@RequestBody List<Long> userIds) {
        for (Long userId : userIds) {
            labUserCrudApplicationService.deleteUser(userId); // 单个硬删（带存在性校验）
        }
        return ResponseDTO.ok();
    }

    /**
     * 批量启用/禁用用户
     */
    @Operation(summary = "批量启用/禁用用户", description = "管理员批量启用或禁用用户")
    @PreAuthorize("@permission.has('lab:user:edit') OR @labUserPermission.isAdmin()")
    @PutMapping("/batch/status")
    public ResponseDTO<Void> batchUpdateUserStatus(
            @RequestBody List<Long> userIds,
            @RequestParam Boolean isActive) {
        for (Long userId : userIds) {
            UpdateLabUserCommand command = new UpdateLabUserCommand();
            command.setId(userId);
            command.setIsActive(isActive);
            // 这里需要先获取用户信息，然后更新状态
            // 为了简化，暂时不实现完整逻辑
        }
        return ResponseDTO.ok();
    }

    // ==================== 统计功能 ====================

    /**
     * 获取用户统计信息
     */
    @Operation(summary = "获取用户统计信息", description = "获取用户身份分布等统计信息")
    @PreAuthorize("@permission.has('lab:user:list') OR @labUserPermission.isAdmin()")
    @GetMapping("/statistics")
    public ResponseDTO<java.util.Map<String, Object>> getUserStatistics() {
        // 这里可以调用原有的统计方法
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("message", "统计功能待实现");
        return ResponseDTO.ok(result);
    }

    // ==================== 导入导出功能 ====================

    /**
     * 导出用户列表
     */
    @Operation(summary = "导出用户列表", description = "导出用户列表到Excel")
    @PreAuthorize("@permission.has('lab:user:export') OR @labUserPermission.isAdmin()")
    @GetMapping("/export")
    public ResponseDTO<String> exportUsers(LabUserQuery query) {
        // 导出功能待实现
        return ResponseDTO.ok("导出功能待实现");
    }

    /**
     * 导入用户
     */
    @Operation(summary = "导入用户", description = "从Excel导入用户")
    @PreAuthorize("@permission.has('lab:user:import') OR @labUserPermission.isAdmin()")
    @PostMapping("/import")
    public ResponseDTO<String> importUsers() {
        // 导入功能待实现
        return ResponseDTO.ok("导入功能待实现");
    }

    // ==================== 文件上传功能 ====================

    /**
     * 上传用户头像
     */
    @Operation(summary = "上传用户头像", description = "用户上传个人头像")
    @PostMapping("/profile/photo")
    public ResponseDTO<UploadFileDTO> uploadPhoto(@RequestParam("photofile") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ApiException(ErrorCode.Business.USER_UPLOAD_FILE_FAILED);
        }

        // 上传文件到头像目录
        String photoUrl = FileUploadUtils.upload(UploadSubDir.AVATAR_PATH, file);

        // 仅更新当前用户的头像，不影响其他字段
        labUserCrudApplicationService.updateProfilePhoto(photoUrl);

        return ResponseDTO.ok(new UploadFileDTO(photoUrl));
    }

    /**
     * 管理员为指定用户上传头像
     */
    @Operation(summary = "管理员上传用户头像", description = "管理员为指定用户上传头像")
    @PreAuthorize("@permission.has('lab:user:edit') OR @labUserPermission.isAdmin()")
    @PostMapping("/{userId}/photo")
    public ResponseDTO<UploadFileDTO> uploadUserPhoto(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @RequestParam("photofile") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ApiException(ErrorCode.Business.USER_UPLOAD_FILE_FAILED);
        }

        // 上传文件到头像目录
        String photoUrl = FileUploadUtils.upload(UploadSubDir.AVATAR_PATH, file);

        // 更新指定用户的头像（直接更新，避免验证其他必填字段）
        labUserCrudApplicationService.updateUserPhoto(userId, photoUrl);

        return ResponseDTO.ok(new UploadFileDTO(photoUrl));
    }
}
