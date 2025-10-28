-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.0.37 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.5.0.6677
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for csx_pos
CREATE DATABASE IF NOT EXISTS `csx_pos` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `csx_pos`;

-- Dumping structure for table csx_pos.brands
CREATE TABLE IF NOT EXISTS `brands` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name_en` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `name_kh` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT (now()),
  `updated_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table csx_pos.brands: ~16 rows (approximately)
INSERT INTO `brands` (`id`, `name_en`, `name_kh`, `status`, `created_by`, `updated_by`, `created_at`, `updated_at`) VALUES
	(3, 'admin', '123456', '123456', NULL, 1, NULL, NULL),
	(5, 'admin', '123456', '123456', NULL, 1, NULL, NULL),
	(6, 'admin', '123456', '123456', NULL, 1, NULL, NULL),
	(7, 'admin', '123456', '123456', NULL, 1, NULL, NULL),
	(8, 'aasddmin', 'sad', 'asdas', NULL, 1, NULL, '2024-06-11 07:03:34'),
	(10, 'admin', '123456', '123456', 34, NULL, '2024-06-07 07:48:08', NULL),
	(11, 'admin', '123456', '123456', 1, NULL, '2024-06-07 07:54:15', NULL),
	(13, 'admin', '123456', '123456', 1, NULL, '2024-06-07 10:10:01', NULL),
	(14, 'admin', '123456', '123456', 1, NULL, '2024-06-07 10:10:14', NULL),
	(15, 'admin', 'test', '123456', 1, NULL, '2024-06-07 10:13:18', NULL),
	(16, 'ffss333', 'sad', 'asdas', 1, 1, '2024-06-07 10:26:25', '2024-06-10 06:44:48'),
	(17, 'admin', 'test', '123456', 1, NULL, '2024-06-10 07:12:13', NULL),
	(18, 'admin', 'test', '123456', 1, NULL, '2024-06-10 07:31:41', NULL),
	(19, 'admin', 'test', '123456', 1, NULL, '2024-06-10 07:55:43', NULL),
	(20, 'admin', 'test', '123456', 1, NULL, '2024-06-11 06:56:18', NULL),
	(21, 'admin', 'test', '123456', 1, NULL, '2024-07-28 07:38:57', NULL);

-- Dumping structure for table csx_pos.categories
CREATE TABLE IF NOT EXISTS `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint DEFAULT NULL,
  `short_name_en` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `short_name_kh` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `name_en` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `name_kh` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `description_en` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `description_kh` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_by` int DEFAULT NULL,
  `updated_by` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT (now()),
  `updated_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table csx_pos.categories: ~4 rows (approximately)
INSERT INTO `categories` (`id`, `parent_id`, `short_name_en`, `short_name_kh`, `name_en`, `name_kh`, `description_en`, `description_kh`, `status`, `created_by`, `updated_by`, `created_at`, `updated_at`) VALUES
	(2, 3, 'admin2', 'test2', 'name2En', 'te2st', 'test2', 'tes2t', '24', 1, 1, '2024-06-25 04:25:39', '2024-06-25 04:35:31'),
	(4, 3, 'admin', 'test', 'nameEn', 'test', 'test', 'test', '4', 1, NULL, '2024-06-25 04:26:23', NULL);

-- Dumping structure for table csx_pos.permissions
CREATE TABLE IF NOT EXISTS `permissions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table csx_pos.permissions: ~4 rows (approximately)
INSERT INTO `permissions` (`id`, `name`) VALUES
	(1, 'DELETE'),
	(2, 'CREATE'),
	(3, 'READ'),
	(4, 'UPDATE');

-- Dumping structure for table csx_pos.products
CREATE TABLE IF NOT EXISTS `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `brand_id` bigint DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `product_code` bigint DEFAULT NULL,
  `bar_code` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `product_name_en` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `product_name_kh` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `supply_price` decimal(10,2) DEFAULT NULL,
  `quantity_available` int DEFAULT NULL,
  `store_at` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT (now()),
  `updated_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table csx_pos.products: ~26 rows (approximately)
INSERT INTO `products` (`id`, `brand_id`, `category_id`, `product_code`, `bar_code`, `product_name_en`, `product_name_kh`, `type`, `price`, `supply_price`, `quantity_available`, `store_at`, `status`, `created_by`, `updated_by`, `created_at`, `updated_at`) VALUES
	(3, 3, NULL, 233122, '121111111', 'fff', 'ff', '2131', 0.00, 0.00, 1, '12122', 'fffffdsfs', 1, NULL, '2024-06-11 02:36:33', NULL),
	(4, 12, NULL, 123123, '123131', 'dsfsfa', 'fdsfs', '2131', 0.00, 0.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-10 09:43:04', NULL),
	(5, 12, NULL, 123123, '123131', 'dsfsfa', 'fdsfs', '2131', 0.00, 0.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-10 09:43:16', NULL),
	(6, 12, NULL, 123123, '123131', 'dsfsfa', 'fdsfs', '2131', 0.00, 0.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-10 09:43:17', NULL),
	(7, 12, NULL, 123123, '123131', 'dsfsfa', 'fdsfs', '2131', 0.00, 0.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-10 09:43:19', NULL),
	(8, 12, NULL, 123123, '123131', 'dsfsfa', 'fdsfs', '2131', 0.00, 0.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-10 09:43:44', NULL),
	(9, 2, NULL, 233122, '121111111', 'fff', 'ff', '2131', 0.00, 0.00, 1, '12122', 'fffffdsfs', 1, NULL, '2024-06-11 03:00:08', NULL),
	(10, 3, NULL, 233122, '121111111', 'fff', 'ff', '2131', 0.00, 0.00, 1, '12122', 'fffffdsfs', 1, NULL, '2024-06-11 03:00:12', NULL),
	(11, 3, NULL, 123123, '123131', 'dsfsfa', 'fdsfs', '2131', 0.00, 0.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-11 08:34:46', NULL),
	(12, 3, NULL, 123123, '123131', 'dsfsfa', 'fdsfs', '2131', 0.00, 0.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-11 08:35:42', NULL),
	(13, 3, NULL, 123123, '123131', 'dsfsfa', 'fdsfs', '2131', 0.00, 0.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-11 08:35:44', NULL),
	(14, 3, NULL, 123123, '123131', 'dsfsfa', 'fdsfs', '2131', 0.00, 0.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-11 08:46:14', NULL),
	(15, 3, NULL, 123123, '123131', 'dsfsfa', 'fdsfs', '2131', 0.00, 0.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-11 08:46:17', NULL),
	(16, 3, NULL, 123123, '123131', 'dsfsfa', 'fdsfs', '2131', 0.00, 0.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-11 08:46:38', NULL),
	(17, 3, NULL, 123123, '123131', 'dsfsfa', 'fdsfs', '2131', 0.00, 0.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-11 08:47:33', NULL),
	(18, 3, NULL, 123123, '123131', 'dsfsfa', 'fdsfs', '2131', 0.00, 0.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-11 08:53:43', NULL),
	(19, 3, NULL, 123123, '123131', 'dsfsfa', 'fdsfs', '2131', 0.00, 0.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-11 08:54:01', NULL),
	(20, 3, NULL, 123123, '123131', 'dsfsfa', 'fdsfs', '2131', 0.00, 0.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-11 08:54:01', NULL),
	(21, 3, NULL, 123123, '123131', 'dsfsfa', 'fdsfs', '2131', 1212.00, 12121.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-11 09:00:17', NULL),
	(22, 3, NULL, 123123, '123131', 'dsfsfa', 'fdsfs', '2131', 1212.00, 12121.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-11 09:02:07', NULL),
	(23, 3, NULL, 123123, '123131', 'dsfsfa', 'fdsfs', '2131', 1212.00, 12121.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-11 09:02:54', NULL),
	(24, 3, NULL, 123123, '123131', 'dsfsfa', 'fdsfs', '2131', 1212.00, 12121.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-11 09:04:38', NULL),
	(25, 3, NULL, 1, '212', 'dsfsfa', 'fdsfs', '2131', 1212.00, 12121.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-11 09:06:24', NULL),
	(26, 3, NULL, 1, '212', 'dsfsfa', 'fdsfs', '2131', 1212.00, 12121.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-11 09:10:03', NULL),
	(27, 3, NULL, 1, 'sfa', 'dsfsfa', 'fdsfs', '2131', 1212.00, 12121.00, 1, '12122', 'fdsfs', 1, NULL, '2024-06-11 09:10:09', NULL),
	(28, 3, NULL, 1, 'sfa', 'dsfsfa', 'fdsfs', '2131', 1212.00, 12121.00, 0, '12122', 'fdsfs', 1, NULL, '2024-06-11 09:30:41', NULL),
	(29, 3, 4, 1, 'sfa', 'dsfsfa', 'fdsfs', '2131', 1212.00, 12121.00, 0, '12122', 'fdsfs', 1, NULL, '2024-06-25 07:04:44', NULL);

-- Dumping structure for table csx_pos.roles
CREATE TABLE IF NOT EXISTS `roles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table csx_pos.roles: ~0 rows (approximately)
INSERT INTO `roles` (`id`, `name`) VALUES
	(1, 'ADMIN');

-- Dumping structure for table csx_pos.roles_permissions
CREATE TABLE IF NOT EXISTS `roles_permissions` (
  `permission_id` int NOT NULL,
  `role_id` int NOT NULL,
  PRIMARY KEY (`permission_id`,`role_id`),
  KEY `FKqi9odri6c1o81vjox54eedwyh` (`role_id`),
  CONSTRAINT `FKbx9r9uw77p58gsq4mus0mec0o` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`),
  CONSTRAINT `FKqi9odri6c1o81vjox54eedwyh` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table csx_pos.roles_permissions: ~4 rows (approximately)
INSERT INTO `roles_permissions` (`permission_id`, `role_id`) VALUES
	(1, 1),
	(2, 1),
	(3, 1),
	(4, 1);

-- Dumping structure for table csx_pos.users
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `user_type` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT (now()),
  `updated_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table csx_pos.users: ~2 rows (approximately)
INSERT INTO `users` (`id`, `password`, `username`, `email`, `phone`, `user_type`, `status`, `created_at`, `updated_at`) VALUES
	(1, '$2a$10$6Y72IChiSHnmhVPTkHYL2.9d6tNEoNaz4QQF.IvcfyQsq8dHnV4iq', 'admin', NULL, NULL, NULL, NULL, '2024-06-06 06:42:10', NULL),
	(2, '$2a$10$60h.QjY/lIMpQefy7f464eFMOMB.6m9b1.Olint05EeRTFWGMsNM.', 'david', NULL, NULL, NULL, NULL, '2024-06-06 06:42:10', NULL),
	(4, '$2a$10$sBHxhKmGRS5umJKW8K9cl.1KdP/xcfeEWryY2zQPe4wZCymNpgY9G', 'david2', NULL, NULL, NULL, NULL, NULL, NULL);

-- Dumping structure for table csx_pos.users_roles
CREATE TABLE IF NOT EXISTS `users_roles` (
  `role_id` int NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`role_id`,`user_id`),
  KEY `FK2o0jvgh89lemvvo17cbqvdxaa` (`user_id`),
  CONSTRAINT `FK2o0jvgh89lemvvo17cbqvdxaa` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKj6m8fwv7oqv74fcehir1a9ffy` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table csx_pos.users_roles: ~0 rows (approximately)
INSERT INTO `users_roles` (`role_id`, `user_id`) VALUES
	(1, 1);

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
