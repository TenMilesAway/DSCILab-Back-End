package com.agileboot.domain.lab.category.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量更新排序命令
 *
 * @author agileboot
 */
@Data
@Schema(description = "批量更新排序命令")
public class BatchUpdateSortCommand {

    @NotEmpty(message = "排序项不能为空")
    @Valid
    @Schema(description = "排序项列表", required = true)
    private List<SortItem> items;

    @Data
    @Schema(description = "排序项")
    public static class SortItem {

        @NotNull(message = "类型ID不能为空")
        @Schema(description = "类型ID", required = true)
        private Long id;

        @NotNull(message = "排序号不能为空")
        @Schema(description = "排序号", required = true)
        private Integer sortOrder;
    }
}
