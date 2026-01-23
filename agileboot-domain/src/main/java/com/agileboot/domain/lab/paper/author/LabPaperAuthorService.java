package com.agileboot.domain.lab.paper.author;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface LabPaperAuthorService extends IService<LabPaperAuthorEntity> {

    List<LabPaperAuthorEntity> getAuthorsByPaperId(Long paperId);

    List<LabPaperAuthorEntity> getAuthorsByPaperIds(List<Long> paperIds);

    boolean isAuthor(Long paperId, Long userId);

    LabPaperAuthorEntity getAuthorRecord(Long paperId, Long userId);

    boolean existsOrder(Long paperId, Integer authorOrder, Long excludeId);

    int hardDeleteDeletedByPaperId(Long paperId);

    int hardDeleteByPaperIdAndUserId(Long paperId, Long userId);

    int hardDeleteAllByPaperId(Long paperId);
}
