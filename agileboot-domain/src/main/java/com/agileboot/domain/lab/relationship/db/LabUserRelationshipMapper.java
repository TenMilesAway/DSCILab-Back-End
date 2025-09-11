package com.agileboot.domain.lab.relationship.db;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 实验室师生关系 Mapper 接口
 *
 * @author agileboot
 */
@Mapper
public interface LabUserRelationshipMapper extends BaseMapper<LabUserRelationshipEntity> {

    /**
     * 查询导师的所有学生
     *
     * @param teacherId 导师ID
     * @param status 关系状态，null表示查询所有状态
     * @return 师生关系列表
     */
    List<LabUserRelationshipEntity> selectStudentsByTeacherId(@Param("teacherId") Long teacherId, 
                                                               @Param("status") Integer status);

    /**
     * 查询学生的导师
     *
     * @param studentId 学生ID
     * @param status 关系状态，null表示查询所有状态
     * @return 师生关系
     */
    LabUserRelationshipEntity selectSupervisorByStudentId(@Param("studentId") Long studentId, 
                                                           @Param("status") Integer status);

    /**
     * 检查师生关系是否存在
     *
     * @param teacherId 导师ID
     * @param studentId 学生ID
     * @param status 关系状态，null表示查询所有状态
     * @return 师生关系
     */
    LabUserRelationshipEntity selectByTeacherAndStudent(@Param("teacherId") Long teacherId,
                                                         @Param("studentId") Long studentId,
                                                         @Param("status") Integer status);

    /**
     * 结束学生的所有活跃导师关系（用于更换导师时）
     *
     * @param studentId 学生ID
     * @param updaterId 更新者ID
     * @return 更新的记录数
     */
    int endActiveRelationshipsByStudentId(@Param("studentId") Long studentId, 
                                          @Param("updaterId") Long updaterId);

    /**
     * 统计导师的学生数量
     *
     * @param teacherId 导师ID
     * @param status 关系状态，null表示查询所有状态
     * @return 学生数量
     */
    int countStudentsByTeacherId(@Param("teacherId") Long teacherId, 
                                 @Param("status") Integer status);
}
