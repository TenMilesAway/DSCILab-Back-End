-- 008_add_audit_columns_to_split_tables.sql
-- 为拆分后的成果子表补齐审计字段，保持与 BaseEntity 映射一致

ALTER TABLE `lab_achievement_paper`
    ADD COLUMN `creator_id` BIGINT NULL COMMENT '创建者ID' AFTER `deleted`;

ALTER TABLE `lab_achievement_paper`
    ADD COLUMN `updater_id` BIGINT NULL COMMENT '更新者ID' AFTER `create_time`;

ALTER TABLE `lab_achievement_project`
    ADD COLUMN `creator_id` BIGINT NULL COMMENT '创建者ID' AFTER `deleted`;

ALTER TABLE `lab_achievement_project`
    ADD COLUMN `updater_id` BIGINT NULL COMMENT '更新者ID' AFTER `create_time`;

-- 迁移历史数据：从聚合表补齐创建/更新人
UPDATE `lab_achievement_paper` p
    JOIN `lab_achievement` a ON a.`id` = p.`id`
SET
    p.`creator_id` = a.`creator_id`,
    p.`updater_id` = a.`updater_id`
WHERE
    (p.`creator_id` IS NULL AND a.`creator_id` IS NOT NULL)
    OR (p.`updater_id` IS NULL AND a.`updater_id` IS NOT NULL);

UPDATE `lab_achievement_project` pr
    JOIN `lab_achievement` a ON a.`id` = pr.`id`
SET
    pr.`creator_id` = a.`creator_id`,
    pr.`updater_id` = a.`updater_id`
WHERE
    (pr.`creator_id` IS NULL AND a.`creator_id` IS NOT NULL)
    OR (pr.`updater_id` IS NULL AND a.`updater_id` IS NOT NULL);
