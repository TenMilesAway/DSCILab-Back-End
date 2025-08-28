package com.agileboot.domain.lab.user.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.Year;

/**
 * 更新实验室用户命令
 *
 * @author agileboot
 */
@Data
@Schema(description = "更新实验室用户命令")
public class UpdateLabUserCommand {

    @Schema(description = "用户ID")
    @NotNull(message = "用户ID不能为空")
    private Long id;

    @Schema(description = "学号/工号")
    @Size(max = 20, message = "学号/工号长度不能超过20个字符")
    private String studentNumber;

    @Schema(description = "真实姓名")
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    @Schema(description = "英文名")
    @Size(max = 100, message = "英文名长度不能超过100个字符")
    private String englishName;

    @Schema(description = "性别：0=未知,1=男,2=女")
    @Min(value = 0, message = "性别值必须在0-2之间")
    @Max(value = 2, message = "性别值必须在0-2之间")
    private Integer gender;

    @Schema(description = "身份：1=管理员,2=教师,3=学生")
    @NotNull(message = "身份不能为空")
    @Min(value = 1, message = "身份值必须在1-3之间")
    @Max(value = 3, message = "身份值必须在1-3之间")
    private Integer identity;

    @Schema(description = "学术身份：1=教授,2=副教授,3=讲师,4=博士,5=硕士")
    @Min(value = 1, message = "学术身份值必须在1-5之间")
    @Max(value = 5, message = "学术身份值必须在1-5之间")
    private Integer academicStatus;

    @Schema(description = "研究方向")
    @Size(max = 1000, message = "研究方向长度不能超过1000个字符")
    private String researchArea;

    @Schema(description = "手机号")
    // 可选：为空或合法手机号
    @Pattern(regexp = "^(1[3-9]\\d{9})?$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Schema(description = "状态：1=在读/在职,2=毕业/离职")
    @Min(value = 1, message = "状态值必须在1-2之间")
    @Max(value = 2, message = "状态值必须在1-2之间")
    private Integer status;

    @Schema(description = "入学/入职年份")
    private Year enrollmentYear;

    @Schema(description = "毕业/离职年份")
    private Year graduationYear;

    @Schema(description = "毕业去向")
    @Size(max = 255, message = "毕业去向长度不能超过255个字符")
    private String graduationDest;

    @Schema(description = "个人简历")
    @Size(max = 5000, message = "个人简历长度不能超过5000个字符")
    private String resume;

    @Schema(description = "个人主页")
    @Size(max = 255, message = "个人主页长度不能超过255个字符")
    private String homepageUrl;

    @Schema(description = "ORCID ID")
    @Size(max = 50, message = "ORCID ID长度不能超过50个字符")
    private String orcid;

    @Schema(description = "账号是否启用")
    private Boolean isActive;
}
