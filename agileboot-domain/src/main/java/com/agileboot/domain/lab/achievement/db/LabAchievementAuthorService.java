package com.agileboot.domain.lab.achievement.db;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 成果作者 Service
 */
public interface LabAchievementAuthorService extends IService<LabAchievementAuthorEntity> {

    /**
     * 获取成果的所有作者（按顺序）
     */
    List<LabAchievementAuthorEntity> getAuthorsByAchievementId(Long achievementId);

    /**
     * 批量获取多个成果的作者（按顺序）
     */
    List<LabAchievementAuthorEntity> getAuthorsByAchievementIds(java.util.List<Long> achievementIds);

    /**
     * 检查用户是否为该成果的作者
     */
    boolean isAuthor(Long achievementId, Long userId);

    /**
     * 获取用户在该成果中的作者记录
     */
    LabAchievementAuthorEntity getAuthorRecord(Long achievementId, Long userId);

    /**
     * 检查作者顺序是否已存在
     */
    boolean isAuthorOrderExists(Long achievementId, Integer authorOrder, Long excludeAuthorId);

    /**
     * 物理删除：清理指定成果下已软删(deleted=1)的作者记录
     */
    int hardDeleteDeletedByAchievementId(Long achievementId);

    /**
     * 物理删除：删除指定成果下指定用户的所有作者记录（包括软删的）
     */
    int hardDeleteByAchievementIdAndUserId(Long achievementId, Long userId);
}
