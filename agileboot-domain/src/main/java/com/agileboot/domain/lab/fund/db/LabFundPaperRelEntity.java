package com.agileboot.domain.lab.fund.db;

import com.agileboot.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 基金与论文关联实体
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("lab_fund_paper_rel")
@Schema(description = "基金与论文关联")
public class LabFundPaperRelEntity extends BaseEntity<LabFundPaperRelEntity> {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableField("fund_id")
    @Schema(description = "基金ID")
    private Long fundId;

    @TableField("paper_id")
    @Schema(description = "论文成果ID")
    private Long paperId;

    @TableField("support_role")
    @Schema(description = "资助角色/类型")
    private String supportRole;

    @TableField("amount")
    @Schema(description = "资助金额（万元）")
    private BigDecimal amount;

    @TableField("note")
    @Schema(description = "备注")
    private String note;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}
