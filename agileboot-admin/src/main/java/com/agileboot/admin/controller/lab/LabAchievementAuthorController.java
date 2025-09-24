package com.agileboot.admin.controller.lab;

import com.agileboot.admin.customize.aop.accessLog.AccessLog;
import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.lab.achievement.command.AddAuthorCommand;
import com.agileboot.domain.lab.achievement.command.UpdateAuthorCommand;
import com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity;
import com.agileboot.domain.lab.achievement.db.LabAchievementAuthorService;
import com.agileboot.domain.lab.achievement.db.LabAchievementService;
import com.agileboot.domain.lab.user.LabUserPermissionChecker;
import com.agileboot.domain.lab.user.db.LabUserEntity;
import com.agileboot.domain.lab.user.db.LabUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 成果作者管理
 */
@Tag(name = "成果作者", description = "成果作者管理接口")
@RestController
@RequestMapping("/lab/achievements/{achievementId}/authors")
@RequiredArgsConstructor
public class LabAchievementAuthorController extends BaseController {

    private final LabAchievementService achievementService;
    private final LabAchievementAuthorService authorService;
    private final LabUserPermissionChecker labUserPermission;
    private final LabUserService labUserService;

    private void checkEditPermission(Long achievementId) {
        LabUserEntity current = labUserPermission.getCurrentLabUser();
        if (current == null) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION, "/lab/achievements" );
        }
        boolean isAdmin = labUserPermission.isAdmin();
        boolean isOwner = achievementService.isOwner(achievementId, current.getId());
        if (!isAdmin && !isOwner) {
            throw new ApiException(ErrorCode.Business.PERMISSION_NOT_ALLOWED_TO_OPERATE);
        }
    }

    @Operation(summary = "作者列表", description = "获取指定成果的作者列表，按作者顺序排序")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "403", description = "权限不足"),
        @ApiResponse(responseCode = "404", description = "成果不存在")
    })
    @GetMapping
    @PreAuthorize("@permission.has('lab:achievement:query')")
    @AccessLog(title = "成果作者")
    public ResponseDTO<List<LabAchievementAuthorEntity>> list(
        @Parameter(description = "成果ID", required = true) @PathVariable Long achievementId) {
        List<LabAchievementAuthorEntity> list = authorService.getAuthorsByAchievementId(achievementId);
        return ResponseDTO.ok(list);
    }

    @Operation(summary = "新增作者", description = "为指定成果添加作者，支持内部作者和外部作者")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "添加成功，返回作者ID"),
        @ApiResponse(responseCode = "400", description = "参数校验失败或作者顺序冲突"),
        @ApiResponse(responseCode = "403", description = "权限不足"),
        @ApiResponse(responseCode = "404", description = "成果不存在")
    })
    @PostMapping
    @PreAuthorize("@permission.has('lab:achievement:edit')")
    @AccessLog(title = "成果作者")
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> add(@PathVariable Long achievementId, @Validated @RequestBody AddAuthorCommand cmd) {
        checkEditPermission(achievementId);

        // 校验内部/外部作者
        if (cmd.getUserId() == null && (cmd.getName() == null || cmd.getName().trim().isEmpty())) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "外部作者必须提供姓名");
        }
        // 校验顺序唯一
        if (authorService.isAuthorOrderExists(achievementId, cmd.getAuthorOrder(), null)) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "作者顺序已存在");
        }

        // 自动绑定：当未提供 userId 时，优先按唯一键(username/email/phone/studentNumber)解析，再退回到唯一姓名匹配
        if (cmd.getUserId() == null) {
            LabUserEntity matched = null;
            String uname = cmd.getUsername() == null ? null : cmd.getUsername().trim();
            String mail = cmd.getEmail() == null ? null : cmd.getEmail().trim();

            // 同时提供 username 与 email 时，要求二者一致才绑定
            if (uname != null && !uname.isEmpty() && mail != null && !mail.isEmpty()) {
                LabUserEntity byUsername = labUserService.getByUsername(uname);
                if (byUsername != null && mail.equalsIgnoreCase(byUsername.getEmail())) {
                    matched = byUsername;
                } else {
                    LabUserEntity byEmail = labUserService.getByEmail(mail);
                    if (byEmail != null && uname.equalsIgnoreCase(byEmail.getUsername())) {
                        matched = byEmail;
                    } else {
                        throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "用户名与邮箱不一致，无法唯一绑定内部作者");
                    }
                }
            }

            if (matched == null && uname != null && !uname.isEmpty()) {
                matched = labUserService.getByUsername(uname);
            }
            if (matched == null && mail != null && !mail.isEmpty()) {
                matched = labUserService.getByEmail(mail);
            }
            if (matched == null && cmd.getPhone() != null && !cmd.getPhone().trim().isEmpty()) {
                matched = labUserService.getByPhone(cmd.getPhone().trim());
            }
            if (matched == null && cmd.getStudentNumber() != null && !cmd.getStudentNumber().trim().isEmpty()) {
                matched = labUserService.getByStudentNumber(cmd.getStudentNumber().trim());
            }
            if (matched == null) {
                matched = labUserService.getUniqueByRealName(cmd.getName());
                if (matched == null) {
                    // 检查是否存在重名用户
                    List<com.agileboot.domain.lab.user.db.LabUserEntity> duplicateUsers = labUserService.lambdaQuery()
                        .eq(com.agileboot.domain.lab.user.db.LabUserEntity::getRealName, cmd.getName())
                        .eq(com.agileboot.domain.lab.user.db.LabUserEntity::getDeleted, false)
                        .list();
                    if (duplicateUsers.size() > 1) {
                        // 存在重名用户，需要提供邮箱来区分
                        throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                            "存在多个名为 \"" + cmd.getName() + "\" 的用户，请提供邮箱以确定具体是哪一个用户");
                    }
                }
            }
            if (matched == null && cmd.getNameEn() != null) {
                matched = labUserService.getUniqueByEnglishName(cmd.getNameEn());
            }
            if (matched != null && !authorService.isAuthor(achievementId, matched.getId())) {
                // 验证填写的邮箱是否与匹配用户的实际邮箱一致
                if (mail != null && !mail.isEmpty() && matched.getEmail() != null) {
                    if (!mail.equalsIgnoreCase(matched.getEmail())) {
                        throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID,
                            "填写的邮箱与用户 " + matched.getRealName() + " 的实际邮箱不匹配，请确认后重新填写");
                    }
                }
                cmd.setUserId(matched.getId());
                // 如果自动绑定成功，使用用户的真实邮箱而不是填写的邮箱
                if (matched.getEmail() != null) {
                    cmd.setEmail(matched.getEmail());
                }
            }
        }

        // 若绑定的是内部作者，检查是否已存在（包含软删）。
        if (cmd.getUserId() != null) {
            LabAchievementAuthorEntity existed = authorService.lambdaQuery()
                .eq(LabAchievementAuthorEntity::getAchievementId, achievementId)
                .eq(LabAchievementAuthorEntity::getUserId, cmd.getUserId())
                .one();
            if (existed != null) {
                if (Boolean.FALSE.equals(existed.getDeleted())) {
                    throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "该内部作者已存在于该成果");
                } else {
                    // 复活软删记录，需校验顺序唯一（排除自身ID）
                    if (authorService.isAuthorOrderExists(achievementId, cmd.getAuthorOrder(), existed.getId())) {
                        throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "作者顺序已存在");
                    }
                    existed.setName(cmd.getName());
                    existed.setNameEn(cmd.getNameEn());
                    existed.setEmail(cmd.getEmail());
                    existed.setAffiliation(cmd.getAffiliation());
                    existed.setAuthorOrder(cmd.getAuthorOrder());
                    existed.setIsCorresponding(Boolean.TRUE.equals(cmd.getIsCorresponding()));
                    existed.setRole(cmd.getRole());
                    existed.setVisible(Boolean.TRUE.equals(cmd.getVisible()));
                    existed.setDeleted(false);
                    existed.setUpdateTime(new java.util.Date());
                    authorService.updateById(existed);
                    return ResponseDTO.ok(existed.getId());
                }
            }
        }

        LabAchievementAuthorEntity e = new LabAchievementAuthorEntity();
        e.setAchievementId(achievementId);
        e.setUserId(cmd.getUserId());
        e.setName(cmd.getName());
        e.setNameEn(cmd.getNameEn());
        e.setEmail(cmd.getEmail());
        e.setAffiliation(cmd.getAffiliation());
        e.setAuthorOrder(cmd.getAuthorOrder());
        e.setIsCorresponding(Boolean.TRUE.equals(cmd.getIsCorresponding()));
        e.setRole(cmd.getRole());
        e.setVisible(Boolean.TRUE.equals(cmd.getVisible()));
        e.setDeleted(false);
        authorService.save(e);
        return ResponseDTO.ok(e.getId());
    }

    @Operation(summary = "更新作者")
    @PutMapping("/{authorId}")
    @PreAuthorize("@permission.has('lab:achievement:edit')")
    @AccessLog(title = "成果作者")
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> update(@PathVariable Long achievementId, @PathVariable Long authorId, @Validated @RequestBody UpdateAuthorCommand cmd) {
        checkEditPermission(achievementId);
        LabAchievementAuthorEntity e = authorService.getById(authorId);
        if (e == null || Boolean.TRUE.equals(e.getDeleted()) || !achievementId.equals(e.getAchievementId())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "作者");
        }
        // 检查新顺序是否冲突
        if (cmd.getAuthorOrder() != null && authorService.isAuthorOrderExists(achievementId, cmd.getAuthorOrder(), authorId)) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "作者顺序已存在");
        }
        if (cmd.getUserId() != null) e.setUserId(cmd.getUserId());
        if (cmd.getName() != null) e.setName(cmd.getName());
        if (cmd.getNameEn() != null) e.setNameEn(cmd.getNameEn());
        if (cmd.getEmail() != null) e.setEmail(cmd.getEmail());
        if (cmd.getAffiliation() != null) e.setAffiliation(cmd.getAffiliation());
        if (cmd.getAuthorOrder() != null) e.setAuthorOrder(cmd.getAuthorOrder());
        if (cmd.getIsCorresponding() != null) e.setIsCorresponding(cmd.getIsCorresponding());
        if (cmd.getRole() != null) e.setRole(cmd.getRole());
        if (cmd.getVisible() != null) e.setVisible(cmd.getVisible());
        authorService.updateById(e);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除作者")
    @DeleteMapping("/{authorId}")
    @PreAuthorize("@permission.has('lab:achievement:edit')")
    @AccessLog(title = "成果作者")
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> delete(@PathVariable Long achievementId, @PathVariable Long authorId) {
        checkEditPermission(achievementId);
        LabAchievementAuthorEntity e = authorService.getById(authorId);
        if (e == null || Boolean.TRUE.equals(e.getDeleted()) || !achievementId.equals(e.getAchievementId())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "作者");
        }
        e.setDeleted(true);
        authorService.updateById(e);
        return ResponseDTO.ok();
    }

    @Operation(summary = "调整作者顺序")
    @PutMapping("/{authorId}/reorder")
    @PreAuthorize("@permission.has('lab:achievement:edit')")
    @AccessLog(title = "成果作者")
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> reorder(@PathVariable Long achievementId, @PathVariable Long authorId, @RequestParam Integer newOrder) {
        checkEditPermission(achievementId);
        if (newOrder == null || newOrder < 1) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "newOrder必须>=1");
        }
        if (authorService.isAuthorOrderExists(achievementId, newOrder, authorId)) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "作者顺序已存在");
        }
        LabAchievementAuthorEntity e = authorService.getById(authorId);
        if (e == null || Boolean.TRUE.equals(e.getDeleted()) || !achievementId.equals(e.getAchievementId())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "作者");
        }
        e.setAuthorOrder(newOrder);
        authorService.updateById(e);
        return ResponseDTO.ok();
    }

    @Operation(summary = "切换个人页可见性（仅内部作者生效）")
    @PutMapping("/{authorId}/visibility")
    @PreAuthorize("@permission.has('lab:achievement:edit')")
    @AccessLog(title = "成果作者")
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> toggleVisibility(@PathVariable Long achievementId, @PathVariable Long authorId, @RequestParam Boolean visible) {
        checkEditPermission(achievementId);
        LabAchievementAuthorEntity e = authorService.getById(authorId);
        if (e == null || Boolean.TRUE.equals(e.getDeleted()) || !achievementId.equals(e.getAchievementId())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "作者");
        }
        e.setVisible(Boolean.TRUE.equals(visible));
        authorService.updateById(e);
        return ResponseDTO.ok();
    }
}

