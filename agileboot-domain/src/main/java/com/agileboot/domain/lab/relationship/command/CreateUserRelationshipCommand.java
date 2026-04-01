package com.agileboot.domain.lab.relationship.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 创建师生关系命令
 *
 * @author agileboot
 */
@Data
@Schema(description = "创建师生关系命令")
public class CreateUserRelationshipCommand {

    @Schema(description = "导师ID")
    @NotNull(message = "导师ID不能为空")
    private Long teacherId;

    @Schema(description = "学生ID")
    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @Schema(description = "关系类型：1=导师关系")
    private Integer relationshipType = 1;

    @Schema(description = "关系开始时间")
    private LocalDate startDate;

    @Schema(description = "备注")
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
