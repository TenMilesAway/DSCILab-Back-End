package com.agileboot.domain.lab.user.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 修改密码命令
 * 
 * @author agileboot
 */
@Data
@Schema(description = "修改密码命令")
public class ChangePasswordCommand {

    @Schema(description = "原密码")
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @Schema(description = "新密码")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间")
    private String newPassword;

    @Schema(description = "确认新密码")
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
