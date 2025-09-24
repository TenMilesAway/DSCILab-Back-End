package com.agileboot.domain.lab.achievement.db;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 成果作者 Mapper
 */
@Mapper
public interface LabAchievementAuthorMapper extends BaseMapper<LabAchievementAuthorEntity> {

    /**
     * 物理删除：清理指定成果下已软删(deleted=1)的作者记录
     */
    @Delete("DELETE FROM lab_achievement_author WHERE achievement_id = #{achievementId} AND deleted = 1")
    int hardDeleteDeletedByAchievementId(@Param("achievementId") Long achievementId);

    /**
     * 物理删除：删除指定成果下指定用户的所有作者记录（包括软删的）
     */
    @Delete("DELETE FROM lab_achievement_author WHERE achievement_id = #{achievementId} AND user_id = #{userId}")
    int hardDeleteByAchievementIdAndUserId(@Param("achievementId") Long achievementId, @Param("userId") Long userId);

    /**
     * 物理删除：删除指定成果的所有作者记录（用于成果删除时的级联删除）
     */
    @Delete("DELETE FROM lab_achievement_author WHERE achievement_id = #{achievementId}")
    int hardDeleteAllByAchievementId(@Param("achievementId") Long achievementId);
}
