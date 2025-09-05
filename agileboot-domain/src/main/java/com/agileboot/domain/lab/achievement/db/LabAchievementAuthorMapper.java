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
}
