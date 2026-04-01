package com.agileboot.domain.lab.achievement.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 论文-基金关联命令
 */
@Data
@Schema(description = "论文基金关联")
public class FundAssociationCommand {

    @Schema(description = "基金ID", required = true)
    @NotNull(message = "基金ID不能为空")
    private Long fundId;

    @Schema(description = "资助金额（万元）")
    @DecimalMin(value = "0", message = "资助金额不能为负数")
    private BigDecimal amount;
}
