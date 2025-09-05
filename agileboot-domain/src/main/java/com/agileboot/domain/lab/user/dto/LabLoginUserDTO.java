package com.agileboot.domain.lab.user.dto;

import com.agileboot.domain.lab.user.db.LabUserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录态下返回的实验室用户精简信息")
public class LabLoginUserDTO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "登录用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "性别：0=未知,1=男,2=女")
    private Integer gender;

    @Schema(description = "性别描述")
    private String genderDesc;

    @Schema(description = "身份：1=管理员,2=教师,3=学生")
    private Integer identity;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像")
    private String photo;

    @Schema(description = "账号是否启用")
    private Boolean isActive;

    @Schema(description = "创建时间")
    private java.util.Date createTime;

    @Schema(description = "更新时间")
    private java.util.Date updateTime;

    public static LabLoginUserDTO from(LabUserEntity e) {
        if (e == null) return null;
        LabLoginUserDTO dto = new LabLoginUserDTO();
        dto.setId(e.getId());
        dto.setUsername(e.getUsername());
        dto.setRealName(e.getRealName());
        dto.setGender(e.getGender());
        dto.setGenderDesc(e.getGender() == null ? null :
            (e.getGender() == 1 ? "男" : (e.getGender() == 2 ? "女" : "未知")));
        dto.setIdentity(e.getIdentity());
        dto.setEmail(e.getEmail());
        dto.setPhone(e.getPhone());
        dto.setPhoto(e.getPhoto());
        dto.setIsActive(e.getIsActive());
        dto.setCreateTime(e.getCreateTime());
        dto.setUpdateTime(e.getUpdateTime());
        return dto;
    }
}

