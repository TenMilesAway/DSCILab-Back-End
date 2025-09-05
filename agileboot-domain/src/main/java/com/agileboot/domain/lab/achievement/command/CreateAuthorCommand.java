package com.agileboot.domain.lab.achievement.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@Schema(description = "创建作者命令（用于批量附带）")
public class CreateAuthorCommand {

    @Schema(description = "内部作者userId；为空代表外部作者")
    private Long userId;

    @Schema(description = "作者姓名（外部作者必填）")
    @Size(max = 100, message = "作者姓名长度不能超过100")
    private String name;

    @Schema(description = "英文姓名")
    @Size(max = 100, message = "英文姓名长度不能超过100")
    private String nameEn;

    @Schema(description = "单位/机构")
    @Size(max = 300, message = "单位长度不能超过300")
    private String affiliation;

    @Schema(description = "作者顺序(>0)")
    @NotNull(message = "作者顺序不能为空")
    @Min(value = 1, message = "作者顺序必须>=1")
    private Integer authorOrder;

    @Schema(description = "是否通讯作者")
    private Boolean isCorresponding = false;

    @Schema(description = "作者角色/贡献")
    @Size(max = 100, message = "角色长度不能超过100")
    private String role;

    @Schema(description = "是否在个人页可见（仅内部作者生效）")
    private Boolean visible = true;
}
