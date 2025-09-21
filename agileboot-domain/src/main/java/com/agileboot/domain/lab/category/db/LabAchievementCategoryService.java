package com.agileboot.domain.lab.category.db;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 实验室成果类型Service接口
 *
 * @author agileboot
 */
public interface LabAchievementCategoryService extends IService<LabAchievementCategoryEntity> {

    /**
     * 获取类型树形结构
     *
     * @param includeInactive 是否包含未启用的类型
     * @return 类型树
     */
    List<LabAchievementCategoryEntity> getCategoryTree(boolean includeInactive);

    /**
     * 根据父ID获取子类型
     *
     * @param parentId 父类型ID
     * @return 子类型列表
     */
    List<LabAchievementCategoryEntity> getChildrenByParentId(Long parentId);

    /**
     * 获取一级类型
     *
     * @return 一级类型列表
     */
    List<LabAchievementCategoryEntity> getTopLevelCategories();

    /**
     * 根据类型编码获取类型
     *
     * @param categoryCode 类型编码
     * @return 类型实体
     */
    LabAchievementCategoryEntity getByCategoryCode(String categoryCode);

    /**
     * 检查类型编码是否重复
     *
     * @param categoryCode 类型编码
     * @param excludeId 排除的ID
     * @return 是否重复
     */
    boolean isCategoryCodeDuplicated(String categoryCode, Long excludeId);

    /**
     * 检查同一父类型下名称是否重复
     *
     * @param parentId 父类型ID
     * @param categoryName 类型名称
     * @param excludeId 排除的ID
     * @return 是否重复
     */
    boolean isCategoryNameDuplicated(Long parentId, String categoryName, Long excludeId);

    /**
     * 检查是否可以删除
     *
     * @param categoryId 类型ID
     * @return 是否可以删除
     */
    boolean canDelete(Long categoryId);

    /**
     * 检查是否有子类型
     *
     * @param categoryId 类型ID
     * @return 是否有子类型
     */
    boolean hasChildren(Long categoryId);

    /**
     * 检查是否被成果使用
     *
     * @param categoryId 类型ID
     * @return 是否被使用
     */
    boolean isUsedByAchievements(Long categoryId);

    /**
     * 获取下一个排序号
     *
     * @param parentId 父类型ID
     * @return 下一个排序号
     */
    Integer getNextSortOrder(Long parentId);

    /**
     * 批量更新排序
     *
     * @param categories 类型列表
     */
    void batchUpdateSortOrder(List<LabAchievementCategoryEntity> categories);

    /**
     * 检查是否会造成循环引用
     *
     * @param categoryId 类型ID
     * @param newParentId 新的父类型ID
     * @return 是否会造成循环引用
     */
    boolean wouldCauseCyclicReference(Long categoryId, Long newParentId);

    /**
     * 获取所有祖先类型ID
     *
     * @param categoryId 类型ID
     * @return 祖先类型ID列表
     */
    List<Long> getAncestorIds(Long categoryId);

    /**
     * 获取所有后代类型ID
     *
     * @param categoryId 类型ID
     * @return 后代类型ID列表
     */
    List<Long> getDescendantIds(Long categoryId);
}
