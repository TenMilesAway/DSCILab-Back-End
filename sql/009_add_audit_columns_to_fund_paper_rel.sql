-- 009_add_audit_columns_to_fund_paper_rel.sql
-- 为基金-论文关联表补充逻辑删除与审计字段

ALTER TABLE `lab_fund_paper_rel`
    ADD COLUMN `deleted` BOOLEAN DEFAULT FALSE COMMENT '是否删除' AFTER `note`;

ALTER TABLE `lab_fund_paper_rel`
    ADD COLUMN `creator_id` BIGINT NULL COMMENT '创建者ID' AFTER `deleted`;

ALTER TABLE `lab_fund_paper_rel`
    ADD COLUMN `updater_id` BIGINT NULL COMMENT '更新者ID' AFTER `create_time`;

-- 历史数据默认视为未删除
UPDATE `lab_fund_paper_rel`
SET `deleted` = FALSE
WHERE `deleted` IS NULL;
