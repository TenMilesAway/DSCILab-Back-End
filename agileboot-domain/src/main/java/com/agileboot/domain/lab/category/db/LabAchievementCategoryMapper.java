package com.agileboot.domain.lab.category.db;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 实验室成果类型Mapper接口
 *
 * @author agileboot
 */
@Mapper
public interface LabAchievementCategoryMapper extends BaseMapper<LabAchievementCategoryEntity> {

    /**
     * 查询所有启用的类型（树形结构）
     *
     * @return 类型列表
     */
    @Select("SELECT * FROM lab_achievement_category WHERE is_active = 1 AND deleted = 0 ORDER BY sort_order ASC")
    List<LabAchievementCategoryEntity> selectActiveCategories();

    /**
     * 根据父ID查询子类型
     *
     * @param parentId 父类型ID
     * @return 子类型列表
     */
    @Select("SELECT * FROM lab_achievement_category WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort_order ASC")
    List<LabAchievementCategoryEntity> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询一级类型
     *
     * @return 一级类型列表
     */
    @Select("SELECT * FROM lab_achievement_category WHERE parent_id IS NULL AND deleted = 0 ORDER BY sort_order ASC")
    List<LabAchievementCategoryEntity> selectTopLevelCategories();

    /**
     * 根据类型编码查询
     *
     * @param categoryCode 类型编码
     * @return 类型实体
     */
    @Select("SELECT * FROM lab_achievement_category WHERE category_code = #{categoryCode} AND deleted = 0")
    LabAchievementCategoryEntity selectByCategoryCode(@Param("categoryCode") String categoryCode);

    /**
     * 检查类型编码是否存在
     *
     * @param categoryCode 类型编码
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) > 0 FROM lab_achievement_category WHERE category_code = #{categoryCode} AND deleted = 0 AND id != #{excludeId}")
    boolean existsByCategoryCode(@Param("categoryCode") String categoryCode, @Param("excludeId") Long excludeId);

    /**
     * 检查同一父类型下名称是否存在
     *
     * @param parentId 父类型ID
     * @param categoryName 类型名称
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) > 0 FROM lab_achievement_category WHERE parent_id = #{parentId} AND category_name = #{categoryName} AND deleted = 0 AND id != #{excludeId}")
    boolean existsByParentIdAndName(@Param("parentId") Long parentId, @Param("categoryName") String categoryName, @Param("excludeId") Long excludeId);

    /**
     * 检查是否有子类型
     *
     * @param parentId 父类型ID
     * @return 是否有子类型
     */
    @Select("SELECT COUNT(1) > 0 FROM lab_achievement_category WHERE parent_id = #{parentId} AND deleted = 0")
    boolean hasChildren(@Param("parentId") Long parentId);

    /**
     * 检查类型是否被成果使用
     *
     * @param categoryId 类型ID
     * @return 是否被使用
     */
    @Select("SELECT COUNT(1) > 0 FROM lab_achievement WHERE category_id = #{categoryId} AND deleted = 0")
    boolean isUsedByAchievements(@Param("categoryId") Long categoryId);

    /**
     * 获取同级类型的最大排序号
     *
     * @param parentId 父类型ID
     * @return 最大排序号
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM lab_achievement_category WHERE parent_id = #{parentId} AND deleted = 0")
    Integer getMaxSortOrderByParentId(@Param("parentId") Long parentId);

    /**
     * 批量更新排序
     *
     * @param categories 类型列表
     */
    void batchUpdateSortOrder(@Param("categories") List<LabAchievementCategoryEntity> categories);
}
