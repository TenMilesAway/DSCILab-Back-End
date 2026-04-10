package com.agileboot.domain.lab.event.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "公开活动查询条件")
public class PublicEventQuery extends LabEventQuery {

    public PublicEventQuery() {
        this.setPublished(true);
    }
}
