package com.agileboot.domain.lab.paper.command;

import com.agileboot.common.core.validation.CreateGroup;
import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Schema(description = "创建论文命令")
public class CreatePaperCommand {

    @Schema(description = "论文标题", required = true)
    @NotBlank(message = "论文标题不能为空")
    @Size(max = 500, message = "论文标题长度不能超过500个字符")
    private String title;

    @Schema(description = "英文标题")
    @Size(max = 500, message = "英文标题长度不能超过500个字符")
    private String titleEn;

    @Schema(description = "摘要/描述")
    private String description;

    @Schema(description = "关键词")
    @Size(max = 1000, message = "关键词长度不能超过1000个字符")
    private String keywords;

    @Schema(description = "成果分类ID（lab_achievement_category）")
    @NotNull(message = "成果分类不能为空", groups = CreateGroup.class)
    private Long categoryId;

    @Schema(description = "论文类型（lab_type.PAPERTYPE）")
    private Integer paperTypeId;

    @Schema(description = "期刊/会议/出版物名称")
    @Size(max = 500, message = "出版物名称长度不能超过500个字符")
    private String publication;

    @Schema(description = "会议名称")
    @Size(max = 255, message = "会议名称长度不能超过255个字符")
    private String conferenceName;

    @Schema(description = "卷(期)/编号")
    @Size(max = 255, message = "编号长度不能超过255个字符")
    private String issueNumber;

    @Schema(description = "会议地点")
    @Size(max = 255, message = "会议地点长度不能超过255个字符")
    private String publicationPlace;

    @Schema(description = "出版社/授权单位")
    @Size(max = 255, message = "出版社长度不能超过255个字符")
    private String publisher;

    @Schema(description = "页码")
    @Size(max = 100, message = "页码长度不能超过100个字符")
    private String pageRange;

    @Schema(description = "发表日期（格式：yyyy-MM-dd）")
    private String publishDate;

    @Schema(description = "参考信息")
    @Size(max = 5000, message = "参考信息长度不能超过5000个字符")
    private String reference;

    @Schema(description = "DOI")
    @Size(max = 128, message = "DOI 长度不能超过128个字符")
    private String doi;

    @Schema(description = "外部链接")
    @Size(max = 500, message = "链接长度不能超过500个字符")
    private String linkUrl;

    @Schema(description = "Git 仓库地址")
    @Size(max = 500, message = "Git 仓库地址长度不能超过500个字符")
    private String gitUrl;

    @Schema(description = "主页地址")
    @Size(max = 500, message = "主页地址长度不能超过500个字符")
    private String homepageUrl;

    @Schema(description = "PDF 地址")
    @Size(max = 500, message = "PDF 地址长度不能超过500个字符")
    private String pdfUrl;

    @Schema(description = "备注/其它信息")
    @Size(max = 1000, message = "备注长度不能超过1000个字符")
    private String notes;

    @Schema(description = "是否发布")
    private Boolean published = false;

    @Schema(description = "作者列表")
    private List<CreatePaperAuthorCommand> authors;

    @Schema(description = "关联基金列表")
    private List<PaperFundCommand> funds;

    @Schema(description = "关联项目ID列表")
    private List<Long> projectIds;

    @Data
    @Schema(description = "论文作者创建命令")
    public static class CreatePaperAuthorCommand {
        @Schema(description = "用户ID（内部作者）")
        private Long userId;

        @Schema(description = "作者姓名")
        @NotBlank(message = "作者姓名不能为空")
        private String name;

        @Schema(description = "英文姓名")
        private String nameEn;

        @Schema(description = "邮箱")
        private String email;

        @Schema(description = "单位/机构")
        @JsonAlias({ "organization", "organisation" })
        private String affiliation;

        @Schema(description = "作者顺序", required = true)
        @NotNull(message = "作者顺序不能为空")
        @Min(value = 1, message = "作者顺序必须>=1")
        private Integer authorOrder;

        @Schema(description = "是否通讯作者")
        @JsonAlias("isCorresponding")
        private Boolean corresponding = false;

        @Schema(description = "作者角色/贡献")
        private String role;

        @Schema(description = "是否展示在个人页")
        @JsonAlias("isVisible")
        private Boolean visible = true;
    }

    @Data
    @Schema(description = "论文基金关联命令")
    public static class PaperFundCommand {
        @NotNull
        private Long fundId;
        private java.math.BigDecimal amount;
    }
}
