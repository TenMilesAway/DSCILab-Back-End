package com.agileboot.domain.lab.event.author;

import com.agileboot.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("lab_event_author")
public class LabEventAuthorEntity extends BaseEntity<LabEventAuthorEntity> {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("event_id")
    private Long eventId;

    @TableField("user_id")
    private Long userId;

    @TableField("name")
    private String name;

    @TableField("name_en")
    private String nameEn;

    @TableField("affiliation")
    private String affiliation;

    @TableField("author_order")
    private Integer authorOrder;

    @TableField("is_corresponding")
    private Boolean isCorresponding;

    @TableField("role")
    private String role;

    @TableField("visible")
    private Boolean visible;

    @TableField("deleted")
    private Boolean deleted;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}
