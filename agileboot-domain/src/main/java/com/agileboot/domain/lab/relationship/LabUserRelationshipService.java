package com.agileboot.domain.lab.relationship;

import com.agileboot.domain.lab.relationship.db.LabUserRelationshipEntity;
import com.agileboot.domain.lab.relationship.db.LabUserRelationshipMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 实验室师生关系服务类
 *
 * @author agileboot
 */
@Service
@RequiredArgsConstructor
public class LabUserRelationshipService extends ServiceImpl<LabUserRelationshipMapper, LabUserRelationshipEntity> {

    /**
     * 查询导师的所有学生
     *
     * @param teacherId 导师ID
     * @param status 关系状态，null表示查询所有状态
     * @return 师生关系列表
     */
    public List<LabUserRelationshipEntity> getStudentsByTeacherId(Long teacherId, Integer status) {
        return baseMapper.selectStudentsByTeacherId(teacherId, status);
    }

    /**
     * 查询学生的导师
     *
     * @param studentId 学生ID
     * @param status 关系状态，null表示查询所有状态
     * @return 师生关系
     */
    public LabUserRelationshipEntity getSupervisorByStudentId(Long studentId, Integer status) {
        return baseMapper.selectSupervisorByStudentId(studentId, status);
    }

    /**
     * 检查师生关系是否存在
     *
     * @param teacherId 导师ID
     * @param studentId 学生ID
     * @param status 关系状态，null表示查询所有状态
     * @return 师生关系
     */
    public LabUserRelationshipEntity getByTeacherAndStudent(Long teacherId, Long studentId, Integer status) {
        return baseMapper.selectByTeacherAndStudent(teacherId, studentId, status);
    }

    /**
     * 结束学生的所有活跃导师关系
     *
     * @param studentId 学生ID
     * @param updaterId 更新者ID
     * @return 更新的记录数
     */
    public int endActiveRelationshipsByStudentId(Long studentId, Long updaterId) {
        return baseMapper.endActiveRelationshipsByStudentId(studentId, updaterId);
    }

    /**
     * 统计导师的学生数量
     *
     * @param teacherId 导师ID
     * @param status 关系状态，null表示查询所有状态
     * @return 学生数量
     */
    public int countStudentsByTeacherId(Long teacherId, Integer status) {
        return baseMapper.countStudentsByTeacherId(teacherId, status);
    }

    /**
     * 检查师生关系是否存在（活跃状态）
     *
     * @param teacherId 导师ID
     * @param studentId 学生ID
     * @return 是否存在活跃的师生关系
     */
    public boolean existsActiveRelationship(Long teacherId, Long studentId) {
        LabUserRelationshipEntity relationship = getByTeacherAndStudent(teacherId, studentId, 
            LabUserRelationshipEntity.Status.ACTIVE.getCode());
        return relationship != null;
    }

    /**
     * 检查学生是否已有活跃的导师
     *
     * @param studentId 学生ID
     * @return 是否已有活跃的导师
     */
    public boolean hasActiveSupervisor(Long studentId) {
        LabUserRelationshipEntity relationship = getSupervisorByStudentId(studentId, 
            LabUserRelationshipEntity.Status.ACTIVE.getCode());
        return relationship != null;
    }
}
