package com.agileboot.domain.lab.category;

import com.agileboot.common.exception.ApiException;
import com.agileboot.domain.lab.category.db.LabAchievementCategoryEntity;
import com.agileboot.domain.lab.category.db.LabAchievementCategoryService;
import com.agileboot.domain.lab.category.dto.LabAchievementCategoryDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 成果类型应用服务测试
 *
 * @author agileboot
 */
@ExtendWith(MockitoExtension.class)
class LabAchievementCategoryApplicationServiceTest {

    @Mock
    private LabAchievementCategoryService categoryService;

    @InjectMocks
    private LabAchievementCategoryApplicationService applicationService;

    @Test
    void testGetCategoryTree() {
        // 准备测试数据
        LabAchievementCategoryEntity parent = new LabAchievementCategoryEntity();
        parent.setId(1L);
        parent.setCategoryCode("PAPER");
        parent.setCategoryName("论文");
        parent.setParentId(null);
        parent.setIsActive(true);

        LabAchievementCategoryEntity child = new LabAchievementCategoryEntity();
        child.setId(2L);
        child.setCategoryCode("JOURNAL_PAPER");
        child.setCategoryName("期刊论文");
        child.setParentId(1L);
        child.setIsActive(true);

        List<LabAchievementCategoryEntity> mockData = Arrays.asList(parent, child);

        // 模拟服务调用
        when(categoryService.getCategoryTree(false)).thenReturn(mockData);

        // 执行测试
        List<LabAchievementCategoryDTO> result = applicationService.getCategoryTree(false);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("PAPER", result.get(0).getCategoryCode());
        assertEquals("论文", result.get(0).getCategoryName());
        assertEquals("JOURNAL_PAPER", result.get(1).getCategoryCode());
        assertEquals("期刊论文", result.get(1).getCategoryName());

        // 验证服务调用
        verify(categoryService, times(1)).getCategoryTree(false);
    }

    @Test
    void testGetCategoryById() {
        // 准备测试数据
        Long categoryId = 1L;
        LabAchievementCategoryEntity entity = new LabAchievementCategoryEntity();
        entity.setId(categoryId);
        entity.setCategoryCode("PAPER");
        entity.setCategoryName("论文");
        entity.setIsActive(true);

        // 模拟服务调用
        when(categoryService.getById(categoryId)).thenReturn(entity);
        when(categoryService.hasChildren(categoryId)).thenReturn(true);
        when(categoryService.isUsedByAchievements(categoryId)).thenReturn(false);

        // 执行测试
        LabAchievementCategoryDTO result = applicationService.getCategoryById(categoryId);

        // 验证结果
        assertNotNull(result);
        assertEquals(categoryId, result.getId());
        assertEquals("PAPER", result.getCategoryCode());
        assertEquals("论文", result.getCategoryName());
        assertTrue(result.getHasChildren());
        assertFalse(result.getIsUsed());

        // 验证服务调用
        verify(categoryService, times(1)).getById(categoryId);
        verify(categoryService, times(1)).hasChildren(categoryId);
        verify(categoryService, times(1)).isUsedByAchievements(categoryId);
    }

    @Test
    void testDeleteCategoryShouldRewriteUniqueFieldsBeforeSoftDelete() {
        Long categoryId = 55L;
        LabAchievementCategoryEntity entity = new LabAchievementCategoryEntity();
        entity.setId(categoryId);
        entity.setParentId(2L);
        entity.setCategoryCode("PROJECT_CAT");
        entity.setCategoryName("测试2");
        entity.setIsSystem(false);

        when(categoryService.getById(categoryId)).thenReturn(entity);
        when(categoryService.isUsedByAchievements(categoryId)).thenReturn(false);
        when(categoryService.hasChildren(categoryId)).thenReturn(false);

        applicationService.deleteCategory(categoryId);

        verify(categoryService, times(1)).updateById(entity);
        verify(categoryService, times(1)).removeById(categoryId);
        assertTrue(entity.getCategoryCode().contains("_deleted_" + categoryId));
        assertTrue(entity.getCategoryName().contains("_deleted_" + categoryId));
    }

    @Test
    void testDeleteCategoryShouldRejectWhenHasChildren() {
        Long categoryId = 54L;
        LabAchievementCategoryEntity entity = new LabAchievementCategoryEntity();
        entity.setId(categoryId);
        entity.setIsSystem(false);

        when(categoryService.getById(categoryId)).thenReturn(entity);
        when(categoryService.isUsedByAchievements(categoryId)).thenReturn(false);
        when(categoryService.hasChildren(categoryId)).thenReturn(true);

        assertThrows(ApiException.class, () -> applicationService.deleteCategory(categoryId));
        verify(categoryService, never()).removeById(anyLong());
    }
}
