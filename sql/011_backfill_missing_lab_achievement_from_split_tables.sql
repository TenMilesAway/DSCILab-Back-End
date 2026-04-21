-- --------------------------------------------------------------------
-- 011_backfill_missing_lab_achievement_from_split_tables.sql
-- 目标：回填“拆分表存在但主成果表缺失”的数据，修复可见性等依赖主表的接口。
-- 使用方式：mysql -u${USER} -p${PWD} web < sql/011_backfill_missing_lab_achievement_from_split_tables.sql
-- --------------------------------------------------------------------

SET NAMES utf8mb4;
START TRANSACTION;

-- 1) 从论文明细回填主成果表
INSERT INTO lab_achievement (
  id, title, title_en, description, keywords,
  type, paper_type, project_type, category_id,
  venue, publish_date, project_start_date, project_end_date,
  reference, link_url, git_url, homepage_url, pdf_url,
  doi, funding_amount, owner_user_id,
  published, is_verified, extra, deleted,
  creator_id, create_time, updater_id, update_time
)
SELECT
  p.id,
  p.title,
  p.title_en,
  p.description,
  p.keywords,
  1 AS type,
  p.paper_type_id AS paper_type,
  NULL AS project_type,
  p.category_id,
  p.publication AS venue,
  p.publish_date,
  NULL AS project_start_date,
  NULL AS project_end_date,
  p.reference,
  p.link_url,
  p.git_url,
  p.homepage_url,
  p.pdf_url,
  p.doi,
  NULL AS funding_amount,
  p.owner_user_id,
  p.published,
  p.is_verified,
  CAST(p.extra AS CHAR),
  p.deleted,
  p.creator_id,
  p.create_time,
  p.updater_id,
  p.update_time
FROM lab_achievement_paper p
WHERE NOT EXISTS (
  SELECT 1 FROM lab_achievement a WHERE a.id = p.id
);

-- 2) 从项目明细回填主成果表
INSERT INTO lab_achievement (
  id, title, title_en, description, keywords,
  type, paper_type, project_type, category_id,
  venue, publish_date, project_start_date, project_end_date,
  reference, link_url, git_url, homepage_url, pdf_url,
  doi, funding_amount, owner_user_id,
  published, is_verified, extra, deleted,
  creator_id, create_time, updater_id, update_time
)
SELECT
  pr.id,
  pr.title,
  pr.title_en,
  pr.description,
  pr.keywords,
  2 AS type,
  NULL AS paper_type,
  pr.project_type_id AS project_type,
  pr.category_id,
  NULL AS venue,
  NULL AS publish_date,
  pr.project_start_date,
  pr.project_end_date,
  pr.reference,
  pr.link_url,
  pr.git_url,
  pr.homepage_url,
  pr.pdf_url,
  NULL AS doi,
  pr.funding_amount,
  pr.owner_user_id,
  pr.published,
  pr.is_verified,
  CAST(pr.extra AS CHAR),
  pr.deleted,
  pr.creator_id,
  pr.create_time,
  pr.updater_id,
  pr.update_time
FROM lab_achievement_project pr
WHERE NOT EXISTS (
  SELECT 1 FROM lab_achievement a WHERE a.id = pr.id
);

COMMIT;
