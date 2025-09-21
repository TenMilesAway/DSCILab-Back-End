-- 移除旧的类型字段约束，实现纯 categoryId 分类系统
-- 执行前请确保已完成 003_create_lab_achievement_category.sql

-- 删除旧的类型字段组合约束
ALTER TABLE `lab_achievement` DROP CONSTRAINT IF EXISTS `chk_type_fields_combo`;

-- 删除单独的论文和项目字段约束（如果存在）
ALTER TABLE `lab_achievement` DROP CONSTRAINT IF EXISTS `chk_paper_fields`;
ALTER TABLE `lab_achievement` DROP CONSTRAINT IF EXISTS `chk_project_fields`;

-- 保留有意义的约束
-- ALTER TABLE `lab_achievement` DROP CONSTRAINT IF EXISTS `chk_funding_non_negative`; -- 保留这个

-- 可选：将旧字段设为可空，减少约束
-- 注意：如果有历史数据依赖这些字段，请谨慎执行
-- ALTER TABLE `lab_achievement` MODIFY COLUMN `type` INT NULL;
-- ALTER TABLE `lab_achievement` MODIFY COLUMN `paper_type` INT NULL;  
-- ALTER TABLE `lab_achievement` MODIFY COLUMN `project_type` INT NULL;

-- 添加 categoryId 的外键约束（如果还没有）
ALTER TABLE `lab_achievement` 
ADD CONSTRAINT `fk_achievement_category` 
FOREIGN KEY (`category_id`) REFERENCES `lab_achievement_category` (`id`)
ON DELETE RESTRICT ON UPDATE CASCADE;
