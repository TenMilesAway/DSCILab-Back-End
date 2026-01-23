package com.agileboot.domain.lab.project.author;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface LabProjectAuthorService extends IService<LabProjectAuthorEntity> {

    List<LabProjectAuthorEntity> getAuthorsByProjectIds(List<Long> projectIds);

    List<LabProjectAuthorEntity> getAuthorsByProjectId(Long projectId);

    boolean isMember(Long projectId, Long userId);
}
