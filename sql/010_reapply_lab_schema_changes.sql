-- --------------------------------------------------------------------
-- 010_reapply_lab_schema_changes.sql
-- 适用场景：数据库回滚到 2025-11-05 之前的版本，需要重新套用
--         论文/项目拆分后的最新表结构以及作者表重命名。
-- 运行前提：已恢复 web_2025-11-05.sql，尚未执行本仓库 007+ 以后的
--         任意迁移脚本；确保当前库中不存在 lab_paper_author。
-- 使用方式：mysql -u${USER} -p${PWD} web < sql/010_reapply_lab_schema_changes.sql
-- --------------------------------------------------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 调整论文表结构，删除旧分类列并补充新字段
ALTER TABLE `lab_achievement_paper`
  DROP FOREIGN KEY `fk_lab_ach_paper_category`;

ALTER TABLE `lab_achievement_paper`
  DROP INDEX `idx_lab_ach_paper_category`;

ALTER TABLE `lab_achievement_paper`
  DROP COLUMN `category_id`;

ALTER TABLE `lab_achievement_paper`
  CHANGE COLUMN `paper_type` `paper_type_id` INT UNSIGNED DEFAULT NULL COMMENT '论文类型（lab_type.PAPERTYPE）',
  CHANGE COLUMN `venue` `publication` VARCHAR(500) DEFAULT NULL COMMENT '期刊/会议/出版物名称';

ALTER TABLE `lab_achievement_paper`
  ADD COLUMN `author_text` VARCHAR(1000) DEFAULT NULL COMMENT '作者原始字符串' AFTER `paper_type_id`,
  ADD COLUMN `conference_name` VARCHAR(255) DEFAULT NULL COMMENT '会议名称' AFTER `publication`,
  ADD COLUMN `issue_number` VARCHAR(255) DEFAULT NULL COMMENT '卷(期)/编号' AFTER `conference_name`,
  ADD COLUMN `publication_place` VARCHAR(255) DEFAULT NULL COMMENT '会议地点' AFTER `issue_number`,
  ADD COLUMN `publisher` VARCHAR(255) DEFAULT NULL COMMENT '出版社/授权单位' AFTER `publication_place`,
  ADD COLUMN `page_range` VARCHAR(100) DEFAULT NULL COMMENT '页码' AFTER `publisher`,
  ADD COLUMN `publish_date_display` VARCHAR(20) DEFAULT NULL COMMENT '展示用日期字符串（年/年月/年月日）' AFTER `publish_date`,
  ADD COLUMN `publish_date_precision` TINYINT DEFAULT 0 COMMENT '日期精度：1=年、2=年月、3=年月日' AFTER `publish_date_display`,
  ADD COLUMN `extra_url` VARCHAR(500) DEFAULT NULL COMMENT '其它链接/备用URL' AFTER `link_url`,
  ADD COLUMN `notes` VARCHAR(1000) DEFAULT NULL COMMENT '其它信息/备注' AFTER `doi`;

ALTER TABLE `lab_achievement_paper`
  ADD INDEX `idx_lab_paper_type` (`paper_type_id`, `publish_date`);

-- 2. 调整项目表结构，删除旧分类列并补充新字段
ALTER TABLE `lab_achievement_project`
  DROP FOREIGN KEY `fk_lab_project_category`;

ALTER TABLE `lab_achievement_project`
  DROP INDEX `idx_lab_project_category`;

ALTER TABLE `lab_achievement_project`
  DROP COLUMN `category_id`;

ALTER TABLE `lab_achievement_project`
  CHANGE COLUMN `project_type` `project_type_id` INT UNSIGNED DEFAULT NULL COMMENT '项目类型（lab_type.FUNDTYPE）';

ALTER TABLE `lab_achievement_project`
  ADD COLUMN `project_number` VARCHAR(255) DEFAULT NULL COMMENT '项目号' AFTER `project_type_id`,
  ADD COLUMN `supporter` VARCHAR(255) DEFAULT NULL COMMENT '资助单位/主办方' AFTER `project_number`,
  ADD COLUMN `amount_display` VARCHAR(100) DEFAULT NULL COMMENT '金额展示字符串' AFTER `funding_amount`,
  ADD COLUMN `support_cn` VARCHAR(500) DEFAULT NULL COMMENT '资助说明（中文）' AFTER `amount_display`,
  ADD COLUMN `support_en` VARCHAR(500) DEFAULT NULL COMMENT '资助说明（英文）' AFTER `support_cn`,
  ADD COLUMN `requirement` VARCHAR(1000) DEFAULT NULL COMMENT '结项成果要求' AFTER `support_en`,
  ADD COLUMN `desc_text` VARCHAR(1000) DEFAULT NULL COMMENT '其它信息/备注' AFTER `requirement`;

ALTER TABLE `lab_achievement_project`
  ADD INDEX `idx_lab_project_type` (`project_type_id`, `project_start_date`);

-- 3. 将成果作者表重命名为论文作者表，并关联到论文明细
RENAME TABLE `lab_achievement_author` TO `lab_paper_author`;

ALTER TABLE `lab_paper_author`
  CHANGE COLUMN `achievement_id` `paper_id` BIGINT NOT NULL COMMENT '论文ID';

ALTER TABLE `lab_paper_author`
  DROP INDEX `uniq_achievement_author_order`,
  ADD UNIQUE KEY `uniq_paper_author_order` (`paper_id`, `author_order`, `deleted`),
  DROP INDEX `uniq_achievement_user`,
  ADD UNIQUE KEY `uniq_paper_user` (`paper_id`, `user_id`),
  DROP INDEX `idx_lab_ach_author_ach_deleted`,
  ADD KEY `idx_lab_paper_author_deleted` (`paper_id`, `deleted`);

ALTER TABLE `lab_paper_author`
  DROP FOREIGN KEY `fk_author_achievement`,
  ADD CONSTRAINT `fk_paper_author_paper` FOREIGN KEY (`paper_id`)
    REFERENCES `lab_achievement_paper` (`id`) ON DELETE CASCADE;

-- 4. 基金-论文关联补充对论文表的外键（旧库中缺失）
ALTER TABLE `lab_fund_paper_rel`
  ADD CONSTRAINT `fk_fund_paper_rel_paper` FOREIGN KEY (`paper_id`)
    REFERENCES `lab_achievement_paper` (`id`) ON DELETE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;
