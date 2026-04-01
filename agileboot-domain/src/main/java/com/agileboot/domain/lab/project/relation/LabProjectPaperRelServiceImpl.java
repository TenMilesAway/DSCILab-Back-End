package com.agileboot.domain.lab.project.relation;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class LabProjectPaperRelServiceImpl
    extends ServiceImpl<LabProjectPaperRelMapper, LabProjectPaperRelEntity>
    implements LabProjectPaperRelService {

    @Override
    public void hardDeleteByPaperId(Long paperId) {
        if (paperId == null) {
            return;
        }
        this.baseMapper.hardDeleteByPaperId(paperId);
    }

    @Override
    public void hardDeleteByProjectId(Long projectId) {
        if (projectId == null) {
            return;
        }
        this.baseMapper.hardDeleteByProjectId(projectId);
    }
}
