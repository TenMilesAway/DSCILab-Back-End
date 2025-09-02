package com.agileboot.domain.lab.user.dto;

import com.agileboot.domain.lab.user.db.LabUserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Year;

/**
 * 实验室用户个人信息DTO
 *
 * @author agileboot
 */
@Data
@NoArgsConstructor
@Schema(description = "实验室用户个人信息")
public class LabUserProfileDTO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "学号/工号")
    private String studentNumber;

    @Schema(description = "登录用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "英文名")
    private String englishName;

    @Schema(description = "性别：0=未知,1=男,2=女")
    private Integer gender;

    @Schema(description = "性别描述")
    private String genderDesc;

    @Schema(description = "身份：1=管理员,2=教师,3=学生")
    private Integer identity;

    @Schema(description = "身份描述")
    private String identityDesc;

    @Schema(description = "学术身份：0=实验室负责人,1=教授,2=副教授,3=讲师,4=博士,5=硕士,6=本科")
    private Integer academicStatus;

    @Schema(description = "学术身份描述")
    private String academicStatusDesc;

    @Schema(description = "研究方向")
    private String researchArea;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "状态：1=在读/在职,2=毕业/离职")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "入学/入职年份")
    private Year enrollmentYear;

    @Schema(description = "毕业/离职年份")
    private Year graduationYear;

    @Schema(description = "毕业去向")
    private String graduationDest;

    @Schema(description = "照片路径")
    private String photo;

    @Schema(description = "个人简历")
    private String resume;

    @Schema(description = "个人主页")
    private String homepageUrl;

    @Schema(description = "ORCID ID")
    private String orcid;

    @Schema(description = "账号是否启用")
    private Boolean isActive;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;

    public LabUserProfileDTO(LabUserEntity entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.studentNumber = entity.getStudentNumber();
            this.username = entity.getUsername();
            this.realName = entity.getRealName();
            this.englishName = entity.getEnglishName();
            this.gender = entity.getGender();
            this.identity = entity.getIdentity();
            this.academicStatus = entity.getAcademicStatus();
            this.researchArea = entity.getResearchArea();
            this.phone = entity.getPhone();
            this.email = entity.getEmail();
            this.status = entity.getStatus();
            this.enrollmentYear = entity.getEnrollmentYear();
            this.graduationYear = entity.getGraduationYear();
            this.graduationDest = entity.getGraduationDest();
            this.photo = entity.getPhoto();
            this.resume = entity.getResume();
            this.homepageUrl = entity.getHomepageUrl();
            this.orcid = entity.getOrcid();
            this.isActive = entity.getIsActive();

            // 设置描述信息
            this.genderDesc = getGenderDesc(this.gender);
            this.identityDesc = getIdentityDesc(this.identity);
            this.academicStatusDesc = getAcademicStatusDesc(this.academicStatus);
            this.statusDesc = getStatusDesc(this.status);

            // 格式化时间
            if (entity.getCreateTime() != null) {
                this.createTime = entity.getCreateTime().toString();
            }
            if (entity.getUpdateTime() != null) {
                this.updateTime = entity.getUpdateTime().toString();
            }
        }
    }

    private String getGenderDesc(Integer gender) {
        if (gender == null) return null;
        for (LabUserEntity.Gender g : LabUserEntity.Gender.values()) {
            if (g.getCode().equals(gender)) {
                return g.getDesc();
            }
        }
        return null;
    }

    private String getIdentityDesc(Integer identity) {
        if (identity == null) return null;
        for (LabUserEntity.Identity i : LabUserEntity.Identity.values()) {
            if (i.getCode().equals(identity)) {
                return i.getDesc();
            }
        }
        return null;
    }

    private String getAcademicStatusDesc(Integer academicStatus) {
        if (academicStatus == null) return null;
        for (LabUserEntity.AcademicStatus a : LabUserEntity.AcademicStatus.values()) {
            if (a.getCode().equals(academicStatus)) {
                return a.getDesc();
            }
        }
        return null;
    }

    private String getStatusDesc(Integer status) {
        if (status == null) return null;
        for (LabUserEntity.Status s : LabUserEntity.Status.values()) {
            if (s.getCode().equals(status)) {
                return s.getDesc();
            }
        }
        return null;
    }
}
