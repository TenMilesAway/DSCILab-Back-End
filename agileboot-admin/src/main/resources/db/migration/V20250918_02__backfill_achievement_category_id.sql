-- Backfill lab_achievement.category_id based on legacy type/paper_type/project_type
-- and ensure an 'Uncategorized' fallback category exists.
-- Idempotent and safe to run multiple times.

-- 1) Ensure fallback category 'UNCATEGORIZED' exists
INSERT INTO lab_achievement_category (parent_id, category_code, category_name, category_name_en, sort_order, is_system, is_active, deleted)
SELECT NULL, 'UNCATEGORIZED', '未分类', 'Uncategorized', 9999, TRUE, TRUE, FALSE
WHERE NOT EXISTS (
  SELECT 1 FROM lab_achievement_category WHERE category_code='UNCATEGORIZED' AND deleted=FALSE
);

-- 2) Map papers (type=1) by paper_type when category_id is NULL
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

-- 3) Map projects (type=2) by project_type when category_id is NULL
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

-- 4) Fill any remaining NULLs with 'UNCATEGORIZED'
UPDATE lab_achievement a
JOIN lab_achievement_category c ON c.category_code='UNCATEGORIZED' AND c.deleted=FALSE
SET a.category_id = c.id
WHERE a.deleted=FALSE AND a.category_id IS NULL;

