package com.agileboot.domain.lab.user.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 管理员重置实验室用户密码命令（无需旧密码）
 */
@Data
@Schema(description = "管理员重置实验室用户密码命令")
public class ResetLabUserPasswordCommand {

    @Schema(description = "用户ID（由路径参数提供）")
    private Long userId;

    @Schema(description = "新密码（与创建用户时要求一致：6-20 位）", required = true)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;
}

