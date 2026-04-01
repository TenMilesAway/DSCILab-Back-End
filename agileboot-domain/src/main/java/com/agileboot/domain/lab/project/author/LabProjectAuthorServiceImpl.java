package com.agileboot.domain.lab.project.author;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class LabProjectAuthorServiceImpl
    extends ServiceImpl<LabProjectAuthorMapper, LabProjectAuthorEntity>
    implements LabProjectAuthorService {

    @Override
    public List<LabProjectAuthorEntity> getAuthorsByProjectIds(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Collections.emptyList();
        }
        return lambdaQuery()
            .in(LabProjectAuthorEntity::getProjectId, projectIds)
            .eq(LabProjectAuthorEntity::getDeleted, false)
            .orderByAsc(LabProjectAuthorEntity::getProjectId, LabProjectAuthorEntity::getAuthorOrder)
            .list();
    }

    @Override
    public List<LabProjectAuthorEntity> getAuthorsByProjectId(Long projectId) {
        if (projectId == null) {
            return Collections.emptyList();
        }
        return lambdaQuery()
            .eq(LabProjectAuthorEntity::getProjectId, projectId)
            .eq(LabProjectAuthorEntity::getDeleted, false)
            .orderByAsc(LabProjectAuthorEntity::getAuthorOrder)
            .list();
    }

    @Override
    public boolean isMember(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return false;
        }
        return lambdaQuery()
            .eq(LabProjectAuthorEntity::getProjectId, projectId)
            .eq(LabProjectAuthorEntity::getUserId, userId)
            .eq(LabProjectAuthorEntity::getDeleted, false)
            .count() > 0;
    }
}
