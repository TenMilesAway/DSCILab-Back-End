package com.agileboot.domain.lab.user.query;

import cn.hutool.core.util.StrUtil;
import com.agileboot.common.core.page.AbstractPageQuery;
import com.agileboot.domain.lab.user.db.LabUserEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Year;

/**
 * 实验室用户查询对象
 * 
 * @author agileboot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "实验室用户查询对象")
public class LabUserQuery extends AbstractPageQuery<LabUserEntity> {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "英文名")
    private String englishName;

    @Schema(description = "身份：1=管理员,2=教师,3=学生")
    private Integer identity;

    @Schema(description = "学术身份：1=教授,2=副教授,3=讲师,4=博士,5=硕士")
    private Integer academicStatus;

    @Schema(description = "研究方向关键词")
    private String researchAreaKeyword;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "状态：1=在读/在职,2=毕业/离职")
    private Integer status;

    @Schema(description = "入学年份开始")
    private Year enrollmentYearStart;

    @Schema(description = "入学年份结束")
    private Year enrollmentYearEnd;

    @Schema(description = "是否启用")
    private Boolean isActive;

    @Schema(description = "关键词搜索（姓名、用户名、邮箱）")
    private String keyword;

    @Override
    public QueryWrapper<LabUserEntity> addQueryCondition() {
        QueryWrapper<LabUserEntity> queryWrapper = new QueryWrapper<>();
        
        queryWrapper.like(StrUtil.isNotEmpty(username), "username", username)
                   .like(StrUtil.isNotEmpty(realName), "real_name", realName)
                   .like(StrUtil.isNotEmpty(englishName), "english_name", englishName)
                   .eq(identity != null, "identity", identity)
                   .eq(academicStatus != null, "academic_status", academicStatus)
                   .like(StrUtil.isNotEmpty(researchAreaKeyword), "research_area", researchAreaKeyword)
                   .like(StrUtil.isNotEmpty(email), "email", email)
                   .like(StrUtil.isNotEmpty(phone), "phone", phone)
                   .eq(status != null, "status", status)
                   .ge(enrollmentYearStart != null, "enrollment_year", enrollmentYearStart)
                   .le(enrollmentYearEnd != null, "enrollment_year", enrollmentYearEnd)
                   .eq(isActive != null, "is_active", isActive)
                   .eq("deleted", false);

        // 关键词搜索
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.and(w -> w.like("real_name", keyword)
                                 .or().like("username", keyword)
                                 .or().like("email", keyword));
        }

        // 设置默认排序
        if (StrUtil.isEmpty(this.getOrderColumn())) {
            this.setOrderColumn("create_time");
            this.setOrderDirection("descending");
        }

        return queryWrapper;
    }
}
