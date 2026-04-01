-- 在完成 001_alter_lab_achievement_add_fields.sql 并修正存量数据后，执行本脚本添加 CHECK 约束

ALTER TABLE `lab_achievement`
  ADD CONSTRAINT `chk_funding_non_negative`
    CHECK (funding_amount IS NULL OR funding_amount >= 0),

  ADD CONSTRAINT `chk_paper_fields`
    CHECK (
      (type = 1)
      AND (paper_type BETWEEN 1 AND 7)
      AND (project_type IS NULL)
      AND (publish_date IS NOT NULL)
      AND (project_start_date IS NULL)
      AND (project_end_date IS NULL)
    ),

  ADD CONSTRAINT `chk_project_fields`
    CHECK (
      (type = 2)
      AND (project_type BETWEEN 1 AND 8)
      AND (paper_type IS NULL)
      AND (publish_date IS NULL)
      AND (project_start_date IS NOT NULL)
      AND (
        project_end_date IS NULL
        OR project_end_date >= project_start_date
      )
    );

