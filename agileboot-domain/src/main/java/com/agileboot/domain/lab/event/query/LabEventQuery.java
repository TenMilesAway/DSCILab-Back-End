package com.agileboot.domain.lab.event.query;

import cn.hutool.core.util.StrUtil;
import com.agileboot.common.core.page.AbstractPageQuery;
import com.agileboot.domain.lab.event.db.LabEventEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "活动查询条件")
public class LabEventQuery extends AbstractPageQuery<LabEventEntity> {

    private String keyword;
    private String tag;
    private Long ownerUserId;
    private Boolean published;
    private LocalDate dateStart;
    private LocalDate dateEnd;

    @Override
    public QueryWrapper<LabEventEntity> addQueryCondition() {
        QueryWrapper<LabEventEntity> qw = new QueryWrapper<>();
        qw.eq("deleted", false);
        if (StrUtil.isNotBlank(keyword)) {
            qw.and(w -> w.like("title", keyword)
                .or().like("summary", keyword)
                .or().like("tag", keyword));
        }
        if (StrUtil.isNotBlank(tag)) {
            qw.eq("tag", tag);
        }
        if (ownerUserId != null) {
            qw.eq("owner_user_id", ownerUserId);
        }
        if (published != null) {
            qw.eq("published", published);
        }
        if (dateStart != null) {
            qw.ge("event_time", dateStart);
        }
        if (dateEnd != null) {
            qw.le("event_time", dateEnd);
        }
        qw.orderByDesc("event_time");
        return qw;
    }
}
