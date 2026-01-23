package com.agileboot.domain.lab.paper.query;

import cn.hutool.core.util.StrUtil;
import com.agileboot.common.core.page.AbstractPageQuery;
import com.agileboot.domain.lab.paper.db.LabPaperEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 论文查询条件
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "论文查询条件")
public class LabPaperQuery extends AbstractPageQuery<LabPaperEntity> {

    @Schema(description = "关键词（标题、作者文本、关键字）")
    private String keyword;

    @Schema(description = "论文类型ID（lab_type.PAPERTYPE）")
    private Integer paperTypeId;

    @Schema(description = "成果分类ID（二级分类）")
    private Long categoryId;

    @Schema(description = "父级分类ID（一级分类）")
    private Long parentCategoryId;

    @Schema(description = "成果所有者ID")
    private Long ownerUserId;

    @Schema(description = "是否已发布")
    private Boolean published;

    @Schema(description = "是否已审核")
    private Boolean verified;

    @Schema(description = "发表日期范围-开始")
    private LocalDate dateStart;

    @Schema(description = "发表日期范围-结束")
    private LocalDate dateEnd;

    @Override
    public QueryWrapper<LabPaperEntity> addQueryCondition() {
        QueryWrapper<LabPaperEntity> qw = new QueryWrapper<>();
        qw.eq("deleted", false);

        if (StrUtil.isNotBlank(keyword)) {
            qw.and(w -> w.like("title", keyword)
                .or().like("author_text", keyword)
                .or().like("keywords", keyword));
        }
        if (paperTypeId != null) {
            qw.eq("paper_type_id", paperTypeId);
        }
        if (ownerUserId != null) {
            qw.eq("owner_user_id", ownerUserId);
        }
        if (published != null) {
            qw.eq("published", published);
        }
        if (verified != null) {
            qw.eq("is_verified", verified);
        }
        if (dateStart != null) {
            qw.ge("publish_date", dateStart);
        }
        if (dateEnd != null) {
            qw.le("publish_date", dateEnd);
        }
        qw.orderByDesc("publish_date");
        return qw;
    }
}
