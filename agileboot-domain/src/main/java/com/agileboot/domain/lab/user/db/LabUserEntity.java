package com.agileboot.domain.lab.user.db;

import com.agileboot.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Year;

/**
 * 实验室用户信息表
 *
 * @author agileboot
 */
@Getter
@Setter
@TableName("lab_user")
@Schema(description = "实验室用户信息表")
public class LabUserEntity extends BaseEntity<LabUserEntity> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "学号/工号")
    @TableField(value = "student_number", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private String studentNumber;

    @Schema(description = "登录用户名")
    @TableField("username")
    private String username;

    @Schema(description = "真实姓名")
    @TableField("real_name")
    private String realName;

    @Schema(description = "英文名")
    @TableField("english_name")
    private String englishName;

    @Schema(description = "密码（加密）")
    @TableField("password")
    private String password;

    @Schema(description = "性别：0=未知,1=男,2=女")
    @TableField("gender")
    private Integer gender;

    @Schema(description = "身份：1=管理员,2=教师,3=学生")
    @TableField("identity")
    private Integer identity;

    @Schema(description = "学术身份：0=实验室负责人,1=教授,2=副教授,3=讲师,4=博士,5=硕士,6=本科")
    @TableField("academic_status")
    private Integer academicStatus;

    @Schema(description = "研究方向")
    @TableField("research_area")
    private String researchArea;

    @Schema(description = "手机号")
    @TableField(value = "phone", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private String phone;

    @Schema(description = "邮箱")
    @TableField(value = "email", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private String email;

    @Schema(description = "状态：1=在读/在职,2=毕业/离职")
    @TableField("status")
    private Integer status;

    @Schema(description = "入学/入职年份")
    @TableField("enrollment_year")
    private Year enrollmentYear;

    @Schema(description = "毕业/离职年份")
    @TableField("graduation_year")
    private Year graduationYear;

    @Schema(description = "毕业去向")
    @TableField("graduation_dest")
    private String graduationDest;

    @Schema(description = "照片路径")
    @TableField("photo")
    private String photo;

    @Schema(description = "个人简历")
    @TableField("resume")
    private String resume;

    @Schema(description = "个人主页")
    @TableField("homepage_url")
    private String homepageUrl;

    @Schema(description = "ORCID ID")
    @TableField("orcid")
    private String orcid;

    @Schema(description = "账号是否启用")
    @TableField("is_active")
    private Boolean isActive;

    @Override
    public Serializable pkVal() {
        return this.id;
    }

    /**
     * 身份枚举
     */
    public enum Identity {
        ADMIN(1, "管理员"),
        TEACHER(2, "教师"),
        STUDENT(3, "学生");

        private final Integer code;
        private final String desc;

        Identity(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 学术身份枚举
     */
    public enum AcademicStatus {
        HEAD(0, "实验室负责人"),
        PROFESSOR(1, "教授"),
        ASSOCIATE_PROFESSOR(2, "副教授"),
        LECTURER(3, "讲师"),
        PHD(4, "博士"),
        MASTER(5, "硕士"),
        BACHELOR(6, "本科");

        private final Integer code;
        private final String desc;

        AcademicStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 性别枚举
     */
    public enum Gender {
        UNKNOWN(0, "未知"),
        MALE(1, "男"),
        FEMALE(2, "女");

        private final Integer code;
        private final String desc;

        Gender(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 状态枚举
     */
    public enum Status {
        ACTIVE(1, "在读/在职"),
        GRADUATED(2, "毕业/离职");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
