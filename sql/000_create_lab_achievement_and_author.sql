-- Create base tables for achievements and authors (idempotent)
-- MySQL 8.0+ (CHECK constraints supported). Adjust engine/charset as needed.

CREATE TABLE IF NOT EXISTS `lab_achievement` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,

  -- Basic
  `title` VARCHAR(500) NOT NULL COMMENT '成果标题',
  `title_en` VARCHAR(500) NULL COMMENT '英文标题',
  `description` TEXT NULL COMMENT '摘要/描述',
  `keywords` VARCHAR(1000) NULL COMMENT '关键词',

  -- Classification
  `type` TINYINT NOT NULL COMMENT '成果类型：1=论文,2=项目',
  `paper_type` TINYINT NULL COMMENT '论文类型：1-7（仅当type=1时有效）',
  `project_type` TINYINT NULL COMMENT '项目类型：1-8（仅当type=2时有效）',

  -- Venue & timing
  `venue` VARCHAR(300) NULL COMMENT '期刊/会议/发布渠道',
  `publish_date` DATE NULL COMMENT '发表/公开日期（论文用）',
  `project_start_date` DATE NULL COMMENT '项目开始日期（项目用）',
  `project_end_date` DATE NULL COMMENT '项目结束日期（项目用，可空=进行中）',

  -- Links & resources
  `cover_url` VARCHAR(500) NULL COMMENT '封面图URL',
  `link_url` VARCHAR(500) NULL COMMENT '外部链接URL',
  `git_url` VARCHAR(500) NULL COMMENT 'Git 仓库地址',
  `homepage_url` VARCHAR(500) NULL COMMENT '主页/展示页地址',
  `pdf_url` VARCHAR(500) NULL COMMENT 'PDF 下载地址',

  -- Paper/Project specific
  `doi` VARCHAR(128) NULL COMMENT '论文 DOI',
  `funding_amount` DECIMAL(12,2) NULL COMMENT '项目经费（万元）',

  -- Ownership & publish
  `owner_user_id` BIGINT NOT NULL COMMENT '成果所有者（创建者）',
  `published` BOOLEAN DEFAULT FALSE COMMENT '是否对外发布',
  `is_verified` BOOLEAN DEFAULT FALSE COMMENT '是否已审核',

  -- Ext
  `extra` JSON NULL COMMENT '扩展信息（保留字段）',

  -- Audit
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` BOOLEAN DEFAULT FALSE,

  -- Indexes
  INDEX `idx_type_publish_date` (`type`, `publish_date`),
  INDEX `idx_type_project_dates` (`type`, `project_start_date`, `project_end_date`),
  INDEX `idx_published_publish_date` (`published`, `publish_date`),
  INDEX `idx_owner_user_id` (`owner_user_id`),
  INDEX `idx_doi` (`doi`),
  INDEX `idx_deleted` (`deleted`),

  CONSTRAINT `fk_ach_owner_user` FOREIGN KEY (`owner_user_id`) REFERENCES `lab_user` (`id`),

  -- Checks
  CONSTRAINT `chk_funding_non_negative` CHECK (`funding_amount` IS NULL OR `funding_amount` >= 0),
  CONSTRAINT `chk_type_fields_combo` CHECK (
    (
      `type` = 1 AND `paper_type` BETWEEN 1 AND 7 AND `project_type` IS NULL
      AND `publish_date` IS NOT NULL
      AND `project_start_date` IS NULL AND `project_end_date` IS NULL
    )
    OR
    (
      `type` = 2 AND `project_type` BETWEEN 1 AND 8 AND `paper_type` IS NULL
      AND `publish_date` IS NULL
      AND `project_start_date` IS NOT NULL
      AND (`project_end_date` IS NULL OR `project_end_date` >= `project_start_date`)
    )
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='实验室成果表';


CREATE TABLE IF NOT EXISTS `lab_achievement_author` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `achievement_id` BIGINT NOT NULL COMMENT '成果ID',
  `user_id` BIGINT NULL COMMENT '内部作者user_id；NULL为外部作者',

  `name` VARCHAR(100) NOT NULL COMMENT '作者姓名（内部作者可冗余）',
  `name_en` VARCHAR(100) NULL,
  `affiliation` VARCHAR(300) NULL,

  `author_order` INT NOT NULL COMMENT '作者顺序（>0）',
  `is_corresponding` BOOLEAN DEFAULT FALSE COMMENT '是否通讯作者',
  `role` VARCHAR(100) NULL COMMENT '作者角色/贡献',
  `visible` BOOLEAN DEFAULT TRUE COMMENT '仅对内部作者生效：是否在该用户个人页可见',

  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` BOOLEAN DEFAULT FALSE,

  UNIQUE KEY `uniq_achievement_author_order` (`achievement_id`,`author_order`),
  UNIQUE KEY `uniq_achievement_user` (`achievement_id`,`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_achievement_id` (`achievement_id`),
  KEY `idx_user_visible_order` (`user_id`,`visible`,`author_order`),

  CONSTRAINT `fk_author_achievement` FOREIGN KEY (`achievement_id`) REFERENCES `lab_achievement` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_author_user` FOREIGN KEY (`user_id`) REFERENCES `lab_user` (`id`) ON DELETE CASCADE,

  CONSTRAINT `chk_author_order_positive` CHECK (`author_order` > 0),
  CONSTRAINT `chk_internal_or_name` CHECK ((`user_id` IS NOT NULL) OR (`user_id` IS NULL AND `name` IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='成果作者表（内外部作者统一)';

