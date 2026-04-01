package com.agileboot.domain.lab.project.relation;

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

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("lab_project_paper_rel")
@Schema(description = "项目-论文关联")
public class LabProjectPaperRelEntity extends BaseEntity<LabProjectPaperRelEntity> {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("project_id")
    private Long projectId;

    @TableField("paper_id")
    private Long paperId;

    @TableField("support_role")
    private String supportRole;

    @TableField("support_amount")
    private BigDecimal supportAmount;

    @TableField("note")
    private String note;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("create_time")
    private Date createTime;

    @TableField("updater_id")
    private Long updaterId;

    @TableField("update_time")
    private Date updateTime;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}
