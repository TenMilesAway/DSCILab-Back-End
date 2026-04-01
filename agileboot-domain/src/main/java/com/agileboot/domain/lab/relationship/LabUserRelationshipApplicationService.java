package com.agileboot.domain.lab.relationship;

import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.domain.lab.relationship.command.CreateUserRelationshipCommand;
import com.agileboot.domain.lab.relationship.db.LabUserRelationshipEntity;
import com.agileboot.domain.lab.relationship.dto.UserRelationshipDTO;
import com.agileboot.domain.lab.user.db.LabUserService;
import com.agileboot.domain.lab.user.db.LabUserEntity;
import com.agileboot.domain.lab.user.dto.PublicLabUserDTO;
import com.agileboot.infrastructure.user.web.SystemLoginUser;
import com.agileboot.infrastructure.user.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 师生关系应用服务
 *
 * @author agileboot
 */
@Service
@RequiredArgsConstructor
public class LabUserRelationshipApplicationService {

    private final LabUserRelationshipService relationshipService;
    private final LabUserService userService;

    /**
     * 创建师生关系
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createRelationship(CreateUserRelationshipCommand command) {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        if (loginUser == null) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION);
        }

        // 验证导师和学生是否存在
        LabUserEntity teacher = userService.getById(command.getTeacherId());
        if (teacher == null || teacher.getDeleted()) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "导师");
        }

        LabUserEntity student = userService.getById(command.getStudentId());
        if (student == null || student.getDeleted()) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "学生");
        }

        // 验证身份
        if (!LabUserEntity.Identity.TEACHER.getCode().equals(teacher.getIdentity())) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "用户身份必须是教师才能作为导师");
        }

        if (!LabUserEntity.Identity.STUDENT.getCode().equals(student.getIdentity())) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "用户身份必须是学生才能被指导");
        }

        // 检查是否已存在活跃的师生关系
        if (relationshipService.existsActiveRelationship(command.getTeacherId(), command.getStudentId())) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "该师生关系已存在");
        }

        // 检查学生是否已有活跃的导师（一个学生同时只能有一个导师）
        if (relationshipService.hasActiveSupervisor(command.getStudentId())) {
            throw new ApiException(ErrorCode.Client.COMMON_REQUEST_PARAMETERS_INVALID, "该学生已有导师，请先解除现有师生关系");
        }

        // 创建师生关系
        LabUserRelationshipEntity entity = new LabUserRelationshipEntity();
        entity.setTeacherId(command.getTeacherId());
        entity.setStudentId(command.getStudentId());
        entity.setRelationshipType(command.getRelationshipType());
        entity.setStartDate(command.getStartDate() != null ? command.getStartDate() : LocalDate.now());
        entity.setStatus(LabUserRelationshipEntity.Status.ACTIVE.getCode());
        entity.setRemark(command.getRemark());
        entity.setCreatorId(loginUser.getUserId());

        relationshipService.save(entity);
        return entity.getId();
    }

    /**
     * 解除师生关系（硬删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void endRelationship(Long teacherId, Long studentId) {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        if (loginUser == null) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION);
        }

        // 查找活跃的师生关系
        LabUserRelationshipEntity relationship = relationshipService.getByTeacherAndStudent(
            teacherId, studentId, LabUserRelationshipEntity.Status.ACTIVE.getCode());

        if (relationship == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "", "师生关系");
        }

        // 硬删除关系记录
        relationshipService.removeById(relationship.getId());
    }

    /**
     * 获取导师的学生列表
     */
    public List<UserRelationshipDTO> getStudentsByTeacherId(Long teacherId, Integer status) {
        List<LabUserRelationshipEntity> relationships = relationshipService.getStudentsByTeacherId(teacherId, status);

        return relationships.stream().map(relationship -> {
            UserRelationshipDTO dto = UserRelationshipDTO.fromEntity(relationship);

            // 填充学生信息
            LabUserEntity student = userService.getById(relationship.getStudentId());
            if (student != null) {
                dto.setStudent(PublicLabUserDTO.fromEntity(student));
            }

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 获取学生的导师信息
     */
    public UserRelationshipDTO getSupervisorByStudentId(Long studentId, Integer status) {
        LabUserRelationshipEntity relationship = relationshipService.getSupervisorByStudentId(studentId, status);
        if (relationship == null) {
            return null;
        }

        UserRelationshipDTO dto = UserRelationshipDTO.fromEntity(relationship);

        // 填充导师信息
        LabUserEntity teacher = userService.getById(relationship.getTeacherId());
        if (teacher != null) {
            dto.setTeacher(PublicLabUserDTO.fromEntity(teacher));
        }

        return dto;
    }

    /**
     * 更换学生的导师
     */
    @Transactional(rollbackFor = Exception.class)
    public Long changeSupervisor(Long studentId, Long newTeacherId, String remark) {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        if (loginUser == null) {
            throw new ApiException(ErrorCode.Client.COMMON_NO_AUTHORIZATION);
        }

        // 结束学生的所有活跃导师关系
        relationshipService.endActiveRelationshipsByStudentId(studentId, loginUser.getUserId());

        // 创建新的师生关系
        CreateUserRelationshipCommand command = new CreateUserRelationshipCommand();
        command.setTeacherId(newTeacherId);
        command.setStudentId(studentId);
        command.setRemark(remark);

        return createRelationship(command);
    }
}
