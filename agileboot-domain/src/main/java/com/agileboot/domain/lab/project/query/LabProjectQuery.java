package com.agileboot.domain.lab.project.query;

import cn.hutool.core.util.StrUtil;
import com.agileboot.common.core.page.AbstractPageQuery;
import com.agileboot.domain.lab.project.db.LabProjectEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "项目查询条件")
public class LabProjectQuery extends AbstractPageQuery<LabProjectEntity> {

    @Schema(description = "关键词（标题/关键词模糊搜索）")
    private String keyword;

    @Schema(description = "项目类型ID（lab_type.FUNDTYPE）")
    private Integer projectTypeId;

    @Schema(description = "资助单位")
    private String supporter;

    @Schema(description = "成果分类ID（二级分类）")
    private Long categoryId;

    @Schema(description = "父级分类ID（一级分类）")
    private Long parentCategoryId;

    @Schema(description = "拥有者ID")
    private Long ownerUserId;

    @Schema(description = "是否发布")
    private Boolean published;

    @Schema(description = "是否审核")
    private Boolean verified;

    @Schema(description = "开始日期-起")
    private LocalDate startDateBegin;

    @Schema(description = "开始日期-止")
    private LocalDate startDateEnd;

    @Override
    public QueryWrapper<LabProjectEntity> addQueryCondition() {
        QueryWrapper<LabProjectEntity> qw = new QueryWrapper<>();
        qw.eq("deleted", false);
        if (StrUtil.isNotBlank(keyword)) {
            qw.and(w -> w.like("title", keyword).or().like("keywords", keyword));
        }
        if (projectTypeId != null) {
            qw.eq("project_type_id", projectTypeId);
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
        if (StrUtil.isNotBlank(supporter)) {
            qw.like("supporter", supporter);
        }
        if (startDateBegin != null) {
            qw.ge("project_start_date", startDateBegin);
        }
        if (startDateEnd != null) {
            qw.le("project_start_date", startDateEnd);
        }
        qw.orderByDesc("project_start_date");
        return qw;
    }
}
