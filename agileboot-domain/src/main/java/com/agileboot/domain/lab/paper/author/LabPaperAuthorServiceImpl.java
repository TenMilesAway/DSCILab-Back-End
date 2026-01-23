package com.agileboot.domain.lab.paper.author;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabPaperAuthorServiceImpl
    extends ServiceImpl<LabPaperAuthorMapper, LabPaperAuthorEntity>
    implements LabPaperAuthorService {

    @Override
    public List<LabPaperAuthorEntity> getAuthorsByPaperId(Long paperId) {
        return lambdaQuery()
            .eq(LabPaperAuthorEntity::getPaperId, paperId)
            .eq(LabPaperAuthorEntity::getDeleted, false)
            .orderByAsc(LabPaperAuthorEntity::getAuthorOrder)
            .list();
    }

    @Override
    public List<LabPaperAuthorEntity> getAuthorsByPaperIds(List<Long> paperIds) {
        if (paperIds == null || paperIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return lambdaQuery()
            .in(LabPaperAuthorEntity::getPaperId, paperIds)
            .eq(LabPaperAuthorEntity::getDeleted, false)
            .orderByAsc(LabPaperAuthorEntity::getPaperId)
            .orderByAsc(LabPaperAuthorEntity::getAuthorOrder)
            .list();
    }

    @Override
    public LabPaperAuthorEntity getAuthorRecord(Long paperId, Long userId) {
        if (paperId == null || userId == null) {
            return null;
        }
        return lambdaQuery()
            .eq(LabPaperAuthorEntity::getPaperId, paperId)
            .eq(LabPaperAuthorEntity::getUserId, userId)
            .eq(LabPaperAuthorEntity::getDeleted, false)
            .one();
    }

    @Override
    public boolean existsOrder(Long paperId, Integer authorOrder, Long excludeId) {
        if (paperId == null || authorOrder == null) {
            return false;
        }
        LambdaQueryWrapper<LabPaperAuthorEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabPaperAuthorEntity::getPaperId, paperId)
            .eq(LabPaperAuthorEntity::getAuthorOrder, authorOrder)
            .eq(LabPaperAuthorEntity::getDeleted, false);
        if (excludeId != null) {
            wrapper.ne(LabPaperAuthorEntity::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    @Override
    public boolean isAuthor(Long paperId, Long userId) {
        if (paperId == null || userId == null) {
            return false;
        }
        return lambdaQuery()
            .eq(LabPaperAuthorEntity::getPaperId, paperId)
            .eq(LabPaperAuthorEntity::getUserId, userId)
            .eq(LabPaperAuthorEntity::getDeleted, false)
            .count() > 0;
    }

    @Override
    public int hardDeleteDeletedByPaperId(Long paperId) {
        if (paperId == null) {
            return 0;
        }
        return baseMapper.delete(new LambdaQueryWrapper<LabPaperAuthorEntity>()
            .eq(LabPaperAuthorEntity::getPaperId, paperId)
            .eq(LabPaperAuthorEntity::getDeleted, true));
    }

    @Override
    public int hardDeleteByPaperIdAndUserId(Long paperId, Long userId) {
        if (paperId == null || userId == null) {
            return 0;
        }
        return baseMapper.delete(new LambdaQueryWrapper<LabPaperAuthorEntity>()
            .eq(LabPaperAuthorEntity::getPaperId, paperId)
            .eq(LabPaperAuthorEntity::getUserId, userId));
    }

    @Override
    public int hardDeleteAllByPaperId(Long paperId) {
        if (paperId == null) {
            return 0;
        }
        return baseMapper.delete(new LambdaQueryWrapper<LabPaperAuthorEntity>()
            .eq(LabPaperAuthorEntity::getPaperId, paperId));
    }
}
