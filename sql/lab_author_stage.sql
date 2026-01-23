-- MySQL dump 10.13  Distrib 9.4.0, for macos15.4 (arm64)
--
-- Host: 127.0.0.1    Database: web
-- ------------------------------------------------------
-- Server version	8.4.6

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `lab_author_stage`
--

DROP TABLE IF EXISTS `lab_author_stage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lab_author_stage` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `achievement_id` bigint NOT NULL COMMENT '成果ID',
  `user_id` bigint DEFAULT NULL COMMENT '内部作者user_id；NULL为外部作者',
  `name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '作者姓名（内部作者可冗余）',
  `name_en` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `affiliation` varchar(300) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `author_order` int NOT NULL COMMENT '作者顺序（>0）',
  `is_corresponding` tinyint(1) DEFAULT '0' COMMENT '是否通讯作者',
  `role` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '作者角色/贡献',
  `visible` tinyint(1) DEFAULT '1' COMMENT '仅对内部作者生效：是否在该用户个人页可见',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) DEFAULT '0',
  `creator_id` bigint DEFAULT NULL COMMENT '创建者ID',
  `updater_id` bigint DEFAULT NULL COMMENT '更新者ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_achievement_author_order` (`achievement_id`,`author_order`),
  UNIQUE KEY `uniq_achievement_user` (`achievement_id`,`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_achievement_id` (`achievement_id`),
  KEY `idx_user_visible_order` (`user_id`,`visible`,`author_order`),
  CONSTRAINT `chk_author_order_positive` CHECK ((`author_order` > 0)),
  CONSTRAINT `chk_internal_or_name` CHECK (((`user_id` is not null) or ((`user_id` is null) and (`name` is not null))))
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='成果作者表（内外部作者统一)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lab_author_stage`
--

LOCK TABLES `lab_author_stage` WRITE;
/*!40000 ALTER TABLE `lab_author_stage` DISABLE KEYS */;
INSERT INTO `lab_author_stage` VALUES (1,9,NULL,'佟强','Tong Qiang',NULL,1,1,NULL,1,'2025-09-03 20:06:23','2025-09-03 20:06:23',0,1,NULL),(2,9,NULL,'谢志超','Xie Zhichao',NULL,2,0,NULL,1,'2025-09-03 20:06:23','2025-09-03 20:06:23',0,1,NULL);
/*!40000 ALTER TABLE `lab_author_stage` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-04 14:30:24
