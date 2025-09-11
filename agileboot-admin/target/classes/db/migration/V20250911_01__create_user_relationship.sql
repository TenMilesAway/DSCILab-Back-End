-- Flyway migration: create lab_user_relationship table for teacher-student relationships
-- Purpose: manage teacher-student relationships in the lab
-- Notes:
-- 1) This table supports many-to-many relationships between teachers and students
-- 2) A student can have only one active supervisor at a time (enforced by business logic)
-- 3) A teacher can have multiple students
-- 4) Soft delete is used to maintain historical relationships

-- Create lab_user_relationship table
CREATE TABLE IF NOT EXISTS `lab_user_relationship` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
  `teacher_id` BIGINT NOT NULL COMMENT '导师ID',
  `student_id` BIGINT NOT NULL COMMENT '学生ID',
  `relationship_type` TINYINT DEFAULT 1 COMMENT '关系类型：1=导师关系',
  `start_date` DATE COMMENT '关系开始时间',
  `end_date` DATE COMMENT '关系结束时间（毕业等）',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1=活跃,2=已结束',
  `remark` VARCHAR(255) COMMENT '备注',
  
  -- 审计字段
  `creator_id` BIGINT COMMENT '创建者ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater_id` BIGINT COMMENT '更新者ID',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除 0=存在 1=删除',
  
  -- 索引
  UNIQUE KEY `uk_teacher_student_deleted` (`teacher_id`, `student_id`, `deleted`) COMMENT '同一师生关系唯一（考虑软删除）',
  KEY `idx_teacher` (`teacher_id`),
  KEY `idx_student` (`student_id`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`),
  KEY `idx_teacher_status` (`teacher_id`, `status`, `deleted`),
  KEY `idx_student_status` (`student_id`, `status`, `deleted`),
  
  -- 外键约束
  CONSTRAINT `fk_relationship_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `lab_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_relationship_student` FOREIGN KEY (`student_id`) REFERENCES `lab_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='实验室师生关系表';

-- 创建索引以优化常用查询
CREATE INDEX `idx_active_relationships` ON `lab_user_relationship` (`status`, `deleted`, `teacher_id`, `student_id`);
CREATE INDEX `idx_create_time` ON `lab_user_relationship` (`create_time`);
