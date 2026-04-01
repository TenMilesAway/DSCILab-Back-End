CREATE TABLE IF NOT EXISTS `lab_project_paper_rel` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint NOT NULL COMMENT '关联项目ID（lab_achievement_project.id）',
  `paper_id` bigint NOT NULL COMMENT '关联论文ID（lab_achievement_paper.id）',
  `support_role` varchar(100) DEFAULT NULL COMMENT '资助角色/类型',
  `support_amount` decimal(12,2) DEFAULT NULL COMMENT '资助金额（万元）',
  `note` varchar(500) DEFAULT NULL COMMENT '备注',
  `creator_id` bigint DEFAULT NULL,n
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updater_id` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_project_paper` (`project_id`,`paper_id`,`deleted`),
  KEY `idx_lab_project_paper_project` (`project_id`),
  KEY `idx_lab_project_paper_paper` (`paper_id`),
  CONSTRAINT `fk_project_paper_project` FOREIGN KEY (`project_id`) REFERENCES `lab_achievement_project` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_project_paper_paper` FOREIGN KEY (`paper_id`) REFERENCES `lab_achievement_paper` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目与论文关联表';
