package com.agileboot.domain.lab.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "关联项目信息")
public class RelatedProjectDTO {

    private Long id;
    private String title;
    private Integer projectTypeId;
    private String projectTypeDesc;
    private LocalDate projectStartDate;
    private LocalDate projectEndDate;
    private Long categoryId;
    private String categoryName;
}
