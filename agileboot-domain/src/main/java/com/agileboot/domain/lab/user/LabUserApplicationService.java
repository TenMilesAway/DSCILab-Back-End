package com.agileboot.domain.lab.user;

import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.lab.user.db.LabUserEntity;
import com.agileboot.domain.lab.user.db.LabUserService;
import com.agileboot.domain.lab.user.dto.LabUserProfileDTO;
import com.agileboot.infrastructure.user.AuthenticationUtils;
import com.agileboot.infrastructure.user.web.SystemLoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 实验室用户应用服务
 * 
 * @author agileboot
 */
@Service
@RequiredArgsConstructor
public class LabUserApplicationService {

    private final LabUserService labUserService;

    /**
     * 获取当前登录用户的个人信息
     * 
     * @return 个人信息
     */
    public LabUserProfileDTO getCurrentUserProfile() {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        if (loginUser == null) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION);
        }

        // 通过用户名关联查询实验室用户信息
        String username = loginUser.getUsername();
        LabUserEntity labUser = labUserService.getByUsername(username);
        
        if (labUser == null) {
            throw new ApiException(ErrorCode.Business.USER_NON_EXIST);
        }

        return new LabUserProfileDTO(labUser);
    }

    /**
     * 根据用户ID获取个人信息
     * 
     * @param userId 用户ID
     * @return 个人信息
     */
    public LabUserProfileDTO getUserProfile(Long userId) {
        if (userId == null) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID);
        }

        LabUserEntity labUser = labUserService.getById(userId);
        if (labUser == null) {
            throw new ApiException(ErrorCode.Business.USER_NON_EXIST);
        }

        // 权限检查：只有管理员或本人可以查看详细信息
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        if (!isAdminOrSelf(loginUser, labUser)) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION);
        }

        return new LabUserProfileDTO(labUser);
    }

    /**
     * 根据用户名获取个人信息
     * 
     * @param username 用户名
     * @return 个人信息
     */
    public LabUserProfileDTO getUserProfileByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID);
        }

        LabUserEntity labUser = labUserService.getByUsername(username);
        if (labUser == null) {
            throw new ApiException(ErrorCode.Business.USER_NON_EXIST);
        }

        // 权限检查：只有管理员或本人可以查看详细信息
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        if (!isAdminOrSelf(loginUser, labUser)) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION);
        }

        return new LabUserProfileDTO(labUser);
    }

    /**
     * 检查是否为管理员或本人
     * 
     * @param loginUser 当前登录用户
     * @param targetUser 目标用户
     * @return 是否有权限
     */
    private boolean isAdminOrSelf(SystemLoginUser loginUser, LabUserEntity targetUser) {
        if (loginUser == null || targetUser == null) {
            return false;
        }

        // 检查是否为管理员
        if (loginUser.isAdmin()) {
            return true;
        }

        // 检查是否为本人
        return loginUser.getUsername().equals(targetUser.getUsername());
    }

    /**
     * 检查用户是否存在
     * 
     * @param userId 用户ID
     * @return 是否存在
     */
    public boolean userExists(Long userId) {
        if (userId == null) {
            return false;
        }
        return labUserService.getById(userId) != null;
    }

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    public boolean usernameExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return labUserService.getByUsername(username) != null;
    }

}
