-- 008_remove_lab_achievement.sql
-- 目标：
--   1. 让 lab_achievement_paper / lab_achievement_project 成为独立主表，字段覆盖 Excel 设计。
--   2. 删除 lab_achievement 及其分类表，简化成果模型。
--   3. 为论文/项目之间的关联和作者信息建立独立表结构。

START TRANSACTION;

-- 1) 解除对子表的外键依赖
ALTER TABLE `lab_achievement_paper`
  DROP FOREIGN KEY `fk_lab_ach_paper_achievement`,
  DROP FOREIGN KEY `fk_lab_ach_paper_category`;

ALTER TABLE `lab_achievement_project`
  DROP FOREIGN KEY `fk_lab_project_achievement`,
  DROP FOREIGN KEY `fk_lab_project_category`;

-- 2) 重构论文表字段
ALTER TABLE `lab_achievement_paper`
  DROP INDEX `idx_lab_ach_paper_category`,
  DROP COLUMN `category_id`,
  MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '论文ID',
  CHANGE COLUMN `paper_type` `paper_type_id` int unsigned DEFAULT NULL COMMENT '论文类型（lab_type.PAPERTYPE）',
  CHANGE COLUMN `venue` `publication` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '期刊/会议/出版物名称',
  ADD COLUMN `author_text` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '作者原始字符串（Excel author）' AFTER `paper_type_id`,
  ADD COLUMN `conference_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '会议名称' AFTER `publication`,
  ADD COLUMN `issue_number` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '卷(期)/编号' AFTER `conference_name`,
  ADD COLUMN `publication_place` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '会议地点' AFTER `issue_number`,
  ADD COLUMN `publisher` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '出版社/授权单位' AFTER `publication_place`,
  ADD COLUMN `page_range` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '页码' AFTER `publisher`,
  ADD COLUMN `extra_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '其它链接（Excel URL）' AFTER `link_url`,
  ADD COLUMN `notes` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '其它信息/备注' AFTER `doi`,
  ADD COLUMN `publish_date_display` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '前端展示的日期字符串（年/年月/年月日）' AFTER `publish_date`,
  ADD COLUMN `publish_date_precision` tinyint DEFAULT 0 COMMENT '日期精度：1=年、2=年月、3=年月日' AFTER `publish_date_display`,
  ADD INDEX `idx_lab_paper_type` (`paper_type_id`,`publish_date`);

-- 3) 重构项目（基金）表字段
ALTER TABLE `lab_achievement_project`
  DROP INDEX `idx_lab_project_category`,
  DROP COLUMN `category_id`,
  MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  CHANGE COLUMN `project_type` `project_type_id` int unsigned DEFAULT NULL COMMENT '基金分类（lab_type.FUNDTYPE）',
  ADD COLUMN `project_number` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '项目号' AFTER `project_type_id`,
  ADD COLUMN `supporter` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '资助单位/主办方' AFTER `project_number`,
  ADD COLUMN `amount_display` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '金额展示字符串（含单位/自定义格式）' AFTER `funding_amount`,
  ADD COLUMN `support_cn` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '资助说明（中文）' AFTER `amount_display`,
  ADD COLUMN `support_en` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '资助说明（英文）' AFTER `support_cn`,
  ADD COLUMN `requirement` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '结项成果要求' AFTER `support_en`,
  ADD COLUMN `desc_text` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '其它信息/备注' AFTER `requirement`,
  ADD INDEX `idx_lab_project_type` (`project_type_id`,`project_start_date`);

-- 4) 论文作者表：改名并指向论文主表
RENAME TABLE `lab_achievement_author` TO `lab_paper_author`;

ALTER TABLE `lab_paper_author`
  CHANGE COLUMN `achievement_id` `paper_id` bigint NOT NULL COMMENT '论文ID',
  DROP INDEX `uniq_achievement_author_order`,
  ADD UNIQUE KEY `uniq_paper_author_order` (`paper_id`,`author_order`,`deleted`),
  DROP INDEX `uniq_achievement_user`,
  ADD UNIQUE KEY `uniq_paper_user` (`paper_id`,`user_id`),
  DROP INDEX `idx_lab_ach_author_ach_deleted`,
  ADD KEY `idx_lab_paper_author_deleted` (`paper_id`,`deleted`),
  DROP FOREIGN KEY `fk_author_achievement`,
  ADD CONSTRAINT `fk_paper_author_paper` FOREIGN KEY (`paper_id`) REFERENCES `lab_achievement_paper` (`id`) ON DELETE CASCADE;

-- 5) 成果-项目关联表（基金关联）
CREATE TABLE IF NOT EXISTS `lab_project_paper_rel` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint NOT NULL COMMENT '关联项目ID（lab_achievement_project.id）',
  `paper_id` bigint NOT NULL COMMENT '关联论文ID（lab_achievement_paper.id）',
  `support_role` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '资助角色/类型',
  `support_amount` decimal(12,2) DEFAULT NULL COMMENT '资助金额（万元）',
  `note` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `creator_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updater_id` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_project_paper` (`project_id`,`paper_id`),
  CONSTRAINT `fk_project_paper_project` FOREIGN KEY (`project_id`) REFERENCES `lab_achievement_project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_project_paper_paper` FOREIGN KEY (`paper_id`) REFERENCES `lab_achievement_paper` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目与成果（论文）关联';

-- 6) 删除分类/父表
DROP TABLE IF EXISTS `lab_achievement_category_bk_20250918_124009`;
DROP TABLE IF EXISTS `lab_achievement_category`;
DROP TABLE IF EXISTS `lab_achievement`;

COMMIT;
