ALTER TABLE `lab_project_author`
    DROP INDEX `uniq_project_author_user`,
    ADD UNIQUE KEY `uniq_project_author_user` (`project_id`, `user_id`, `deleted`);
