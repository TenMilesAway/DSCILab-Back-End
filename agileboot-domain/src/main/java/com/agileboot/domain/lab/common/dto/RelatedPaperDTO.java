package com.agileboot.domain.lab.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "关联论文信息")
public class RelatedPaperDTO {

    private Long id;
    private String title;
    private Integer paperTypeId;
    private String paperTypeDesc;
    private LocalDate publishDate;
    private Long categoryId;
    private String categoryName;
}
