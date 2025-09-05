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
 * 我的成果查询条件
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "我的成果查询条件")
public class MyAchievementQuery extends AbstractPageQuery<LabAchievementEntity> {

    @Schema(description = "关键词（标题、关键词模糊搜索）")
    private String keyword;

    @Schema(description = "成果类型：1=论文,2=项目")
    private Integer type;

    @Schema(description = "论文类型：1-7")
    private Integer paperType;

    @Schema(description = "项目类型：1-8")
    private Integer projectType;

    @Schema(description = "是否对外发布")
    private Boolean published;

    @Schema(description = "是否已审核")
    private Boolean isVerified;

    @Schema(description = "发表/开始日期范围-开始")
    private LocalDate dateStart;

    @Schema(description = "发表/开始日期范围-结束")
    private LocalDate dateEnd;

    @Override
    public QueryWrapper<LabAchievementEntity> addQueryCondition() {
        QueryWrapper<LabAchievementEntity> qw = new QueryWrapper<>();
        qw.eq("deleted", false);
        
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
        if (published != null) {
            qw.eq("published", published);
        }
        if (isVerified != null) {
            qw.eq("is_verified", isVerified);
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
        return qw;
    }
}
