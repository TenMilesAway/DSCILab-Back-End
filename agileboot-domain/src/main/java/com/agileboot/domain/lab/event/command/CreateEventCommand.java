package com.agileboot.domain.lab.event.command;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "创建活动命令")
public class CreateEventCommand {

    @NotBlank(message = "活动标题不能为空")
    @Size(max = 500, message = "活动标题长度不能超过500个字符")
    private String title;

    @Size(max = 1000, message = "活动摘要长度不能超过1000个字符")
    private String summary;

    @Schema(description = "活动时间（默认当前时间）")
    private LocalDate eventTime;

    @Schema(description = "活动内容（富文本）")
    @JsonDeserialize(using = StringDeserializer.class)
    @Size(max = 50000, message = "content长度不能超过50000个字符")
    private String content;

    @Size(max = 100, message = "标签长度不能超过100个字符")
    private String tag;

    private Boolean published = false;

    private List<EventAuthorCommand> authors;

    @Data
    @Schema(description = "活动作者")
    public static class EventAuthorCommand {
        @Schema(description = "内部作者userId；外部作者留空")
        private Long userId;

        @Schema(description = "外部作者必填")
        private String name;

        private String nameEn;

        private String affiliation;

        @NotNull(message = "authorOrder不能为空")
        @Min(value = 1, message = "authorOrder必须>=1")
        private Integer authorOrder;

        @JsonAlias("isCorresponding")
        private Boolean corresponding = false;

        private String role;

        @JsonAlias("isVisible")
        private Boolean visible = true;
    }
}
