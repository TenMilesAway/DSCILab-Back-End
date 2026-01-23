package com.agileboot.domain.lab.achievement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 论文基金关联 DTO
 */
@Data
@Schema(description = "论文基金关联信息")
public class LabFundAssociationDTO {

    @Schema(description = "基金ID")
    private Long fundId;

    @Schema(description = "资助金额（万元）")
    private BigDecimal amount;
}
