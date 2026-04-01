package com.agileboot.domain.lab.achievement.db;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 实验室成果 Service
 */
public interface LabAchievementService extends IService<LabAchievementEntity> {
    
    /**
     * 根据ID获取成果（包含软删除检查）
     */
    LabAchievementEntity getByIdNotDeleted(Long id);
    
    /**
     * 检查用户是否有权限编辑该成果
     */
    boolean canEdit(Long achievementId, Long userId, boolean isAdmin);
    
    /**
     * 检查用户是否拥有该成果
     */
    boolean isOwner(Long achievementId, Long userId);
}
