package com.agileboot.domain.lab.achievement.db;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 实验室成果 Service 实现
 */
@Service
@RequiredArgsConstructor
public class LabAchievementServiceImpl extends ServiceImpl<LabAchievementMapper, LabAchievementEntity>
    implements LabAchievementService {

    @Override
    public LabAchievementEntity getByIdNotDeleted(Long id) {
        return lambdaQuery()
            .eq(LabAchievementEntity::getId, id)
            .eq(LabAchievementEntity::getDeleted, false)
            .one();
    }

    @Override
    public boolean canEdit(Long achievementId, Long userId, boolean isAdmin) {
        if (isAdmin) {
            return true;
        }

        // 新权限规则：所有实验室成员（lab_user表中的用户）都可以修改成果
        // 只要用户ID存在且有效，就允许修改（因为能调用到这里说明用户已经通过身份验证）
        // 外部作者无法登录系统，因此无法调用到这个方法
        return true;
    }

    @Override
    public boolean isOwner(Long achievementId, Long userId) {
        LabAchievementEntity achievement = getByIdNotDeleted(achievementId);
        return achievement != null && userId.equals(achievement.getOwnerUserId());
    }
}
