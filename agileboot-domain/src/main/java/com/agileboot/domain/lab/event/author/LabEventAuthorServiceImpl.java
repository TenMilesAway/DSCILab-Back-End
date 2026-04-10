package com.agileboot.domain.lab.event.author;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class LabEventAuthorServiceImpl extends ServiceImpl<LabEventAuthorMapper, LabEventAuthorEntity>
    implements LabEventAuthorService {

    @Override
    public List<LabEventAuthorEntity> getAuthorsByEventIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        }
        return lambdaQuery()
            .in(LabEventAuthorEntity::getEventId, eventIds)
            .eq(LabEventAuthorEntity::getDeleted, false)
            .orderByAsc(LabEventAuthorEntity::getEventId, LabEventAuthorEntity::getAuthorOrder)
            .list();
    }

    @Override
    public boolean isAuthor(Long eventId, Long userId) {
        if (eventId == null || userId == null) {
            return false;
        }
        return lambdaQuery()
            .eq(LabEventAuthorEntity::getEventId, eventId)
            .eq(LabEventAuthorEntity::getUserId, userId)
            .eq(LabEventAuthorEntity::getDeleted, false)
            .count() > 0;
    }
}
