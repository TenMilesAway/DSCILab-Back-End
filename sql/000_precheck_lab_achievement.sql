-- 预检查：在添加约束前，确认存量数据是否满足论文/项目字段要求
-- 若以下查询返回任何行，请先修正数据后再执行 002_add_checks_lab_achievement.sql

-- 论文违规（type=1）
SELECT id, type, paper_type, project_type, publish_date, project_start_date, project_end_date
FROM lab_achievement
WHERE type = 1 AND (
  paper_type IS NULL OR paper_type NOT BETWEEN 1 AND 7
  OR project_type IS NOT NULL
  OR publish_date IS NULL
  OR project_start_date IS NOT NULL
  OR project_end_date IS NOT NULL
);

-- 项目违规（type=2）
SELECT id, type, paper_type, project_type, publish_date, project_start_date, project_end_date
FROM lab_achievement
WHERE type = 2 AND (
  project_type IS NULL OR project_type NOT BETWEEN 1 AND 8
  OR paper_type IS NOT NULL
  OR project_start_date IS NULL
  OR (project_end_date IS NOT NULL AND project_end_date < project_start_date)
  OR publish_date IS NOT NULL
);

-- 经费负数检查
SELECT id, funding_amount
FROM lab_achievement
WHERE funding_amount IS NOT NULL AND funding_amount < 0;

