-- 新增字段（不包含 CHECK 约束），先执行本脚本；完成后再执行 002_add_checks_lab_achievement.sql
ALTER TABLE `lab_achievement`
  ADD COLUMN `publish_date` DATE NULL COMMENT '发表/公开日期（论文用）' AFTER `venue`,
  ADD COLUMN `project_start_date` DATE NULL COMMENT '项目开始日期（项目用）' AFTER `publish_date`,
  ADD COLUMN `project_end_date` DATE NULL COMMENT '项目结束日期（项目用，可空=进行中）' AFTER `project_start_date`,
  ADD COLUMN `git_url` VARCHAR(500) NULL COMMENT 'Git 仓库地址' AFTER `cover_url`,
  ADD COLUMN `homepage_url` VARCHAR(500) NULL COMMENT '主页/展示页地址' AFTER `git_url`,
  ADD COLUMN `pdf_url` VARCHAR(500) NULL COMMENT 'PDF 下载地址' AFTER `homepage_url`,
  ADD COLUMN `funding_amount` DECIMAL(12,2) NULL COMMENT '项目经费（万元）' AFTER `pdf_url`,
  ADD COLUMN `doi` VARCHAR(128) NULL COMMENT '论文 DOI' AFTER `funding_amount`,
  ADD COLUMN `extra` JSON NULL COMMENT '扩展信息（保留字段）' AFTER `doi`;

-- 补充索引（按需调整）
ALTER TABLE `lab_achievement`
  ADD INDEX `idx_type_publish_date` (`type`, `publish_date`),
  ADD INDEX `idx_type_project_dates` (`type`, `project_start_date`, `project_end_date`),
  ADD INDEX `idx_published_publish_date` (`published`, `publish_date`),
  ADD INDEX `idx_doi` (`doi`);

