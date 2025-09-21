-- 更新类型约束以支持灵活的分类体系
-- 执行前请确保已完成 003_create_lab_achievement_category.sql

-- 删除旧的类型字段组合约束
ALTER TABLE `lab_achievement` DROP CONSTRAINT `chk_type_fields_combo`;

-- 添加新的约束，支持 type=3（其他成果类型）
ALTER TABLE `lab_achievement` 
ADD CONSTRAINT `chk_type_fields_combo` CHECK (
  (
    -- 论文类型 (type=1)
    `type` = 1 AND `paper_type` BETWEEN 1 AND 7 AND `project_type` IS NULL
    AND `publish_date` IS NOT NULL
    AND `project_start_date` IS NULL AND `project_end_date` IS NULL
  )
  OR
  (
    -- 项目类型 (type=2)  
    `type` = 2 AND `project_type` BETWEEN 1 AND 8 AND `paper_type` IS NULL
    AND `publish_date` IS NULL
    AND `project_start_date` IS NOT NULL
    AND (`project_end_date` IS NULL OR `project_end_date` >= `project_start_date`)
  )
  OR
  (
    -- 其他成果类型 (type=3)：较为宽松的约束
    `type` = 3 AND `paper_type` IS NULL AND `project_type` IS NULL
    -- 其他类型允许灵活配置日期字段，不强制要求
  )
);
