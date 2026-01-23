package com.agileboot.domain.lab.project.relation;

import com.baomidou.mybatisplus.extension.service.IService;

public interface LabProjectPaperRelService extends IService<LabProjectPaperRelEntity> {

    /**
     * 物理删除某篇论文的所有项目关联
     */
    void hardDeleteByPaperId(Long paperId);

    /**
     * 物理删除某个项目的所有论文关联
     */
    void hardDeleteByProjectId(Long projectId);
}
