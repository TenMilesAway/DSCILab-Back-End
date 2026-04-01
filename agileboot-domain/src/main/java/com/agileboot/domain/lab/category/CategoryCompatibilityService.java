package com.agileboot.domain.lab.category;

import com.agileboot.domain.lab.category.db.LabAchievementCategoryEntity;
import com.agileboot.domain.lab.category.db.LabAchievementCategoryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 成果类型兼容性服务
 * 用于处理新旧类型系统的映射和兼容
 *
 * @author agileboot
 */
@Service
@RequiredArgsConstructor
public class CategoryCompatibilityService {

    private final LabAchievementCategoryService categoryService;

    /**
     * 旧类型映射信息
     */
    @Data
    public static class LegacyTypeMapping {
        private Integer type;
        private Integer subType;
        private String typeName;
        private String subTypeName;
    }

    /**
     * 根据旧的type和subType获取新的categoryId
     *
     * @param type 成果类型（1=论文，2=项目）
     * @param subType 子类型
     * @return 新的categoryId
     */
    public Long getCategoryIdByLegacyType(Integer type, Integer subType) {
        if (type == null) {
            return null;
        }

        // 论文类型映射
        if (type == 1) {
            switch (subType != null ? subType : 0) {
                case 1: return 3L;  // 期刊论文
                case 2: return 4L;  // 会议论文
                case 3: return 5L;  // 书籍章节
                case 4: return 6L;  // 专利
                case 5: return 7L;  // 标准
                case 6: return 8L;  // 研究报告
                case 7: return 9L;  // 其他论文
                default: return 3L; // 默认为期刊论文
            }
        }

        // 项目类型映射
        if (type == 2) {
            switch (subType != null ? subType : 0) {
                case 1: return 10L; // 国家重点项目
                case 2: return 11L; // 国家一般项目
                case 3: return 12L; // 省部级项目
                case 4: return 13L; // 企业合作项目
                case 5: return 14L; // 国际合作项目
                case 6: return 15L; // 青年基金
                case 7: return 16L; // 博士后基金
                case 8: return 17L; // 其他项目
                default: return 10L; // 默认为国家重点项目
            }
        }

        return null;
    }

    /**
     * 根据categoryId获取旧的type和subType
     *
     * @param categoryId 新的类型ID
     * @return 旧类型映射信息
     */
    public LegacyTypeMapping getLegacyTypeByCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }

        LegacyTypeMapping mapping = new LegacyTypeMapping();

        // 根据categoryId映射回旧的类型
        switch (categoryId.intValue()) {
            // 论文类型
            case 1:
                mapping.setType(1);
                mapping.setSubType(null);
                mapping.setTypeName("论文");
                break;
            case 3:
                mapping.setType(1);
                mapping.setSubType(1);
                mapping.setTypeName("论文");
                mapping.setSubTypeName("期刊论文");
                break;
            case 4:
                mapping.setType(1);
                mapping.setSubType(2);
                mapping.setTypeName("论文");
                mapping.setSubTypeName("会议论文");
                break;
            case 5:
                mapping.setType(1);
                mapping.setSubType(3);
                mapping.setTypeName("论文");
                mapping.setSubTypeName("书籍章节");
                break;
            case 6:
                mapping.setType(1);
                mapping.setSubType(4);
                mapping.setTypeName("论文");
                mapping.setSubTypeName("专利");
                break;
            case 7:
                mapping.setType(1);
                mapping.setSubType(5);
                mapping.setTypeName("论文");
                mapping.setSubTypeName("标准");
                break;
            case 8:
                mapping.setType(1);
                mapping.setSubType(6);
                mapping.setTypeName("论文");
                mapping.setSubTypeName("研究报告");
                break;
            case 9:
                mapping.setType(1);
                mapping.setSubType(7);
                mapping.setTypeName("论文");
                mapping.setSubTypeName("其他论文");
                break;

            // 项目类型
            case 2:
                mapping.setType(2);
                mapping.setSubType(null);
                mapping.setTypeName("项目");
                break;
            case 10:
                mapping.setType(2);
                mapping.setSubType(1);
                mapping.setTypeName("项目");
                mapping.setSubTypeName("国家重点项目");
                break;
            case 11:
                mapping.setType(2);
                mapping.setSubType(2);
                mapping.setTypeName("项目");
                mapping.setSubTypeName("国家一般项目");
                break;
            case 12:
                mapping.setType(2);
                mapping.setSubType(3);
                mapping.setTypeName("项目");
                mapping.setSubTypeName("省部级项目");
                break;
            case 13:
                mapping.setType(2);
                mapping.setSubType(4);
                mapping.setTypeName("项目");
                mapping.setSubTypeName("企业合作项目");
                break;
            case 14:
                mapping.setType(2);
                mapping.setSubType(5);
                mapping.setTypeName("项目");
                mapping.setSubTypeName("国际合作项目");
                break;
            case 15:
                mapping.setType(2);
                mapping.setSubType(6);
                mapping.setTypeName("项目");
                mapping.setSubTypeName("青年基金");
                break;
            case 16:
                mapping.setType(2);
                mapping.setSubType(7);
                mapping.setTypeName("项目");
                mapping.setSubTypeName("博士后基金");
                break;
            case 17:
                mapping.setType(2);
                mapping.setSubType(8);
                mapping.setTypeName("项目");
                mapping.setSubTypeName("其他项目");
                break;

            default:
                // 动态回退：查询分类表，判断是否为顶级或其上级是否为“论文/项目”顶级分类
                LabAchievementCategoryEntity cat = categoryService.getById(categoryId);
                if (cat == null) {
                    return null;
                }
                // 顶级分类：仅能确定大类（不允许写入，但用于兼容判断）
                if (cat.getParentId() == null) {
                    Integer topType = deduceTopType(cat);
                    if (topType != null) {
                        mapping.setType(topType);
                        mapping.setSubType(null);
                        mapping.setTypeName(topType == 1 ? "论文" : "项目");
                        return mapping;
                    }
                    return null;
                }
                // 二级分类：根据顶级父类（通过编码/名称推断）映射到“其他论文/其他项目”
                LabAchievementCategoryEntity parent = categoryService.getById(cat.getParentId());
                if (parent == null) {
                    return null;
                }
                // 在我们只支持两级的前提下，parent 即顶级
                LabAchievementCategoryEntity top = parent.getParentId() == null ? parent : categoryService.getById(parent.getParentId());
                Integer topType = deduceTopType(top);
                if (topType != null) {
                    mapping.setType(topType);
                    if (topType == 1) {
                        mapping.setSubType(7); // 其他论文
                        mapping.setTypeName("论文");
                        mapping.setSubTypeName("其他论文");
                    } else if (topType == 2) {
                        mapping.setSubType(8); // 其他项目
                        mapping.setTypeName("项目");
                        mapping.setSubTypeName("其他项目");
                    } else {
                        // topType == 3，其他类型
                        mapping.setSubType(9); // 其他成果
                        mapping.setTypeName("其他成果");
                        mapping.setSubTypeName(cat.getCategoryName());
                    }
                    return mapping;
                }
                return null;
        }

        return mapping;
    }

    /**
     * 获取新旧类型映射关系
     *
     * @return 映射关系Map
     */
    public Map<String, Object> getTypeMappings() {
        Map<String, Object> mappings = new HashMap<>();

        // 论文类型映射
        Map<String, Long> paperMappings = new HashMap<>();
        paperMappings.put("1-1", 3L); // 期刊论文
        paperMappings.put("1-2", 4L); // 会议论文
        paperMappings.put("1-3", 5L); // 书籍章节
        paperMappings.put("1-4", 6L); // 专利
        paperMappings.put("1-5", 7L); // 标准
        paperMappings.put("1-6", 8L); // 研究报告
        paperMappings.put("1-7", 9L); // 其他论文

        // 项目类型映射
        Map<String, Long> projectMappings = new HashMap<>();
        projectMappings.put("2-1", 10L); // 国家重点项目
        projectMappings.put("2-2", 11L); // 国家一般项目
        projectMappings.put("2-3", 12L); // 省部级项目
        projectMappings.put("2-4", 13L); // 企业合作项目
        projectMappings.put("2-5", 14L); // 国际合作项目
        projectMappings.put("2-6", 15L); // 青年基金
        projectMappings.put("2-7", 16L); // 博士后基金
        projectMappings.put("2-8", 17L); // 其他项目

        mappings.put("paperMappings", paperMappings);
        mappings.put("projectMappings", projectMappings);

        return mappings;
    }

    /**
     * 检查数据迁移状态
     *
     * @return 迁移状态信息
     */
    public Map<String, Object> checkMigrationStatus() {
        Map<String, Object> status = new HashMap<>();

        // 检查类型表是否存在数据
        long categoryCount = categoryService.count();
        status.put("categoryCount", categoryCount);

        // 检查是否有未迁移的成果数据
        // 这里可以添加具体的检查逻辑

        status.put("migrationCompleted", categoryCount > 0);

        return status;
    }

    /**
     * 推断顶级分类的大类（1=论文，2=项目，3=其他），不依赖固定ID。
     */
    private Integer deduceTopType(LabAchievementCategoryEntity top) {
        if (top == null) {
            return null;
        }
        String code = str(top.getCategoryCode()).toUpperCase();
        String name = str(top.getCategoryName()).toLowerCase();
        String nameEn = str(top.getCategoryNameEn()).toLowerCase();
        // 论文：code= PAPER* 或 名称包含“论文”/英文包含 paper
        if (code.equals("PAPER") || code.equals("PAPERS") || code.startsWith("PAPER")
            || name.contains("论文") || nameEn.contains("paper")) {
            return 1;
        }
        // 项目：code= PROJECT* 或 名称包含“项目”/英文包含 project
        if (code.equals("PROJECT") || code.equals("PROJECTS") || code.startsWith("PROJECT")
            || name.contains("项目") || nameEn.contains("project")) {
            return 2;
        }
        // 其他类型：统一映射为type=3，subType根据具体分类动态分配
        return 3;
    }

    private String str(Object v) {
        return v == null ? "" : v.toString().trim();
    }

    /**
     * 解析可写入的叶子分类ID：
     * - 若传入为二级分类，直接返回该ID；
     * - 若传入为一级分类，优先返回其下名称/编码匹配“其他(other)”的子类；若无匹配则返回第一个子类；
     * - 若不存在子类，返回 null。
     */
    public Long resolveWritableLeafCategoryId(Long categoryId) {
        LabAchievementCategoryEntity cat = categoryService.getById(categoryId);
        if (cat == null) {
            return null;
        }
        // 二级分类直接返回
        if (cat.getParentId() != null) {
            return cat.getId();
        }
        // 一级分类：找子类
        java.util.List<LabAchievementCategoryEntity> children = categoryService.getChildrenByParentId(cat.getId());
        if (children == null || children.isEmpty()) {
            return null;
        }
        // 优先匹配“其他/other”
        for (LabAchievementCategoryEntity c : children) {
            String code = c.getCategoryCode() == null ? "" : c.getCategoryCode().toUpperCase();
            String name = c.getCategoryName() == null ? "" : c.getCategoryName();
            String nameEn = c.getCategoryNameEn() == null ? "" : c.getCategoryNameEn().toLowerCase();
            if (name.contains("其他") || code.contains("OTHER") || nameEn.contains("other")) {
                return c.getId();
            }
        }
        // 其次返回第一个启用的，否则第一个
        for (LabAchievementCategoryEntity c : children) {
            if (Boolean.TRUE.equals(c.getIsActive())) {
                return c.getId();
            }
        }
        return children.get(0).getId();
    }
}
