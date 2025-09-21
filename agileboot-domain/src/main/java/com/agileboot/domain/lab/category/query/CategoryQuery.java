package com.agileboot.domain.lab.category.query;

import com.agileboot.common.core.page.AbstractPageQuery;
import com.agileboot.domain.lab.category.db.LabAchievementCategoryEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

/**
 * 成果类型查询对象
 *
 * @author agileboot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "成果类型查询条件")
public class CategoryQuery extends AbstractPageQuery<LabAchievementCategoryEntity> {

    @Schema(description = "关键词（匹配类型名称、编码）")
    private String keyword;

    @Schema(description = "父类型ID，null查询一级类型")
    private Long parentId;

    @Schema(description = "是否启用")
    private Boolean isActive;

    @Schema(description = "是否系统内置")
    private Boolean isSystem;

    @Schema(description = "类型编码")
    private String categoryCode;

    @Schema(description = "是否包含子类型", example = "false")
    private Boolean includeChildren = false;

    @Schema(description = "是否只查询一级类型", example = "false")
    private Boolean topLevelOnly = false;

    @Schema(description = "是否只查询二级类型", example = "false")
    private Boolean secondLevelOnly = false;

    @Override
    public QueryWrapper<LabAchievementCategoryEntity> addQueryCondition() {
        QueryWrapper<LabAchievementCategoryEntity> queryWrapper = new QueryWrapper<>();

        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(w -> w.like("category_name", keyword)
                    .or().like("category_code", keyword)
                    .or().like("category_name_en", keyword));
        }

        // 父类型过滤
        if (parentId != null) {
            queryWrapper.eq("parent_id", parentId);
        }

        // 只查询一级类型
        if (Boolean.TRUE.equals(topLevelOnly)) {
            queryWrapper.isNull("parent_id");
        }

        // 只查询二级类型
        if (Boolean.TRUE.equals(secondLevelOnly)) {
            queryWrapper.isNotNull("parent_id");
        }

        // 启用状态过滤
        if (isActive != null) {
            queryWrapper.eq("is_active", isActive);
        }

        // 系统内置过滤
        if (isSystem != null) {
            queryWrapper.eq("is_system", isSystem);
        }

        // 类型编码过滤
        if (StringUtils.hasText(categoryCode)) {
            queryWrapper.eq("category_code", categoryCode);
        }

        // 排序
        queryWrapper.orderByAsc("sort_order");

        return queryWrapper;
    }
}
