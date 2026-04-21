package com.agileboot.domain.lab.achievement.db;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

/**
 * 实验室成果 Mapper
 */
@Mapper
public interface LabAchievementMapper extends BaseMapper<LabAchievementEntity> {

	@Insert({
		"INSERT INTO lab_achievement (",
		"  id, title, title_en, description, keywords, type, paper_type, project_type, category_id, venue,",
		"  publish_date, project_start_date, project_end_date, reference, link_url, git_url, homepage_url, pdf_url, doi,",
		"  funding_amount, owner_user_id, published, is_verified, extra, deleted, creator_id, updater_id, create_time, update_time",
		") VALUES (",
		"  #{entity.id}, #{entity.title}, #{entity.titleEn}, #{entity.description}, #{entity.keywords}, #{entity.type}, #{entity.paperType}, #{entity.projectType}, #{entity.categoryId}, #{entity.venue},",
		"  #{entity.publishDate}, #{entity.projectStartDate}, #{entity.projectEndDate}, #{entity.reference}, #{entity.linkUrl}, #{entity.gitUrl}, #{entity.homepageUrl}, #{entity.pdfUrl}, #{entity.doi},",
		"  #{entity.fundingAmount}, #{entity.ownerUserId}, #{entity.published}, #{entity.isVerified}, #{entity.extra}, #{entity.deleted}, #{entity.creatorId}, #{entity.updaterId}, #{entity.createTime}, #{entity.updateTime}",
		") ON DUPLICATE KEY UPDATE",
		"  title = VALUES(title),",
		"  title_en = VALUES(title_en),",
		"  description = VALUES(description),",
		"  keywords = VALUES(keywords),",
		"  type = VALUES(type),",
		"  paper_type = VALUES(paper_type),",
		"  project_type = VALUES(project_type),",
		"  category_id = VALUES(category_id),",
		"  venue = VALUES(venue),",
		"  publish_date = VALUES(publish_date),",
		"  project_start_date = VALUES(project_start_date),",
		"  project_end_date = VALUES(project_end_date),",
		"  reference = VALUES(reference),",
		"  link_url = VALUES(link_url),",
		"  git_url = VALUES(git_url),",
		"  homepage_url = VALUES(homepage_url),",
		"  pdf_url = VALUES(pdf_url),",
		"  doi = VALUES(doi),",
		"  funding_amount = VALUES(funding_amount),",
		"  owner_user_id = VALUES(owner_user_id),",
		"  published = VALUES(published),",
		"  is_verified = VALUES(is_verified),",
		"  extra = VALUES(extra),",
		"  deleted = VALUES(deleted),",
		"  creator_id = VALUES(creator_id),",
		"  updater_id = VALUES(updater_id),",
		"  create_time = VALUES(create_time),",
		"  update_time = VALUES(update_time)"
	})
	@Options(useGeneratedKeys = true, keyProperty = "entity.id", keyColumn = "id")
	int upsertSnapshot(@Param("entity") LabAchievementEntity entity);
}
