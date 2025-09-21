package com.agileboot.domain.lab.achievement.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 创建成果命令（v2，仅使用叶子 categoryId，不接受旧字段）
 */
@Data
@Schema(description = "创建成果命令 v2（仅 categoryId）")
public class CreateLabAchievementV2Command {

    // v2 禁止提交旧字段：非空即报错（用于逐步淘汰旧字段）
    @Schema(hidden = true)
    @Null(message = "v2 接口不允许提交 legacy 字段：type")
    private Integer type;

    @Schema(hidden = true)
    @Null(message = "v2 接口不允许提交 legacy 字段：paperType")
    private Integer paperType;

    @Schema(hidden = true)
    @Null(message = "v2 接口不允许提交 legacy 字段：projectType")
    private Integer projectType;

    @Schema(description = "成果标题", required = true)
    @NotBlank(message = "成果标题不能为空")
    @Size(max = 500, message = "成果标题长度不能超过500个字符")
    private String title;

    @Schema(description = "英文标题")
    @Size(max = 500, message = "英文标题长度不能超过500个字符")
    private String titleEn;

    @Schema(description = "摘要/描述")
    private String description;

    @Schema(description = "关键词")
    @Size(max = 1000, message = "关键词长度不能超过1000个字符")
    private String keywords;

    @Schema(description = "成果类型ID（必须为二级/叶子分类）", required = true)
    @NotNull(message = "必须传二级分类ID: categoryId")
    private Long categoryId;

    @Schema(description = "期刊/会议/发布渠道")
    @Size(max = 300, message = "发布渠道长度不能超过300个字符")
    private String venue;

    @Schema(description = "发表年份（论文用），格式：yyyy")
    private String publishDate;

    @Schema(description = "项目开始年月（项目用），格式：yyyy-MM")
    private String projectStartDate;

    @Schema(description = "项目结束年月（项目用），格式：yyyy-MM")
    private String projectEndDate;

    @Schema(description = "参考文献/引用信息")
    @Size(max = 5000, message = "参考文献长度不能超过5000个字符")
    private String reference;

    @Schema(description = "外部链接URL")
    @Size(max = 500, message = "外部链接URL长度不能超过500个字符")
    private String linkUrl;

    @Schema(description = "Git 仓库地址")
    @Size(max = 500, message = "Git仓库地址长度不能超过500个字符")
    private String gitUrl;

    @Schema(description = "主页/展示页地址")
    @Size(max = 500, message = "主页地址长度不能超过500个字符")
    private String homepageUrl;

    @Schema(description = "PDF 下载地址")
    @Size(max = 500, message = "PDF地址长度不能超过500个字符")
    private String pdfUrl;

    @Schema(description = "论文 DOI")
    @Size(max = 128, message = "DOI长度不能超过128个字符")
    private String doi;

    @Schema(description = "项目经费（万元）")
    @DecimalMin(value = "0", message = "项目经费不能为负数")
    private BigDecimal fundingAmount;

    @Schema(description = "是否对外发布")
    private Boolean published = false;

    @Schema(description = "扩展信息")
    private String extra;

    @Schema(description = "作者列表（可选，创建时批量附带）")
    @Valid
    private List<CreateAuthorCommand> authors;
}

