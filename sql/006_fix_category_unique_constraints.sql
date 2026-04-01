-- 修复分类表的唯一约束，允许多次软删除同一个编码
-- 执行前请确保已完成 003_create_lab_achievement_category.sql

-- 删除旧的唯一约束
ALTER TABLE `lab_achievement_category` DROP INDEX `uk_code_deleted`;
ALTER TABLE `lab_achievement_category` DROP INDEX `uk_parent_name_deleted`;

-- 添加新的唯一约束：只对未删除的记录生效
-- 注意：MySQL 5.7+ 支持函数索引，MySQL 8.0+ 更好支持
-- 如果是 MySQL 5.6，可能需要调整语法

-- 方案1：使用条件唯一索引（MySQL 8.0+）
-- CREATE UNIQUE INDEX `uk_code_active` ON `lab_achievement_category` (`category_code`) WHERE `deleted` = 0;
-- CREATE UNIQUE INDEX `uk_parent_name_active` ON `lab_achievement_category` (`parent_id`, `category_name`) WHERE `deleted` = 0;

-- 方案2：兼容性更好的方案，使用触发器或应用层控制
-- 这里我们先移除约束，在应用层控制唯一性

-- 添加普通索引以提高查询性能
CREATE INDEX `idx_code_deleted` ON `lab_achievement_category` (`category_code`, `deleted`);
CREATE INDEX `idx_parent_name_deleted` ON `lab_achievement_category` (`parent_id`, `category_name`, `deleted`);
