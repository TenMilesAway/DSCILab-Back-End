-- 创建成果类型管理表
-- 用于支持动态的成果类型管理，替代硬编码的类型枚举

CREATE TABLE IF NOT EXISTS `lab_achievement_category` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '类型ID',
  `parent_id` BIGINT NULL COMMENT '父类型ID，NULL表示一级类型',
  `category_code` VARCHAR(50) NOT NULL COMMENT '类型编码（用于兼容现有逻辑）',
  `category_name` VARCHAR(100) NOT NULL COMMENT '类型名称',
  `category_name_en` VARCHAR(100) NULL COMMENT '英文名称',
  `description` TEXT NULL COMMENT '类型描述',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `is_system` BOOLEAN DEFAULT FALSE COMMENT '是否系统内置类型（不可删除）',
  `is_active` BOOLEAN DEFAULT TRUE COMMENT '是否启用',
  `icon` VARCHAR(100) NULL COMMENT '图标',
  `color` VARCHAR(20) NULL COMMENT '颜色标识',
  
  -- 审计字段
  `creator_id` BIGINT NULL COMMENT '创建者ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater_id` BIGINT NULL COMMENT '更新者ID', 
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BOOLEAN DEFAULT FALSE COMMENT '逻辑删除标记',
  
  -- 索引
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_category_code` (`category_code`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_is_active` (`is_active`),
  KEY `idx_deleted` (`deleted`),
  
  -- 唯一约束
  UNIQUE KEY `uk_code_deleted` (`category_code`, `deleted`),
  UNIQUE KEY `uk_parent_name_deleted` (`parent_id`, `category_name`, `deleted`),
  
  -- 外键约束
  CONSTRAINT `fk_category_parent` FOREIGN KEY (`parent_id`) REFERENCES `lab_achievement_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='成果类型表';

-- 插入一级类型（兼容现有数据）
INSERT INTO `lab_achievement_category` 
(`id`, `parent_id`, `category_code`, `category_name`, `category_name_en`, `sort_order`, `is_system`, `is_active`) 
VALUES 
(1, NULL, 'PAPER', '论文', 'Paper', 1, TRUE, TRUE),
(2, NULL, 'PROJECT', '项目', 'Project', 2, TRUE, TRUE);

-- 插入论文二级类型（兼容现有paper_type 1-7）
INSERT INTO `lab_achievement_category` 
(`parent_id`, `category_code`, `category_name`, `category_name_en`, `sort_order`, `is_system`, `is_active`) 
VALUES 
(1, 'PAPER_JOURNAL', '期刊论文', 'Journal Paper', 1, TRUE, TRUE),
(1, 'PAPER_CONFERENCE', '会议论文', 'Conference Paper', 2, TRUE, TRUE),
(1, 'PAPER_BOOK_CHAPTER', '书籍章节', 'Book Chapter', 3, TRUE, TRUE),
(1, 'PAPER_PATENT', '专利', 'Patent', 4, TRUE, TRUE),
(1, 'PAPER_STANDARD', '标准', 'Standard', 5, TRUE, TRUE),
(1, 'PAPER_REPORT', '研究报告', 'Research Report', 6, TRUE, TRUE),
(1, 'PAPER_OTHER', '其他论文', 'Other Paper', 7, TRUE, TRUE);

-- 插入项目二级类型（兼容现有project_type 1-8）
INSERT INTO `lab_achievement_category` 
(`parent_id`, `category_code`, `category_name`, `category_name_en`, `sort_order`, `is_system`, `is_active`) 
VALUES 
(2, 'PROJECT_NATIONAL_KEY', '国家重点项目', 'National Key Project', 1, TRUE, TRUE),
(2, 'PROJECT_NATIONAL_GENERAL', '国家一般项目', 'National General Project', 2, TRUE, TRUE),
(2, 'PROJECT_PROVINCIAL', '省部级项目', 'Provincial Project', 3, TRUE, TRUE),
(2, 'PROJECT_ENTERPRISE', '企业合作项目', 'Enterprise Cooperation Project', 4, TRUE, TRUE),
(2, 'PROJECT_INTERNATIONAL', '国际合作项目', 'International Cooperation Project', 5, TRUE, TRUE),
(2, 'PROJECT_YOUTH_FUND', '青年基金', 'Youth Fund', 6, TRUE, TRUE),
(2, 'PROJECT_POSTDOC_FUND', '博士后基金', 'Postdoctoral Fund', 7, TRUE, TRUE),
(2, 'PROJECT_OTHER', '其他项目', 'Other Project', 8, TRUE, TRUE);

-- 为lab_achievement表添加category_id字段（兼容性字段）
ALTER TABLE `lab_achievement` ADD COLUMN `category_id` BIGINT NULL COMMENT '成果类型ID（新类型系统）' AFTER `project_type`;
ALTER TABLE `lab_achievement` ADD KEY `idx_category_id` (`category_id`);

-- 数据迁移：将现有成果数据映射到新的类型系统
-- 论文类型映射
UPDATE `lab_achievement` SET `category_id` = 3 WHERE `type` = 1 AND `paper_type` = 1; -- 期刊论文
UPDATE `lab_achievement` SET `category_id` = 4 WHERE `type` = 1 AND `paper_type` = 2; -- 会议论文
UPDATE `lab_achievement` SET `category_id` = 5 WHERE `type` = 1 AND `paper_type` = 3; -- 书籍章节
UPDATE `lab_achievement` SET `category_id` = 6 WHERE `type` = 1 AND `paper_type` = 4; -- 专利
UPDATE `lab_achievement` SET `category_id` = 7 WHERE `type` = 1 AND `paper_type` = 5; -- 标准
UPDATE `lab_achievement` SET `category_id` = 8 WHERE `type` = 1 AND `paper_type` = 6; -- 研究报告
UPDATE `lab_achievement` SET `category_id` = 9 WHERE `type` = 1 AND `paper_type` = 7; -- 其他论文

-- 项目类型映射
UPDATE `lab_achievement` SET `category_id` = 10 WHERE `type` = 2 AND `project_type` = 1; -- 国家重点项目
UPDATE `lab_achievement` SET `category_id` = 11 WHERE `type` = 2 AND `project_type` = 2; -- 国家一般项目
UPDATE `lab_achievement` SET `category_id` = 12 WHERE `type` = 2 AND `project_type` = 3; -- 省部级项目
UPDATE `lab_achievement` SET `category_id` = 13 WHERE `type` = 2 AND `project_type` = 4; -- 企业合作项目
UPDATE `lab_achievement` SET `category_id` = 14 WHERE `type` = 2 AND `project_type` = 5; -- 国际合作项目
UPDATE `lab_achievement` SET `category_id` = 15 WHERE `type` = 2 AND `project_type` = 6; -- 青年基金
UPDATE `lab_achievement` SET `category_id` = 16 WHERE `type` = 2 AND `project_type` = 7; -- 博士后基金
UPDATE `lab_achievement` SET `category_id` = 17 WHERE `type` = 2 AND `project_type` = 8; -- 其他项目

-- 添加外键约束（可选，建议在生产环境中谨慎使用）
-- ALTER TABLE `lab_achievement` ADD CONSTRAINT `fk_achievement_category` 
-- FOREIGN KEY (`category_id`) REFERENCES `lab_achievement_category` (`id`);
