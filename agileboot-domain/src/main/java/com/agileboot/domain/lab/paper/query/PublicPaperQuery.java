package com.agileboot.domain.lab.paper.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公开论文查询条件，继承 LabPaperQuery 并默认只查询已发布的论文
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "公开论文查询条件")
public class PublicPaperQuery extends LabPaperQuery {

    public PublicPaperQuery() {
        // 默认公开只展示已发布的论文
        this.setPublished(true);
    }
}
