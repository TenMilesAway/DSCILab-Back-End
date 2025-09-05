package com.agileboot.domain.lab.achievement.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 成果作者 Service 实现
 */
@Service
public class LabAchievementAuthorServiceImpl extends ServiceImpl<LabAchievementAuthorMapper, LabAchievementAuthorEntity>
    implements LabAchievementAuthorService {

    @Override
    public List<LabAchievementAuthorEntity> getAuthorsByAchievementId(Long achievementId) {
        return lambdaQuery()
            .eq(LabAchievementAuthorEntity::getAchievementId, achievementId)
            .eq(LabAchievementAuthorEntity::getDeleted, false)
            .orderByAsc(LabAchievementAuthorEntity::getAuthorOrder)
            .list();
    }

    @Override
    public java.util.List<LabAchievementAuthorEntity> getAuthorsByAchievementIds(java.util.List<Long> achievementIds) {
        if (achievementIds == null || achievementIds.isEmpty()) return java.util.Collections.emptyList();
        return lambdaQuery()
            .in(LabAchievementAuthorEntity::getAchievementId, achievementIds)
            .eq(LabAchievementAuthorEntity::getDeleted, false)
            .orderByAsc(LabAchievementAuthorEntity::getAchievementId)
            .orderByAsc(LabAchievementAuthorEntity::getAuthorOrder)
            .list();
    }

    @Override
    public boolean isAuthor(Long achievementId, Long userId) {
        return lambdaQuery()
            .eq(LabAchievementAuthorEntity::getAchievementId, achievementId)
            .eq(LabAchievementAuthorEntity::getUserId, userId)
            .eq(LabAchievementAuthorEntity::getDeleted, false)
            .exists();
    }

    @Override
    public LabAchievementAuthorEntity getAuthorRecord(Long achievementId, Long userId) {
        return lambdaQuery()
            .eq(LabAchievementAuthorEntity::getAchievementId, achievementId)
            .eq(LabAchievementAuthorEntity::getUserId, userId)
            .eq(LabAchievementAuthorEntity::getDeleted, false)
            .one();
    }

    @Override
    public boolean isAuthorOrderExists(Long achievementId, Integer authorOrder, Long excludeAuthorId) {
        return lambdaQuery()
            .eq(LabAchievementAuthorEntity::getAchievementId, achievementId)
            .eq(LabAchievementAuthorEntity::getAuthorOrder, authorOrder)
            .ne(excludeAuthorId != null, LabAchievementAuthorEntity::getId, excludeAuthorId)
            .eq(LabAchievementAuthorEntity::getDeleted, false)
            .exists();
    }

    @Override
    public int hardDeleteDeletedByAchievementId(Long achievementId) {
        // 直接物理删除 deleted=1 的历史作者（使用自定义 Mapper SQL）
        return this.baseMapper.hardDeleteDeletedByAchievementId(achievementId);
    }
}
