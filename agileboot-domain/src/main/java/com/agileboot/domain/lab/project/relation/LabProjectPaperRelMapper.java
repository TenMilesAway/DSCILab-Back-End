package com.agileboot.domain.lab.project.relation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface LabProjectPaperRelMapper extends BaseMapper<LabProjectPaperRelEntity> {

    /**
     * 物理删除指定论文的所有关联记录，避免逻辑删除产生的唯一索引冲突
     */
    @Delete("DELETE FROM lab_project_paper_rel WHERE paper_id = #{paperId}")
    int hardDeleteByPaperId(@Param("paperId") Long paperId);

    /**
     * 物理删除指定项目的所有关联记录
     */
    @Delete("DELETE FROM lab_project_paper_rel WHERE project_id = #{projectId}")
    int hardDeleteByProjectId(@Param("projectId") Long projectId);
}
