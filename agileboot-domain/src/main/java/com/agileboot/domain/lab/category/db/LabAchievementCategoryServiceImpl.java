package com.agileboot.domain.lab.category.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 实验室成果类型Service实现类
 *
 * @author agileboot
 */
@Service
@RequiredArgsConstructor
public class LabAchievementCategoryServiceImpl extends ServiceImpl<LabAchievementCategoryMapper, LabAchievementCategoryEntity>
        implements LabAchievementCategoryService {

    @Override
    public List<LabAchievementCategoryEntity> getCategoryTree(boolean includeInactive) {
        List<LabAchievementCategoryEntity> allCategories;
        
        if (includeInactive) {
            // 查询所有未删除的类型
            allCategories = lambdaQuery()
                    .eq(LabAchievementCategoryEntity::getDeleted, false)
                    .orderByAsc(LabAchievementCategoryEntity::getSortOrder)
                    .list();
        } else {
            // 只查询启用的类型
            allCategories = baseMapper.selectActiveCategories();
        }

        return buildTree(allCategories);
    }

    @Override
    public List<LabAchievementCategoryEntity> getChildrenByParentId(Long parentId) {
        return baseMapper.selectByParentId(parentId);
    }

    @Override
    public List<LabAchievementCategoryEntity> getTopLevelCategories() {
        return baseMapper.selectTopLevelCategories();
    }

    @Override
    public LabAchievementCategoryEntity getByCategoryCode(String categoryCode) {
        return baseMapper.selectByCategoryCode(categoryCode);
    }

    @Override
    public boolean isCategoryCodeDuplicated(String categoryCode, Long excludeId) {
        if (excludeId == null) {
            excludeId = -1L; // 新增时使用一个不存在的ID
        }
        return baseMapper.existsByCategoryCode(categoryCode, excludeId);
    }

    @Override
    public boolean isCategoryNameDuplicated(Long parentId, String categoryName, Long excludeId) {
        if (excludeId == null) {
            excludeId = -1L; // 新增时使用一个不存在的ID
        }
        return baseMapper.existsByParentIdAndName(parentId, categoryName, excludeId);
    }

    @Override
    public boolean canDelete(Long categoryId) {
        LabAchievementCategoryEntity category = getById(categoryId);
        if (category == null) {
            return false;
        }

        // 系统内置类型不可删除
        if (Boolean.TRUE.equals(category.getIsSystem())) {
            return false;
        }

        // 有子类型的不可删除
        if (hasChildren(categoryId)) {
            return false;
        }

        // 被成果使用的不可删除
        if (isUsedByAchievements(categoryId)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean hasChildren(Long categoryId) {
        return baseMapper.hasChildren(categoryId);
    }

    @Override
    public boolean isUsedByAchievements(Long categoryId) {
        return baseMapper.isUsedByAchievements(categoryId);
    }

    @Override
    public Integer getNextSortOrder(Long parentId) {
        Integer maxSortOrder = baseMapper.getMaxSortOrderByParentId(parentId);
        return maxSortOrder + 1;
    }

    @Override
    public void batchUpdateSortOrder(List<LabAchievementCategoryEntity> categories) {
        if (categories != null && !categories.isEmpty()) {
            baseMapper.batchUpdateSortOrder(categories);
        }
    }

    @Override
    public boolean wouldCauseCyclicReference(Long categoryId, Long newParentId) {
        if (newParentId == null) {
            return false; // 移动到顶级，不会造成循环
        }

        if (categoryId.equals(newParentId)) {
            return true; // 不能将自己设为父类型
        }

        // 检查新的父类型是否是当前类型的后代
        List<Long> descendantIds = getDescendantIds(categoryId);
        return descendantIds.contains(newParentId);
    }

    @Override
    public List<Long> getAncestorIds(Long categoryId) {
        List<Long> ancestorIds = new ArrayList<>();
        LabAchievementCategoryEntity current = getById(categoryId);
        
        while (current != null && current.getParentId() != null) {
            ancestorIds.add(current.getParentId());
            current = getById(current.getParentId());
        }
        
        return ancestorIds;
    }

    @Override
    public List<Long> getDescendantIds(Long categoryId) {
        List<Long> descendantIds = new ArrayList<>();
        collectDescendantIds(categoryId, descendantIds);
        return descendantIds;
    }

    /**
     * 递归收集后代类型ID
     */
    private void collectDescendantIds(Long parentId, List<Long> descendantIds) {
        List<LabAchievementCategoryEntity> children = getChildrenByParentId(parentId);
        for (LabAchievementCategoryEntity child : children) {
            descendantIds.add(child.getId());
            collectDescendantIds(child.getId(), descendantIds);
        }
    }

    /**
     * 构建树形结构
     */
    private List<LabAchievementCategoryEntity> buildTree(List<LabAchievementCategoryEntity> allCategories) {
        // 按父ID分组
        Map<Long, List<LabAchievementCategoryEntity>> groupByParent = allCategories.stream()
                .collect(Collectors.groupingBy(
                        category -> category.getParentId() == null ? 0L : category.getParentId()
                ));

        // 获取顶级类型
        List<LabAchievementCategoryEntity> topLevelCategories = groupByParent.getOrDefault(0L, new ArrayList<>());

        // 为每个类型设置子类型
        setChildren(topLevelCategories, groupByParent);

        return topLevelCategories;
    }

    /**
     * 递归设置子类型
     */
    private void setChildren(List<LabAchievementCategoryEntity> categories, Map<Long, List<LabAchievementCategoryEntity>> groupByParent) {
        for (LabAchievementCategoryEntity category : categories) {
            List<LabAchievementCategoryEntity> children = groupByParent.getOrDefault(category.getId(), new ArrayList<>());
            category.setChildren(children);
            
            // 递归设置子类型的子类型
            if (!children.isEmpty()) {
                setChildren(children, groupByParent);
            }
        }
    }
}
