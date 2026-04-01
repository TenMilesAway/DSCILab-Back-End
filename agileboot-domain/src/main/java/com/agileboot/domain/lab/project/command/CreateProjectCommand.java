package com.agileboot.domain.lab.project.command;

import com.agileboot.common.core.validation.CreateGroup;
import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "创建项目命令")
public class CreateProjectCommand {

    @Schema(description = "项目名称", required = true)
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 500, message = "项目名称长度不能超过500个字符")
    private String title;

    @Schema(description = "英文名称")
    private String titleEn;

    @Schema(description = "摘要/描述")
    private String description;

    @Schema(description = "关键词")
    private String keywords;

    @Schema(description = "项目类型（lab_type.FUNDTYPE，可选）")
    private Integer projectTypeId;

    @Schema(description = "成果分类ID（lab_achievement_category）")
    @NotNull(message = "成果分类不能为空", groups = CreateGroup.class)
    private Long categoryId;

    @Schema(description = "项目号")
    private String projectNumber;

    @Schema(description = "资助单位/主办方")
    private String supporter;

    @Schema(description = "开始日期（yyyy-MM-dd）")
    private String projectStartDate;

    @Schema(description = "结束日期（yyyy-MM-dd）")
    private String projectEndDate;

    @Schema(description = "项目经费（万元）")
    private BigDecimal fundingAmount;

    @Schema(description = "金额展示文本")
    private String amountDisplay;

    @Schema(description = "资助说明（中文）")
    private String supportCn;

    @Schema(description = "资助说明（英文）")
    private String supportEn;

    @Schema(description = "结项要求")
    private String requirement;

    @Schema(description = "其它信息")
    private String descText;

    @Schema(description = "参考信息")
    private String reference;

    @Schema(description = "外部链接")
    private String linkUrl;

    @Schema(description = "Git 仓库地址")
    private String gitUrl;

    @Schema(description = "主页链接")
    private String homepageUrl;

    @Schema(description = "PDF 链接")
    private String pdfUrl;

    @Schema(description = "是否发布")
    private Boolean published = false;

    @Schema(description = "参与成员ID列表（兼容旧字段，建议使用 authors）")
    private List<Long> memberIds;

    @Schema(description = "项目作者列表")
    private List<ProjectAuthorCommand> authors;

    @Data
    @Schema(description = "项目作者信息")
    public static class ProjectAuthorCommand {
        @Schema(description = "内部作者用户ID")
        private Long userId;

        @Schema(description = "作者姓名")
        private String name;

        @Schema(description = "作者英文姓名")
        private String nameEn;

        @Schema(description = "邮箱")
        private String email;

        @Schema(description = "单位/机构")
        private String affiliation;

        @Schema(description = "作者顺序（从1开始）")
        private Integer authorOrder;

        @Schema(description = "是否通讯作者")
        @JsonAlias("isCorresponding")
        private Boolean corresponding;

        @Schema(description = "作者角色/贡献")
        private String role;

        @Schema(description = "是否在公开页展示（内部作者）")
        @JsonAlias("isVisible")
        private Boolean visible;
    }
}
