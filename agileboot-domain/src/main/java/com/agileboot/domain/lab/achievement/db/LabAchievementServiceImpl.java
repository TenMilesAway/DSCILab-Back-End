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

    private final LabAchievementAuthorService authorService;

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

        // 检查是否为拥有者
        if (isOwner(achievementId, userId)) {
            return true;
        }

        // 允许第一负责人（第一作者，author_order=1 且内部作者）编辑
        LabAchievementAuthorEntity rec = authorService.getAuthorRecord(achievementId, userId);
        if (rec != null && Boolean.FALSE.equals(rec.getDeleted()) && Integer.valueOf(1).equals(rec.getAuthorOrder())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isOwner(Long achievementId, Long userId) {
        LabAchievementEntity achievement = getByIdNotDeleted(achievementId);
        return achievement != null && userId.equals(achievement.getOwnerUserId());
    }
}
