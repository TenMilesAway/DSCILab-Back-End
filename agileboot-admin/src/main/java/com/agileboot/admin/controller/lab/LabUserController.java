package com.agileboot.admin.controller.lab;

import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.lab.user.LabUserApplicationService;
import com.agileboot.domain.lab.user.dto.LabUserProfileDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 实验室用户控制器
 * 
 * @author agileboot
 */
@Tag(name = "实验室用户API", description = "实验室用户相关接口")
@RestController
@RequestMapping("/lab/users")
@RequiredArgsConstructor
public class LabUserController extends BaseController {

    private final LabUserApplicationService labUserApplicationService;

    /**
     * 获取当前登录用户的个人信息
     */
    @Operation(summary = "获取当前用户个人信息", description = "获取当前登录用户的详细个人信息")
    @GetMapping("/profile")
    public ResponseDTO<LabUserProfileDTO> getCurrentUserProfile() {
        LabUserProfileDTO profile = labUserApplicationService.getCurrentUserProfile();
        return ResponseDTO.ok(profile);
    }

    /**
     * 根据用户ID获取个人信息
     */
    @Operation(summary = "根据用户ID获取个人信息", description = "管理员可以查看任何用户信息，普通用户只能查看自己的信息")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @PreAuthorize("@permission.has('lab:user:query') OR @labUserPermission.canViewUser(#userId)")
    @GetMapping("/{userId}")
    public ResponseDTO<LabUserProfileDTO> getUserProfile(@PathVariable Long userId) {
        LabUserProfileDTO profile = labUserApplicationService.getUserProfile(userId);
        return ResponseDTO.ok(profile);
    }

    /**
     * 根据用户名获取个人信息
     */
    @Operation(summary = "根据用户名获取个人信息", description = "管理员可以查看任何用户信息，普通用户只能查看自己的信息")
    @Parameter(name = "username", description = "用户名", required = true)
    @PreAuthorize("@permission.has('lab:user:query') OR @labUserPermission.canViewUserByUsername(#username)")
    @GetMapping("/username/{username}")
    public ResponseDTO<LabUserProfileDTO> getUserProfileByUsername(@PathVariable String username) {
        LabUserProfileDTO profile = labUserApplicationService.getUserProfileByUsername(username);
        return ResponseDTO.ok(profile);
    }

    /**
     * 检查用户是否存在
     */
    @Operation(summary = "检查用户是否存在", description = "根据用户ID检查用户是否存在")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @GetMapping("/{userId}/exists")
    public ResponseDTO<Boolean> userExists(@PathVariable Long userId) {
        boolean exists = labUserApplicationService.userExists(userId);
        return ResponseDTO.ok(exists);
    }

    /**
     * 检查用户名是否存在
     */
    @Operation(summary = "检查用户名是否存在", description = "根据用户名检查用户是否存在")
    @Parameter(name = "username", description = "用户名", required = true)
    @GetMapping("/username/{username}/exists")
    public ResponseDTO<Boolean> usernameExists(@PathVariable String username) {
        boolean exists = labUserApplicationService.usernameExists(username);
        return ResponseDTO.ok(exists);
    }

    // ==================== 公开测试接口（无需认证） ====================

    /**
     * 测试接口 - 健康检查
     */
    @Operation(summary = "健康检查", description = "检查实验室用户服务是否正常运行")
    @GetMapping("/test/health")
    public ResponseDTO<String> healthCheck() {
        return ResponseDTO.ok("实验室用户服务运行正常！当前时间: " + java.time.LocalDateTime.now());
    }
}
