package com.agileboot.domain.lab.user.dto;

import com.agileboot.domain.lab.user.db.LabUserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Year;

/**
 * 对外开放展示用的精简用户信息（不包含敏感字段）。
 */
@Data
@Schema(description = "对外开放展示用的精简用户信息")
public class PublicLabUserDTO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "英文名")
    private String englishName;

    @Schema(description = "身份：1=管理员,2=教师,3=学生")
    private Integer identity;

    @Schema(description = "学术身份：1=教授,2=副教授,3=讲师,4=博士,5=硕士")
    private Integer academicStatus;

    @Schema(description = "研究方向")
    private String researchArea;

    @Schema(description = "入学/入职年份")
    private Year enrollmentYear;

    @Schema(description = "毕业/离职年份")
    private Year graduationYear;

    @Schema(description = "毕业去向")
    private String graduationDest;

    @Schema(description = "照片路径")
    private String photo;

    @Schema(description = "个人主页")
    private String homepageUrl;

    @Schema(description = "邮箱（对外展示）")
    private String email;

    @Schema(description = "ORCID ID")
    private String orcid;

    public static PublicLabUserDTO fromEntity(LabUserEntity e) {
        PublicLabUserDTO dto = new PublicLabUserDTO();
        dto.setId(e.getId());
        dto.setRealName(e.getRealName());
        dto.setEnglishName(e.getEnglishName());
        dto.setIdentity(e.getIdentity());
        dto.setAcademicStatus(e.getAcademicStatus());
        dto.setResearchArea(e.getResearchArea());
        dto.setEnrollmentYear(e.getEnrollmentYear());
        dto.setGraduationYear(e.getGraduationYear());
        dto.setGraduationDest(e.getGraduationDest());
        dto.setPhoto(e.getPhoto());
        dto.setHomepageUrl(e.getHomepageUrl());
        dto.setEmail(e.getEmail());
        dto.setOrcid(e.getOrcid());
        return dto;
    }
}

