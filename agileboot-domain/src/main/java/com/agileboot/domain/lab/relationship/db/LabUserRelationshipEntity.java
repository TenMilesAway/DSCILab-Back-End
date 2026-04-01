package com.agileboot.domain.lab.relationship.db;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

/**
 * 实验室师生关系表
 *
 * @author agileboot
 */
@Getter
@Setter
@TableName("lab_user_relationship")
@Schema(description = "实验室师生关系表")
public class LabUserRelationshipEntity extends Model<LabUserRelationshipEntity> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "关系ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "导师ID")
    @TableField("teacher_id")
    private Long teacherId;

    @Schema(description = "学生ID")
    @TableField("student_id")
    private Long studentId;

    @Schema(description = "关系类型：1=导师关系")
    @TableField("relationship_type")
    private Integer relationshipType;

    @Schema(description = "关系开始时间")
    @TableField("start_date")
    private LocalDate startDate;

    @Schema(description = "关系结束时间（毕业等）")
    @TableField("end_date")
    private LocalDate endDate;

    @Schema(description = "状态：1=活跃,2=已结束")
    @TableField("status")
    private Integer status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "创建者ID")
    @TableField(value = "creator_id", fill = FieldFill.INSERT)
    private Long creatorId;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @Schema(description = "更新者ID")
    @TableField(value = "updater_id", fill = FieldFill.UPDATE, updateStrategy = FieldStrategy.NOT_NULL)
    private Long updaterId;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private Date updateTime;

    @Override
    public Serializable pkVal() {
        return this.id;
    }

    /**
     * 关系类型枚举
     */
    public enum RelationshipType {
        SUPERVISOR(1, "导师关系");

        private final Integer code;
        private final String desc;

        RelationshipType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 关系状态枚举
     */
    public enum Status {
        ACTIVE(1, "活跃"),
        ENDED(2, "已结束");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
