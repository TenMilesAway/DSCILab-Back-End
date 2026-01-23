package com.agileboot.domain.lab.project.author;

import com.agileboot.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("lab_project_author")
@Schema(description = "项目作者")
public class LabProjectAuthorEntity extends BaseEntity<LabProjectAuthorEntity> {

    @Schema(description = "作者ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "项目ID")
    @TableField("project_id")
    private Long projectId;

    @Schema(description = "内部作者user_id；NULL为外部作者")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "作者姓名")
    @TableField("name")
    private String name;

    @Schema(description = "英文姓名")
    @TableField("name_en")
    private String nameEn;

    @Schema(description = "单位/机构")
    @TableField("affiliation")
    private String affiliation;

    @Schema(description = "作者顺序")
    @TableField("author_order")
    private Integer authorOrder;

    @Schema(description = "是否通讯作者")
    @TableField("is_corresponding")
    private Boolean isCorresponding;

    @Schema(description = "作者角色/贡献")
    @TableField("role")
    private String role;

    @Schema(description = "邮箱")
    @TableField("email")
    private String email;

    @Schema(description = "是否可见")
    @TableField("visible")
    private Boolean visible;

    @Schema(description = "是否删除")
    @TableField("deleted")
    private Boolean deleted;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}
