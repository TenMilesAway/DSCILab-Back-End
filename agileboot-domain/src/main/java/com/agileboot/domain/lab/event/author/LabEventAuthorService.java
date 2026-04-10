package com.agileboot.domain.lab.event.author;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface LabEventAuthorService extends IService<LabEventAuthorEntity> {

    List<LabEventAuthorEntity> getAuthorsByEventIds(List<Long> eventIds);

    boolean isAuthor(Long eventId, Long userId);
}
