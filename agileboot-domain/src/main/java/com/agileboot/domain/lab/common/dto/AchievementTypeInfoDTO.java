package com.agileboot.domain.lab.common.dto;

import lombok.Data;

/**
 * 统一的成果类型描述（兼容 legacy type 与新分类）
 */
@Data
public class AchievementTypeInfoDTO {

    /**
     * 旧的成果类型（1=论文，2=项目）
     */
    private Integer legacyType;

    /**
     * 旧的子类型（论文1-7 / 项目1-8）
     */
    private Integer legacySubType;

    /**
     * 旧子类型描述
     */
    private String legacySubTypeDesc;

    /**
     * 新的分类ID（lab_achievement_category）
     */
    private Long categoryId;

    /**
     * 新分类名称
     */
    private String categoryName;

    /**
     * 新分类完整路径（如：项目 / 横向）
     */
    private String categoryFullPath;
}
