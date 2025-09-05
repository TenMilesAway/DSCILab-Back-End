-- Flyway migration: fix lab_achievement_author unique index to include deleted column
-- Purpose: allow soft-deleted rows to co-exist with new rows without unique conflicts
-- Notes:
-- 1) This script assumes the current unique index name is `uniq_achievement_author_order`.
-- 2) If your index name is different, adjust the DROP INDEX statement accordingly.
-- 3) If you have duplicate active rows (deleted=0) for the same (achievement_id, author_order),
--    this migration will fail. Clean duplicates first before applying.

-- Backup reminder (run separately before migration):
--   mysqldump -h <HOST> -u <USER> -p --databases <DB_NAME> --tables lab_achievement_author > lab_achievement_author_backup.sql

-- Drop old unique index (without deleted column)
DROP INDEX `uniq_achievement_author_order` ON `lab_achievement_author`;

-- Ensure `deleted` column is NOT NULL with default 0
ALTER TABLE `lab_achievement_author`
  MODIFY COLUMN `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=存在 1=删除';

-- Recreate unique index including deleted column
ALTER TABLE `lab_achievement_author`
  ADD UNIQUE KEY `uniq_achievement_author_order` (`achievement_id`, `author_order`, `deleted`);

-- Optional: query performance index
CREATE INDEX `idx_lab_ach_author_ach_deleted`
  ON `lab_achievement_author` (`achievement_id`, `deleted`);

