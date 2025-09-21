package com.agileboot.domain.lab.achievement.service;

import com.agileboot.domain.lab.category.LabAchievementCategoryApplicationService;
import com.agileboot.domain.lab.category.dto.LabAchievementCategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成果类型服务
 * 用于处理成果与类型的关联逻辑
 *
 * @author agileboot
 */
@Service
@RequiredArgsConstructor
public class AchievementCategoryService {

    private final LabAchievementCategoryApplicationService categoryApplicationService;

    /**
     * 获取类型ID到名称的映射
     */
    public Map<Long, String> getCategoryNameMap() {
        List<LabAchievementCategoryDTO> categories = categoryApplicationService.getCategoryTree(true);
        Map<Long, String> nameMap = new HashMap<>();
        
        for (LabAchievementCategoryDTO category : categories) {
            nameMap.put(category.getId(), category.getCategoryName());
            if (category.getChildren() != null) {
                for (LabAchievementCategoryDTO child : category.getChildren()) {
                    nameMap.put(child.getId(), child.getCategoryName());
                }
            }
        }
        
        return nameMap;
    }

    /**
     * 获取类型ID到完整路径的映射
     */
    public Map<Long, String> getCategoryFullPathMap() {
        List<LabAchievementCategoryDTO> categories = categoryApplicationService.getCategoryTree(true);
        Map<Long, String> pathMap = new HashMap<>();
        
        for (LabAchievementCategoryDTO category : categories) {
            pathMap.put(category.getId(), category.getCategoryName());
            if (category.getChildren() != null) {
                for (LabAchievementCategoryDTO child : category.getChildren()) {
                    pathMap.put(child.getId(), category.getCategoryName() + " > " + child.getCategoryName());
                }
            }
        }
        
        return pathMap;
    }

    /**
     * 根据类型ID获取类型名称
     */
    public String getCategoryName(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        
        try {
            LabAchievementCategoryDTO category = categoryApplicationService.getCategoryById(categoryId);
            return category != null ? category.getCategoryName() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据类型ID获取完整路径
     */
    public String getCategoryFullPath(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        
        try {
            LabAchievementCategoryDTO category = categoryApplicationService.getCategoryById(categoryId);
            return category != null ? category.getFullPath() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
