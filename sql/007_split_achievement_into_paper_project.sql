-- ------------------------------------------------------------------
-- 007_split_achievement_into_paper_project.sql
-- 拆分成果表，新增论文表与项目表，并建立基金-论文关联表
-- 保留原 lab_achievement 表作为备份视图，新增数据需保持三表同步
-- ------------------------------------------------------------------

-- 论文表：与 lab_achievement 一对一，承载论文类成果的业务字段
DROP TABLE IF EXISTS `lab_achievement_paper`;
CREATE TABLE `lab_achievement_paper` (
  `id` BIGINT NOT NULL COMMENT '成果ID，对应 lab_achievement.id',

  -- 基础信息
  `title` VARCHAR(500) NOT NULL COMMENT '论文标题',
  `title_en` VARCHAR(500) NULL COMMENT '英文标题',
  `description` TEXT NULL COMMENT '摘要/描述',
  `keywords` VARCHAR(1000) NULL COMMENT '关键词',

  -- 分类信息
  `category_id` BIGINT NULL COMMENT '成果类型ID（新分类系统）',
  `paper_type` TINYINT NULL COMMENT '论文类型：1-7',

  -- 出版信息
  `venue` VARCHAR(300) NULL COMMENT '期刊/会议/发布渠道',
  `publish_date` DATE NULL COMMENT '发表/公开日期',
  `reference` TEXT NULL COMMENT '参考文献/引用信息',
  `doi` VARCHAR(128) NULL COMMENT '论文 DOI',

  -- 链接信息
  `link_url` VARCHAR(500) NULL COMMENT '外部链接URL',
  `git_url` VARCHAR(500) NULL COMMENT 'Git 仓库地址',
  `homepage_url` VARCHAR(500) NULL COMMENT '主页/展示页地址',
  `pdf_url` VARCHAR(500) NULL COMMENT 'PDF 下载地址',

  -- 归属状态
  `owner_user_id` BIGINT NOT NULL COMMENT '成果所有者（创建者）',
  `published` BOOLEAN DEFAULT FALSE COMMENT '是否对外发布',
  `is_verified` BOOLEAN DEFAULT FALSE COMMENT '是否已审核',
  `extra` JSON NULL COMMENT '扩展信息（JSON）',
  `deleted` BOOLEAN DEFAULT FALSE COMMENT '是否删除',

  -- 审计字段
  `creator_id` BIGINT NULL COMMENT '创建者ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updater_id` BIGINT NULL COMMENT '更新者ID',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`),
  CONSTRAINT `fk_lab_ach_paper_achievement` FOREIGN KEY (`id`)
    REFERENCES `lab_achievement` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_lab_ach_paper_category` FOREIGN KEY (`category_id`)
    REFERENCES `lab_achievement_category` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_lab_ach_paper_owner` FOREIGN KEY (`owner_user_id`)
    REFERENCES `lab_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  INDEX `idx_lab_ach_paper_publish` (`published`, `publish_date`),
  INDEX `idx_lab_ach_paper_owner` (`owner_user_id`),
  INDEX `idx_lab_ach_paper_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='实验室论文表';


DROP TABLE IF EXISTS `lab_achievement_project`;
CREATE TABLE `lab_achievement_project` (
  `id` BIGINT NOT NULL COMMENT '成果ID，对应 lab_achievement.id',

  -- 基础信息
  `title` VARCHAR(500) NOT NULL COMMENT '项目名称',
  `title_en` VARCHAR(500) NULL COMMENT '英文名称',
  `description` TEXT NULL COMMENT '摘要/描述',
  `keywords` VARCHAR(1000) NULL COMMENT '关键词',

  -- 分类信息
  `category_id` BIGINT NULL COMMENT '成果类型ID（新分类系统）',
  `project_type` TINYINT NULL COMMENT '项目类型：1-8',

  -- 项目信息
  `project_start_date` DATE NULL COMMENT '项目开始日期',
  `project_end_date` DATE NULL COMMENT '项目结束日期（可空=进行中）',
  `funding_amount` DECIMAL(12,2) NULL COMMENT '项目经费（万元）',
  `reference` TEXT NULL COMMENT '项目参考/简介',

  -- 链接信息
  `link_url` VARCHAR(500) NULL COMMENT '外部链接URL',
  `git_url` VARCHAR(500) NULL COMMENT 'Git 仓库地址',
  `homepage_url` VARCHAR(500) NULL COMMENT '主页/展示页地址',
  `pdf_url` VARCHAR(500) NULL COMMENT '项目文档/PDF',

  -- 归属状态
  `owner_user_id` BIGINT NOT NULL COMMENT '成果所有者（创建者）',
  `published` BOOLEAN DEFAULT FALSE COMMENT '是否对外发布',
  `is_verified` BOOLEAN DEFAULT FALSE COMMENT '是否已审核',
  `extra` JSON NULL COMMENT '扩展信息（JSON）',
  `deleted` BOOLEAN DEFAULT FALSE COMMENT '是否删除',

  -- 审计字段
  `creator_id` BIGINT NULL COMMENT '创建者ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updater_id` BIGINT NULL COMMENT '更新者ID',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`),
  CONSTRAINT `fk_lab_achievement_project_achievement` FOREIGN KEY (`id`)
    REFERENCES `lab_achievement` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_lab_achievement_project_category` FOREIGN KEY (`category_id`)
    REFERENCES `lab_achievement_category` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_lab_achievement_project_owner` FOREIGN KEY (`owner_user_id`)
    REFERENCES `lab_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  INDEX `idx_lab_achievement_project_start` (`project_start_date`),
  INDEX `idx_lab_achievement_project_owner` (`owner_user_id`),
  INDEX `idx_lab_achievement_project_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='实验室项目表';


-- 基金与论文关联表（多对多）
DROP TABLE IF EXISTS `lab_fund_paper_rel`;
CREATE TABLE `lab_fund_paper_rel` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `fund_id` INT UNSIGNED NOT NULL COMMENT '基金ID',
  `paper_id` BIGINT NOT NULL COMMENT '论文成果ID',
  `support_role` VARCHAR(100) NULL COMMENT '资助角色/类型',
  `amount` DECIMAL(12,2) NULL COMMENT '资助金额（万元）',
  `note` VARCHAR(500) NULL COMMENT '备注',
  `deleted` BOOLEAN DEFAULT FALSE COMMENT '是否删除',
  `creator_id` BIGINT NULL COMMENT '创建者ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updater_id` BIGINT NULL COMMENT '更新者ID',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  UNIQUE KEY `uniq_fund_paper` (`fund_id`, `paper_id`),
  CONSTRAINT `fk_fund_paper_rel_fund` FOREIGN KEY (`fund_id`)
    REFERENCES `lab_fund` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_fund_paper_rel_paper` FOREIGN KEY (`paper_id`)
    REFERENCES `lab_achievement_paper` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='基金与论文关联表';


-- 初始化数据：将现有成果按类型拆分同步到新表
INSERT INTO `lab_achievement_paper` (
  `id`, `title`, `title_en`, `description`, `keywords`,
  `category_id`, `paper_type`, `venue`, `publish_date`, `reference`,
  `doi`, `link_url`, `git_url`, `homepage_url`, `pdf_url`,
  `owner_user_id`, `published`, `is_verified`, `extra`, `deleted`,
  `creator_id`, `create_time`, `updater_id`, `update_time`
)
SELECT
  `id`, `title`, `title_en`, `description`, `keywords`,
  `category_id`, `paper_type`, `venue`, `publish_date`, `reference`,
  `doi`, `link_url`, `git_url`, `homepage_url`, `pdf_url`,
  `owner_user_id`, `published`, `is_verified`, `extra`, `deleted`,
  `creator_id`, `create_time`, `updater_id`, `update_time`
FROM `lab_achievement`
WHERE `type` = 1
  AND NOT EXISTS (SELECT 1 FROM `lab_achievement_paper` p WHERE p.`id` = `lab_achievement`.`id`);

INSERT INTO `lab_achievement_project` (
  `id`, `title`, `title_en`, `description`, `keywords`,
  `category_id`, `project_type`, `project_start_date`, `project_end_date`,
  `funding_amount`, `reference`,
  `link_url`, `git_url`, `homepage_url`, `pdf_url`,
  `owner_user_id`, `published`, `is_verified`, `extra`, `deleted`,
  `creator_id`, `create_time`, `updater_id`, `update_time`
)
SELECT
  `id`, `title`, `title_en`, `description`, `keywords`,
  `category_id`, `project_type`, `project_start_date`, `project_end_date`,
  `funding_amount`, `reference`,
  `link_url`, `git_url`, `homepage_url`, `pdf_url`,
  `owner_user_id`, `published`, `is_verified`, `extra`, `deleted`,
  `creator_id`, `create_time`, `updater_id`, `update_time`
FROM `lab_achievement`
WHERE `type` = 2
  AND NOT EXISTS (SELECT 1 FROM `lab_achievement_project` pr WHERE pr.`id` = `lab_achievement`.`id`);
