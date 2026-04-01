-- Transactional wrapper for manual backfill. Safe to run once.
START TRANSACTION;

-- DSCILab: Backfill lab_achievement.category_id manually based on legacy type fields
-- MySQL 8.0+. Idempotent where possible. Run in a maintenance window.

-- 0) Safety: record current time for backup naming
SET @now := DATE_FORMAT(NOW(), '%Y%m%d_%H%i%S');

-- 1) Backup current category_id values (one-time). Creates a timestamped backup table.
SET @bk := CONCAT('lab_achievement_category_bk_', @now);
SET @sql := CONCAT('CREATE TABLE ', @bk, ' AS SELECT id, category_id AS old_category_id FROM lab_achievement');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) Ensure fallback category 'UNCATEGORIZED' exists
INSERT INTO lab_achievement_category (parent_id, category_code, category_name, category_name_en, sort_order, is_system, is_active, deleted)
SELECT NULL, 'UNCATEGORIZED', '未分类', 'Uncategorized', 9999, TRUE, TRUE, FALSE
WHERE NOT EXISTS (
  SELECT 1 FROM lab_achievement_category WHERE category_code='UNCATEGORIZED' AND deleted=FALSE
);

-- 3) Preview counts BEFORE (do not remove; useful to keep in client history)
SELECT COUNT(*) AS total_achievements FROM lab_achievement WHERE deleted=FALSE;
SELECT COUNT(*) AS null_category_before FROM lab_achievement WHERE deleted=FALSE AND category_id IS NULL;
SELECT type, paper_type, project_type, COUNT(*) AS cnt
FROM lab_achievement WHERE deleted=FALSE GROUP BY type, paper_type, project_type ORDER BY type, paper_type, project_type;

-- 4) Mapping updates (only when category_id IS NULL)
-- Papers (type=1)
UPDATE lab_achievement a
JOIN lab_achievement_category c ON c.category_code='PAPER_JOURNAL' AND c.deleted=FALSE
SET a.category_id = c.id
WHERE a.deleted=FALSE AND a.category_id IS NULL AND a.type=1 AND a.paper_type=1;

UPDATE lab_achievement a
JOIN lab_achievement_category c ON c.category_code='PAPER_CONFERENCE' AND c.deleted=FALSE
SET a.category_id = c.id
WHERE a.deleted=FALSE AND a.category_id IS NULL AND a.type=1 AND a.paper_type=2;

UPDATE lab_achievement a
JOIN lab_achievement_category c ON c.category_code='PAPER_BOOK_CHAPTER' AND c.deleted=FALSE
SET a.category_id = c.id
WHERE a.deleted=FALSE AND a.category_id IS NULL AND a.type=1 AND a.paper_type=3;

UPDATE lab_achievement a
JOIN lab_achievement_category c ON c.category_code='PAPER_PATENT' AND c.deleted=FALSE
SET a.category_id = c.id
WHERE a.deleted=FALSE AND a.category_id IS NULL AND a.type=1 AND a.paper_type=4;

UPDATE lab_achievement a
JOIN lab_achievement_category c ON c.category_code='PAPER_STANDARD' AND c.deleted=FALSE
SET a.category_id = c.id
WHERE a.deleted=FALSE AND a.category_id IS NULL AND a.type=1 AND a.paper_type=5;

UPDATE lab_achievement a
JOIN lab_achievement_category c ON c.category_code='PAPER_REPORT' AND c.deleted=FALSE
SET a.category_id = c.id
WHERE a.deleted=FALSE AND a.category_id IS NULL AND a.type=1 AND a.paper_type=6;

UPDATE lab_achievement a
JOIN lab_achievement_category c ON c.category_code='PAPER_OTHER' AND c.deleted=FALSE
SET a.category_id = c.id
WHERE a.deleted=FALSE AND a.category_id IS NULL AND a.type=1 AND a.paper_type=7;

-- Projects (type=2)
UPDATE lab_achievement a
JOIN lab_achievement_category c ON c.category_code='PROJECT_NATIONAL_KEY' AND c.deleted=FALSE
SET a.category_id = c.id
WHERE a.deleted=FALSE AND a.category_id IS NULL AND a.type=2 AND a.project_type=1;

UPDATE lab_achievement a
JOIN lab_achievement_category c ON c.category_code='PROJECT_NATIONAL_GENERAL' AND c.deleted=FALSE
SET a.category_id = c.id
WHERE a.deleted=FALSE AND a.category_id IS NULL AND a.type=2 AND a.project_type=2;

UPDATE lab_achievement a
JOIN lab_achievement_category c ON c.category_code='PROJECT_PROVINCIAL' AND c.deleted=FALSE
SET a.category_id = c.id
WHERE a.deleted=FALSE AND a.category_id IS NULL AND a.type=2 AND a.project_type=3;

UPDATE lab_achievement a
JOIN lab_achievement_category c ON c.category_code='PROJECT_ENTERPRISE' AND c.deleted=FALSE
SET a.category_id = c.id
WHERE a.deleted=FALSE AND a.category_id IS NULL AND a.type=2 AND a.project_type=4;

UPDATE lab_achievement a
JOIN lab_achievement_category c ON c.category_code='PROJECT_INTERNATIONAL' AND c.deleted=FALSE
SET a.category_id = c.id
WHERE a.deleted=FALSE AND a.category_id IS NULL AND a.type=2 AND a.project_type=5;

UPDATE lab_achievement a
JOIN lab_achievement_category c ON c.category_code='PROJECT_YOUTH_FUND' AND c.deleted=FALSE
SET a.category_id = c.id
WHERE a.deleted=FALSE AND a.category_id IS NULL AND a.type=2 AND a.project_type=6;

UPDATE lab_achievement a
JOIN lab_achievement_category c ON c.category_code='PROJECT_POSTDOC_FUND' AND c.deleted=FALSE
SET a.category_id = c.id
WHERE a.deleted=FALSE AND a.category_id IS NULL AND a.type=2 AND a.project_type=7;

UPDATE lab_achievement a
JOIN lab_achievement_category c ON c.category_code='PROJECT_OTHER' AND c.deleted=FALSE
SET a.category_id = c.id
WHERE a.deleted=FALSE AND a.category_id IS NULL AND a.type=2 AND a.project_type=8;

-- 5) Fill any remaining NULL category_id with 'UNCATEGORIZED'
UPDATE lab_achievement a
JOIN lab_achievement_category c ON c.category_code='UNCATEGORIZED' AND c.deleted=FALSE
SET a.category_id = c.id
WHERE a.deleted=FALSE AND a.category_id IS NULL;

-- 6) Verification AFTER
SELECT COUNT(*) AS null_category_after FROM lab_achievement WHERE deleted=FALSE AND category_id IS NULL;
SELECT a.id, a.title, a.type, a.paper_type, a.project_type, a.category_id, c.category_name
FROM lab_achievement a LEFT JOIN lab_achievement_category c ON a.category_id=c.id
WHERE a.deleted=FALSE ORDER BY a.id DESC LIMIT 50;

COMMIT;
