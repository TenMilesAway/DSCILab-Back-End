package com.agileboot.domain.lab.user.db;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;

/**
 * 实验室用户Mapper接口
 *
 * @author agileboot
 */
@Mapper
public interface LabUserMapper extends BaseMapper<LabUserEntity> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    LabUserEntity selectByUsername(@Param("username") String username);

    /**
     * 根据学号/工号查询用户
     *
     * @param studentNumber 学号/工号
     * @return 用户信息
     */
    LabUserEntity selectByStudentNumber(@Param("studentNumber") String studentNumber);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    LabUserEntity selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    LabUserEntity selectByPhone(@Param("phone") String phone);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @param excludeId 排除的用户ID
     * @return 是否存在
     */
    boolean existsByUsername(@Param("username") String username, @Param("excludeId") Long excludeId);

    /**
     * 检查学号/工号是否存在
     *
     * @param studentNumber 学号/工号
     * @param excludeId 排除的用户ID
     * @return 是否存在
     */
    boolean existsByStudentNumber(@Param("studentNumber") String studentNumber, @Param("excludeId") Long excludeId);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @param excludeId 排除的用户ID
     * @return 是否存在
     */
    boolean existsByEmail(@Param("email") String email, @Param("excludeId") Long excludeId);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @param excludeId 排除的用户ID
     * @return 是否存在
     */
    boolean existsByPhone(@Param("phone") String phone, @Param("excludeId") Long excludeId);
    /**
     * 根据身份统计用户数量
     *
     * @return 身份统计结果
     */
    java.util.List<java.util.Map<String, Object>> countByIdentity();

    /**
     * 根据学术身份统计用户数量
     *
     * @return 学术身份统计结果
     */
    java.util.List<java.util.Map<String, Object>> countByAcademicStatus();

    /**
     * 根据状态统计用户数量
     *
     * @return 状态统计结果
     */

    /**
     * 物理删除用户
     */
    @Delete("DELETE FROM lab_user WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);

    java.util.List<java.util.Map<String, Object>> countByStatus();
}
