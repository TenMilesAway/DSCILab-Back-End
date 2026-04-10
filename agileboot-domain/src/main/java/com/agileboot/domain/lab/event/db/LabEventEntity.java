package com.agileboot.domain.lab.event.db;

import com.agileboot.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("lab_event")
@Schema(description = "活动")
public class LabEventEntity extends BaseEntity<LabEventEntity> {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("title")
    private String title;

    @TableField(value = "summary", updateStrategy = FieldStrategy.IGNORED)
    private String summary;

    @TableField("event_time")
    private LocalDate eventTime;

    @TableField(value = "content", updateStrategy = FieldStrategy.IGNORED)
    private String content;

    @TableField(value = "tag", updateStrategy = FieldStrategy.IGNORED)
    private String tag;

    @TableField("owner_user_id")
    private Long ownerUserId;

    @TableField("published")
    private Boolean published;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("updater_id")
    private Long updaterId;

    @TableLogic
    @TableField("deleted")
    private Boolean deleted;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}
