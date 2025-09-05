package com.agileboot.domain.lab.user;

import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.lab.user.command.*;
import com.agileboot.domain.lab.user.db.LabUserEntity;
import com.agileboot.domain.lab.user.db.LabUserService;
import com.agileboot.domain.lab.user.dto.LabUserProfileDTO;
import com.agileboot.domain.lab.user.query.LabUserQuery;
import com.agileboot.infrastructure.user.AuthenticationUtils;
import com.agileboot.infrastructure.user.web.SystemLoginUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 实验室用户CRUD应用服务
 *
 * @author agileboot
 */
@Service
@RequiredArgsConstructor
public class LabUserCrudApplicationService {


    private static String normalizeBlankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private final LabUserService labUserService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 创建用户
     *
     * @param command 创建命令
     * @return 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(CreateLabUserCommand command) {
        // 验证用户名唯一性
        if (labUserService.isUsernameDuplicated(command.getUsername(), null)) {
            throw new ApiException(ErrorCode.Business.USER_NAME_IS_NOT_UNIQUE);
        }

        // 验证学号唯一性
        if (StringUtils.hasText(command.getStudentNumber()) &&
            labUserService.isStudentNumberDuplicated(command.getStudentNumber(), null)) {
            throw new ApiException(ErrorCode.Business.USER_NAME_IS_NOT_UNIQUE);
        }

        // 验证邮箱唯一性
        if (StringUtils.hasText(command.getEmail()) &&
            labUserService.isEmailDuplicated(command.getEmail(), null)) {
            throw new ApiException(ErrorCode.Business.USER_EMAIL_IS_NOT_UNIQUE);
        }

        // 验证手机号唯一性
        if (StringUtils.hasText(command.getPhone()) &&
            labUserService.isPhoneDuplicated(command.getPhone(), null)) {
            throw new ApiException(ErrorCode.Business.USER_PHONE_NUMBER_IS_NOT_UNIQUE);
        }

        // 创建用户实体
        LabUserEntity user = new LabUserEntity();
        user.setStudentNumber(normalizeBlankToNull(command.getStudentNumber()));
        user.setUsername(command.getUsername());
        user.setRealName(command.getRealName());
        user.setEnglishName(command.getEnglishName());
        user.setPassword(passwordEncoder.encode(command.getPassword()));
        user.setGender(command.getGender());
        user.setIdentity(command.getIdentity());
        user.setAcademicStatus(command.getAcademicStatus());
        user.setResearchArea(command.getResearchArea());
        // 允许前端传空字符串：统一归一为 null，避免命中唯一索引（'' 与 '' 冲突）
        user.setPhone(normalizeBlankToNull(command.getPhone()));
        user.setEmail(normalizeBlankToNull(command.getEmail()));
        user.setStatus(command.getStatus());
        user.setEnrollmentYear(command.getEnrollmentYear());
        user.setGraduationYear(command.getGraduationYear());
        user.setGraduationDest(command.getGraduationDest());
        user.setResume(command.getResume());
        user.setHomepageUrl(command.getHomepageUrl());
        user.setOrcid(command.getOrcid());
        user.setIsActive(command.getIsActive());

        // 设置创建人
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        if (loginUser != null) {
            user.setCreatorId(loginUser.getUserId());
        }

        labUserService.save(user);
        return user.getId();
    }

    /**
     * 更新用户信息（管理员）
     *
     * @param command 更新命令
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UpdateLabUserCommand command) {
        LabUserEntity user = labUserService.getById(command.getId());
        if (user == null) {
            throw new ApiException(ErrorCode.Business.USER_NON_EXIST, command.getId().toString());
        }

        // 验证学号唯一性
        if (StringUtils.hasText(command.getStudentNumber()) &&
            labUserService.isStudentNumberDuplicated(command.getStudentNumber(), command.getId())) {
            throw new ApiException(ErrorCode.Business.USER_NAME_IS_NOT_UNIQUE);
        }

        // 验证邮箱唯一性
        if (StringUtils.hasText(command.getEmail()) &&
            labUserService.isEmailDuplicated(command.getEmail(), command.getId())) {
            throw new ApiException(ErrorCode.Business.USER_EMAIL_IS_NOT_UNIQUE);
        }

        // 验证手机号唯一性
        if (StringUtils.hasText(command.getPhone()) &&
            labUserService.isPhoneDuplicated(command.getPhone(), command.getId())) {
            throw new ApiException(ErrorCode.Business.USER_PHONE_NUMBER_IS_NOT_UNIQUE);
        }

        // 更新用户信息
        user.setStudentNumber(normalizeBlankToNull(command.getStudentNumber()));
        user.setRealName(command.getRealName());
        user.setEnglishName(command.getEnglishName());
        user.setGender(command.getGender());
        user.setIdentity(command.getIdentity());
        user.setAcademicStatus(command.getAcademicStatus());
        user.setResearchArea(command.getResearchArea());
        user.setPhone(normalizeBlankToNull(command.getPhone()));
        user.setEmail(normalizeBlankToNull(command.getEmail()));
        user.setStatus(command.getStatus());
        user.setEnrollmentYear(command.getEnrollmentYear());
        user.setGraduationYear(command.getGraduationYear());
        user.setGraduationDest(command.getGraduationDest());
        user.setResume(command.getResume());
        user.setHomepageUrl(command.getHomepageUrl());
        user.setOrcid(command.getOrcid());
        user.setIsActive(command.getIsActive());

        // 设置更新人
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        if (loginUser != null) {
            user.setUpdaterId(loginUser.getUserId());
        }

        labUserService.updateById(user);
    }

    /**
     * 更新个人信息
     *
     * @param command 更新命令
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(UpdateProfileCommand command) {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        if (loginUser == null) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION);
        }

        LabUserEntity user = labUserService.getByUsername(loginUser.getUsername());
        if (user == null) {
            throw new ApiException(ErrorCode.Business.USER_NON_EXIST, loginUser.getUsername());
        }

        // 验证邮箱唯一性
        if (StringUtils.hasText(command.getEmail()) &&
            labUserService.isEmailDuplicated(command.getEmail(), user.getId())) {
            throw new ApiException(ErrorCode.Business.USER_EMAIL_IS_NOT_UNIQUE);
        }

        // 验证手机号唯一性
        if (StringUtils.hasText(command.getPhone()) &&
            labUserService.isPhoneDuplicated(command.getPhone(), user.getId())) {
            throw new ApiException(ErrorCode.Business.USER_PHONE_NUMBER_IS_NOT_UNIQUE);
        }

        // 更新个人信息
        user.setRealName(command.getRealName());
        user.setEnglishName(command.getEnglishName());
        user.setGender(command.getGender());
        user.setAcademicStatus(command.getAcademicStatus());
        user.setResearchArea(command.getResearchArea());
        user.setPhone(normalizeBlankToNull(command.getPhone()));
        user.setEmail(normalizeBlankToNull(command.getEmail()));
        user.setGraduationYear(command.getGraduationYear());
        user.setGraduationDest(command.getGraduationDest());
        user.setResume(command.getResume());
        user.setHomepageUrl(command.getHomepageUrl());
        user.setOrcid(command.getOrcid());

        user.setUpdaterId(loginUser.getUserId());
        labUserService.updateById(user);
    }

    /**
     * 修改密码
     *
     * @param command 修改密码命令
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordCommand command) {
        // 验证新密码和确认密码是否一致
        if (!command.getNewPassword().equals(command.getConfirmPassword())) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "新密码和确认密码不一致");
        }

        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        if (loginUser == null) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION);
        }

        LabUserEntity user = labUserService.getByUsername(loginUser.getUsername());
        if (user == null) {
            throw new ApiException(ErrorCode.Business.USER_NON_EXIST, loginUser.getUsername());
        }

        // 验证原密码
        if (!passwordEncoder.matches(command.getOldPassword(), user.getPassword())) {
            throw new ApiException(ErrorCode.Business.USER_PASSWORD_IS_NOT_CORRECT);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(command.getNewPassword()));
        user.setUpdaterId(loginUser.getUserId());
        labUserService.updateById(user);
    }


    /**
     * 管理员重置用户密码（无需旧密码）
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordByAdmin(ResetLabUserPasswordCommand command) {
        LabUserEntity user = labUserService.getById(command.getUserId());
        if (user == null) {
            throw new ApiException(ErrorCode.Business.USER_NON_EXIST, String.valueOf(command.getUserId()));
        }
        user.setPassword(passwordEncoder.encode(command.getPassword()));
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        if (loginUser != null) {
            user.setUpdaterId(loginUser.getUserId());
        }
        labUserService.updateById(user);
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        LabUserEntity user = labUserService.getById(userId);
        if (user == null) {
            throw new ApiException(ErrorCode.Business.USER_NON_EXIST, userId.toString());
        }

        // 硬删除（绕过逻辑删除）
        labUserService.hardDeleteById(userId);
    }

    /**
     * 分页查询用户列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    public PageDTO<LabUserProfileDTO> getUserList(LabUserQuery query) {
        if (query == null) {
            query = new LabUserQuery();
        }
        Page<LabUserEntity> page = query.toPage();
        IPage<LabUserEntity> result = labUserService.page(page, query.addQueryCondition());

        // 转换为DTO（防御空records）
        List<LabUserEntity> records = result.getRecords();
        if (records == null) {
            records = new ArrayList<>();
        }
        List<LabUserProfileDTO> dtoList = records.stream()
                .map(LabUserProfileDTO::new)
                .collect(Collectors.toList());

        return new PageDTO<>(dtoList, result.getTotal());
    }

    /**
     * 搜索用户
     *
     * @param keyword 关键词
     * @return 用户列表
     */
    public List<LabUserProfileDTO> searchUsers(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<LabUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.like(LabUserEntity::getRealName, keyword)
                         .or().like(LabUserEntity::getUsername, keyword)
                         .or().like(LabUserEntity::getEmail, keyword)
                         .or().like(LabUserEntity::getEnglishName, keyword))
               .eq(LabUserEntity::getDeleted, false)
               .eq(LabUserEntity::getIsActive, true)
               .orderByDesc(LabUserEntity::getCreateTime)
               .last("LIMIT 20"); // 限制返回数量

        List<LabUserEntity> users = labUserService.list(wrapper);
        return users.stream()
                .map(LabUserProfileDTO::new)
                .collect(Collectors.toList());
    }
}
