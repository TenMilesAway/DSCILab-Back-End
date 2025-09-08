package com.agileboot.domain.lab.achievement.query;

import com.agileboot.common.core.page.AbstractPageQuery;
import com.agileboot.domain.lab.achievement.db.LabAchievementEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 公开成果查询条件
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "公开成果查询条件")
public class PublicAchievementQuery extends AbstractPageQuery<LabAchievementEntity> {

    @Schema(description = "关键词（标题、关键词模糊搜索）")
    private String keyword;

    @Schema(description = "成果类型：1=论文,2=项目")
    private Integer type;

    @Schema(description = "论文类型：1-7")
    private Integer paperType;

    @Schema(description = "项目类型：1-8")
    private Integer projectType;

    @Schema(description = "发表/开始日期范围-开始")
    private LocalDate dateStart;

    @Schema(description = "发表/开始日期范围-结束")
    private LocalDate dateEnd;

    @Schema(description = "作者姓名（模糊搜索作者的中文名或英文名）")
    private String authorName;

    @Override
    public QueryWrapper<LabAchievementEntity> addQueryCondition() {
        QueryWrapper<LabAchievementEntity> qw = new QueryWrapper<>();

        // 公开端固定条件：已发布且已审核
        qw.eq("deleted", false)
          .eq("published", true)
          .eq("is_verified", true);

        if (StrUtil.isNotBlank(keyword)) {
            qw.and(w -> w.like("title", keyword).or().like("keywords", keyword));
        }
        if (type != null) {
            qw.eq("type", type);
        }
        if (paperType != null) {
            qw.eq("paper_type", paperType);
        }
        if (projectType != null) {
            qw.eq("project_type", projectType);
        }

        // 日期范围：论文用 publish_date，项目用 project_start_date
        if (dateStart != null || dateEnd != null) {
            qw.and(w -> w.and(w1 -> w1.eq("type", 1)
                                      .ge(dateStart != null, "publish_date", dateStart)
                                      .le(dateEnd != null, "publish_date", dateEnd))
                           .or()
                           .and(w2 -> w2.eq("type", 2)
                                      .ge(dateStart != null, "project_start_date", dateStart)
                                      .le(dateEnd != null, "project_start_date", dateEnd))
            );
        }

        // 排序：论文按发表日期降序，项目按开始日期降序
        qw.orderByDesc("CASE WHEN type = 1 THEN publish_date WHEN type = 2 THEN project_start_date END");

        return qw;
    }

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<LabAchievementEntity> toPage() {
        // 公开接口默认分页大小改为1000
        pageNum = cn.hutool.core.util.ObjectUtil.defaultIfNull(pageNum, DEFAULT_PAGE_NUM);
        pageSize = cn.hutool.core.util.ObjectUtil.defaultIfNull(pageSize, 1000);
        return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
    }
}
