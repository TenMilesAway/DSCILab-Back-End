package com.agileboot.domain.lab.user.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.Year;

/**
 * 更新实验室用户信息命令
 * 
 * @author agileboot
 */
@Data
@Schema(description = "更新实验室用户信息命令")
public class UpdateLabUserProfileCommand {

    @Schema(description = "真实姓名")
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    @Schema(description = "英文名")
    @Size(max = 100, message = "英文名长度不能超过100个字符")
    private String englishName;

    @Schema(description = "性别：0=未知,1=男,2=女")
    private Integer gender;

    @Schema(description = "学术身份：1=教授,2=副教授,3=讲师,4=博士,5=硕士")
    private Integer academicStatus;

    @Schema(description = "研究方向")
    @Size(max = 1000, message = "研究方向长度不能超过1000个字符")
    private String researchArea;

    @Schema(description = "手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

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
}
