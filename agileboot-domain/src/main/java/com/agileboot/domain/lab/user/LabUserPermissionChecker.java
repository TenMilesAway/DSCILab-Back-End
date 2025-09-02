package com.agileboot.domain.lab.user;

import com.agileboot.domain.lab.user.db.LabUserEntity;
import com.agileboot.domain.lab.user.db.LabUserService;
import com.agileboot.infrastructure.user.AuthenticationUtils;
import com.agileboot.infrastructure.user.web.SystemLoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 实验室用户权限检查器
 * 
 * @author agileboot
 */
@Component("labUserPermission")
@RequiredArgsConstructor
public class LabUserPermissionChecker {

    private final LabUserService labUserService;

    /**
     * 检查是否可以查看指定用户信息
     * 
     * @param userId 目标用户ID
     * @return 是否有权限
     */
    public boolean canViewUser(Long userId) {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        if (loginUser == null) {
            return false;
        }

        // 管理员可以查看所有用户
        if (loginUser.isAdmin()) {
            return true;
        }

        // 查询目标用户信息
        LabUserEntity targetUser = labUserService.getById(userId);
        if (targetUser == null) {
            return false;
        }

        // 检查是否为本人
        return loginUser.getUsername().equals(targetUser.getUsername());
    }

    /**
     * 检查是否可以查看指定用户名的用户信息
     * 
     * @param username 目标用户名
     * @return 是否有权限
     */
    public boolean canViewUserByUsername(String username) {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        if (loginUser == null) {
            return false;
        }

        // 管理员可以查看所有用户
        if (loginUser.isAdmin()) {
            return true;
        }

        // 检查是否为本人
        return loginUser.getUsername().equals(username);
    }

    /**
     * 检查是否可以修改指定用户信息
     * 
     * @param userId 目标用户ID
     * @return 是否有权限
     */
    public boolean canEditUser(Long userId) {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        if (loginUser == null) {
            return false;
        }

        // 管理员可以修改所有用户
        if (loginUser.isAdmin()) {
            return true;
        }

        // 查询目标用户信息
        LabUserEntity targetUser = labUserService.getById(userId);
        if (targetUser == null) {
            return false;
        }

        // 检查是否为本人
        return loginUser.getUsername().equals(targetUser.getUsername());
    }

    /**
     * 检查当前用户是否为管理员
     * 
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        return loginUser != null && loginUser.isAdmin();
    }

    /**
     * 获取当前用户的实验室用户信息
     * 
     * @return 实验室用户信息
     */
    public LabUserEntity getCurrentLabUser() {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        if (loginUser == null) {
            return null;
        }

        return labUserService.getByUsername(loginUser.getUsername());
    }
}
