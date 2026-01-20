CREATE DATABASE IF NOT EXISTS `second_trade`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE `second_trade`;

SET NAMES utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` VARCHAR(64) NULL COMMENT '学号/工号（用于登录）',
  `username` VARCHAR(64) NULL COMMENT '用户名（用于登录）',
  `password_hash` VARCHAR(255) NULL COMMENT '密码哈希（BCrypt）',
  `role` VARCHAR(16) NULL COMMENT '角色：user/admin',
  `nickname` VARCHAR(64) NULL COMMENT '昵称',
  `email` VARCHAR(128) NULL COMMENT '邮箱',
  `phone` VARCHAR(32) NULL COMMENT '手机号',
  `school` VARCHAR(128) NULL COMMENT '学校',
  `avatar` VARCHAR(255) NULL COMMENT '头像URL',
  `address` VARCHAR(255) NULL COMMENT '地址',
  `credit_score` INT NULL COMMENT '信用分',
  `is_disabled` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否禁用：0否1是',
  `is_superuser` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否超级管理员：0否1是',
  `is_staff` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否后台人员：0否1是',
  `created_at` DATETIME NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_student_id` (`student_id`),
  UNIQUE KEY `uk_users_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `auth_tokens` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NULL COMMENT '用户ID',
  `refresh_token` VARCHAR(512) NULL COMMENT 'Refresh Token（JWT）',
  `revoked` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否吊销：0否1是',
  `created_at` DATETIME NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_auth_tokens_refresh` (`refresh_token`),
  KEY `idx_auth_tokens_user` (`user_id`),
  CONSTRAINT `fk_auth_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='刷新令牌表';

CREATE TABLE IF NOT EXISTS `market_categories` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(64) NULL COMMENT '类目名称',
  `code` VARCHAR(64) NULL COMMENT '类目编码（可选）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品类目表';

CREATE TABLE IF NOT EXISTS `market_brands` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(255) NULL COMMENT '品牌名称',
  `category_id` BIGINT NULL COMMENT '所属类目ID',
  PRIMARY KEY (`id`),
  KEY `idx_brands_category` (`category_id`),
  CONSTRAINT `fk_brands_category` FOREIGN KEY (`category_id`) REFERENCES `market_categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌表';

CREATE TABLE IF NOT EXISTS `market_device_models` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(128) NULL COMMENT '型号名称',
  `brand_id` BIGINT NULL COMMENT '品牌ID',
  `category_id` BIGINT NULL COMMENT '类目ID',
  `image_url` VARCHAR(255) NULL COMMENT '型号参考图URL',
  `msrp_price` VARCHAR(64) NULL COMMENT '官方参考价（字符串，兼容不同币种/格式）',
  `index_type` VARCHAR(64) NULL COMMENT '索引类型（可选）',
  PRIMARY KEY (`id`),
  KEY `idx_device_models_brand` (`brand_id`),
  KEY `idx_device_models_category` (`category_id`),
  CONSTRAINT `fk_device_models_brand` FOREIGN KEY (`brand_id`) REFERENCES `market_brands` (`id`),
  CONSTRAINT `fk_device_models_category` FOREIGN KEY (`category_id`) REFERENCES `market_categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备型号表';

CREATE TABLE IF NOT EXISTS `market_drafts` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `draft_key` VARCHAR(64) NULL COMMENT '草稿唯一Key',
  `seller_id` BIGINT NULL COMMENT '卖家用户ID',
  `category_id` BIGINT NULL COMMENT '类目ID',
  `device_model_id` BIGINT NULL COMMENT '型号ID',
  `years_used` INT NULL COMMENT '使用年限（整数，前端可能传小数时需后端处理）',
  `original_price` INT NULL COMMENT '购买原价',
  `created_at` DATETIME NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_market_drafts_key` (`draft_key`),
  KEY `idx_market_drafts_seller` (`seller_id`),
  CONSTRAINT `fk_market_drafts_seller` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上架草稿表';

CREATE TABLE IF NOT EXISTS `market_draft_images` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `draft_id` BIGINT NULL COMMENT '草稿ID',
  `url` VARCHAR(255) NULL COMMENT '图片URL（建议 /media/...）',
  `sort_order` INT NULL COMMENT '排序（从0开始）',
  `main_image` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否主图：0否1是',
  PRIMARY KEY (`id`),
  KEY `idx_draft_images_draft` (`draft_id`),
  CONSTRAINT `fk_draft_images_draft` FOREIGN KEY (`draft_id`) REFERENCES `market_drafts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='草稿图片表';

CREATE TABLE IF NOT EXISTS `market_products` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `seller_id` BIGINT NULL COMMENT '卖家用户ID',
  `category_id` BIGINT NULL COMMENT '类目ID',
  `device_model_id` BIGINT NULL COMMENT '型号ID',
  `title` VARCHAR(255) NULL COMMENT '商品标题',
  `school` VARCHAR(128) NULL COMMENT '学校（可选）',
  `product_summary` VARCHAR(1000) NULL COMMENT '商品摘要（手动填写）',
  `description` TEXT NULL COMMENT '商品描述',
  `selling_price` INT NULL COMMENT '售价',
  `original_price` INT NULL COMMENT '购买原价',
  `years_used` INT NULL COMMENT '使用年限（整数）',
  `grade_label` VARCHAR(32) NULL COMMENT '成色标签（A/B/C等）',
  `defects` TEXT NULL COMMENT '瑕疵列表JSON（示例：[\"scratch_screen\"]）',
  `main_image` VARCHAR(255) NULL COMMENT '主图URL',
  `status` VARCHAR(32) NULL COMMENT '状态：pending_review/on_sale/rejected',
  `review_reject_reason` VARCHAR(255) NULL COMMENT '审核驳回原因',
  `favorite_count` INT NULL COMMENT '收藏数',
  `view_count` INT NULL COMMENT '浏览数',
  `market_tag` VARCHAR(64) NULL COMMENT '市场标签（New/Hot/Recommend等）',
  `created_at` DATETIME NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_products_status` (`status`),
  KEY `idx_products_seller` (`seller_id`),
  KEY `idx_products_category` (`category_id`),
  KEY `idx_products_device_model` (`device_model_id`),
  CONSTRAINT `fk_products_seller` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_products_category` FOREIGN KEY (`category_id`) REFERENCES `market_categories` (`id`),
  CONSTRAINT `fk_products_device_model` FOREIGN KEY (`device_model_id`) REFERENCES `market_device_models` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE IF NOT EXISTS `market_product_images` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT NULL COMMENT '商品ID',
  `url` VARCHAR(255) NULL COMMENT '图片URL',
  `sort_order` INT NULL COMMENT '排序（从0开始）',
  `main_image` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否主图：0否1是',
  PRIMARY KEY (`id`),
  KEY `idx_product_images_product` (`product_id`),
  CONSTRAINT `fk_product_images_product` FOREIGN KEY (`product_id`) REFERENCES `market_products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

CREATE TABLE IF NOT EXISTS `market_orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NULL COMMENT '订单号',
  `status` VARCHAR(32) NULL COMMENT '订单状态：pending_payment/pending_shipment/shipped/completed/refunded',
  `buyer_id` BIGINT NULL COMMENT '买家用户ID',
  `seller_id` BIGINT NULL COMMENT '卖家用户ID',
  `product_id` BIGINT NULL COMMENT '商品ID',
  `pickup` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否自提：0否1是',
  `amount` INT NULL COMMENT '订单金额（通常等于商品售价）',
  `created_at` DATETIME NULL COMMENT '创建时间',
  `paid_at` DATETIME NULL COMMENT '支付时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_market_orders_order_no` (`order_no`),
  KEY `idx_orders_buyer` (`buyer_id`),
  KEY `idx_orders_seller` (`seller_id`),
  KEY `idx_orders_product` (`product_id`),
  CONSTRAINT `fk_orders_buyer` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_orders_seller` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_orders_product` FOREIGN KEY (`product_id`) REFERENCES `market_products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE IF NOT EXISTS `market_product_comments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT NULL COMMENT '商品ID',
  `sender_id` BIGINT NULL COMMENT '发送者用户ID',
  `sender_role` VARCHAR(16) NULL COMMENT '发送者角色：buyer/seller',
  `content` TEXT NULL COMMENT '留言内容',
  `created_at` DATETIME NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_comments_product` (`product_id`),
  KEY `idx_comments_sender` (`sender_id`),
  CONSTRAINT `fk_comments_product` FOREIGN KEY (`product_id`) REFERENCES `market_products` (`id`),
  CONSTRAINT `fk_comments_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品留言表';

-- -------------------------
-- 默认数据（可重复执行）mockuser
-- -------------------------
-- 管理员： admin / admin123
-- 普通用户： 20240001 / 123456

INSERT IGNORE INTO `users`
(`id`, `student_id`, `username`, `password_hash`, `role`, `nickname`, `email`, `phone`, `school`, `avatar`, `address`, `credit_score`, `is_disabled`, `is_superuser`, `is_staff`, `created_at`)
VALUES
(1, 'admin', 'admin', '$2y$10$LNHr2KhaXo5va8QqZBw9Oeho/hBqg/7Tur3N452WtalTlDcwk3CVi', 'admin', '管理员', 'admin@example.com', '13900000000', '示例大学', NULL, 'Admin Address', 100, b'0', b'1', b'1', NOW()),
(2, '20240001', '20240001', '$2y$10$dpmFHnOiwc.V0cXOjVYzlu1Lh6KgQtYAR1G9qDuNJA12fQzkqrYXy', 'user', 'Mock User', 'mock@example.com', '13800138000', '示例大学', NULL, 'Mock Address', 90, b'0', b'0', b'0', NOW()),
(3, '20240002', '20240002', '$2y$10$dpmFHnOiwc.V0cXOjVYzlu1Lh6KgQtYAR1G9qDuNJA12fQzkqrYXy', 'user', '买家A', 'buyera@example.com', '13800138001', '示例大学', NULL, 'Buyer Address', 88, b'0', b'0', b'0', NOW()),
(4, '20240003', '20240003', '$2y$10$dpmFHnOiwc.V0cXOjVYzlu1Lh6KgQtYAR1G9qDuNJA12fQzkqrYXy', 'user', '卖家99', 'seller99@example.com', '13800138002', '示例大学', NULL, 'Seller Address', 92, b'0', b'0', b'0', NOW());

INSERT IGNORE INTO `market_categories` (`id`, `name`, `code`) VALUES
(1, 'Smartphones', 'smartphones'),
(2, 'Laptops', 'laptops'),
(3, 'Tablets', 'tablets'),
(4, 'Smartwatches', 'smartwatches');

INSERT IGNORE INTO `market_brands` (`id`, `name`, `category_id`) VALUES
(1, 'Apple', 1),
(2, 'Samsung', 1),
(3, 'Xiaomi', 1),
(4, 'Dell', 2),
(5, 'Lenovo', 2),
(6, 'Apple ipad', 3),
(7, 'Hua Wei ipad', 3),
(8, 'Galaxy', 4);

INSERT IGNORE INTO `market_device_models` (`id`, `name`, `brand_id`, `category_id`, `image_url`, `msrp_price`, `index_type`) VALUES
(101, 'iPhone 13', 1, 1, 'https://p1.ssl.qhimgs1.com/sdr/400__/t048bfd78006729ebdb.png', '5999', 'phone_standard'),
(102, 'iPhone 12', 1, 1, 'https://p1.ssl.qhimgs1.com/sdr/400__/t01c65b8132622260cb.jpg', '4999', 'phone_standard'),
(103, 'Galaxy S21', 2, 1, 'https://p1.ssl.qhimgs1.com/sdr/400__/t01d8dc1762ff3e8587.png', '4999', 'phone_standard'),
(104, 'Xiaomi 12', 3, 1, 'https://p1.ssl.qhimgs1.com/sdr/400__/t01c17faf76fad286e7.jpg', '3999', 'phone_standard'),
(201, 'Dell XPS 13', 4, 2, 'https://p0.ssl.qhimgs1.com/sdr/400__/t019917bf2354920df4.jpg', '8999', 'laptop_standard'),
(202, 'Lenovo ThinkPad X1', 5, 2, 'https://p0.ssl.qhimgs1.com/t016395af5b40a6ef1a.jpg', '10999', 'laptop_standard'),
(301, 'iPad 10th Gen', 6, 3, 'https://p1.ssl.qhimgs1.com/sdr/400__/t0134d2c14a3558930d.jpg', '3599', 'tablet_standard'),
(302, 'iPad Air 5', 6, 3, 'https://p0.ssl.qhimgs1.com/sdr/400__/t0106bff52a2fd0da7d.jpg', '4799', 'tablet_standard'),
(303, 'HUAWEI MatePad 11', 7, 3, 'https://p0.ssl.qhimgs1.com/sdr/400__/t014c560c30b7c1cd50.jpg', '2499', 'tablet_standard'),
(304, 'HUAWEI MatePad Pro 12.6', 7, 3, 'https://p1.ssl.qhimgs1.com/sdr/400__/t0103fdcf859f2f7b22.jpg', '4999', 'tablet_standard'),
(401, 'Galaxy Watch 6', 8, 4, 'https://p0.ssl.qhimgs1.com/sdr/400__/t0155f9f70d5fb3cd66.jpg', '2299', 'watch_standard'),
(402, 'Galaxy Watch 5', 8, 4, 'https://p0.ssl.qhimgs1.com/sdr/400__/t04bc2a3da6f0425e50.jpg', '1999', 'watch_standard');

INSERT IGNORE INTO `market_products`
(`id`, `seller_id`, `category_id`, `device_model_id`, `title`, `school`, `product_summary`, `description`, `selling_price`, `original_price`, `years_used`, `grade_label`, `defects`, `main_image`, `status`, `review_reject_reason`, `favorite_count`, `view_count`, `market_tag`, `created_at`)
VALUES
(2001, 4, 1, 101, 'iPhone 13 128G 9成新', '示例大学', '日常使用，电池健康88%，配件齐全。', '无拆修，屏幕有轻微划痕，拍照不明显。支持当面交易。', 3999, 5999, 1, 'A', '[\"scratch_screen\"]', 'https://p1.ssl.qhimgs1.com/sdr/400__/t048bfd78006729ebdb.png', 'on_sale', NULL, 12, 88, 'Recommend', NOW()),
(2002, 4, 2, 201, 'Dell XPS 13 16G/512G', '示例大学', '轻薄本，办公学习好用，键盘手感佳。', '外观正常磨损，功能全部正常。附原装充电器。', 4999, 8999, 2, 'B', '[\"battery_wear\"]', 'https://p0.ssl.qhimgs1.com/t016395af5b40a6ef1a.jpg', 'on_sale', NULL, 8, 56, 'Hot', NOW()),
(2003, 2, 1, 103, 'Samsung Galaxy S21', '示例大学', '正常使用痕迹，拍照清晰。', '边框有磕碰，屏幕无坏点。', 2500, 5000, 2, 'B', '[\"dent_body\"]', 'https://p0.ssl.qhimgs1.com/sdr/400__/t0151366c50b9bbd476.jpg', 'on_sale', NULL, 5, 44, 'New', NOW()),
(2004, 2, 1, 102, 'iPhone 12 64G 待审核示例', '示例大学', '示例：发布后待审核商品。', '用于演示 admin 审核列表。', 1999, 4999, 3, 'C', '[]', 'https://p0.ssl.qhimgs1.com/sdr/400__/t01d10690179d657aa0.jpg', 'pending_review', NULL, 0, 0, 'New', NOW()),
(2005, 2, 1, 104, 'Xiaomi 12 审核驳回示例', '示例大学', '示例：审核驳回商品。', '用于演示驳回原因展示。', 1999, 3999, 1, 'B', '[]', 'https://p3.ssl.qhimgs1.com/sdr/400__/t03d9b4057a79ec4b4d.jpg', 'rejected', '图片不清晰', 0, 0, 'New', NOW());

INSERT IGNORE INTO `market_product_images` (`id`, `product_id`, `url`, `sort_order`, `main_image`) VALUES
(1, 2001, 'https://p1.ssl.qhimgs1.com/sdr/400__/t044f4e8d3f573178cb.jpg', 0, b'1'),
(2, 2001, 'https://p1.ssl.qhimgs1.com/sdr/400__/t01b157173f764727e1.jpg', 1, b'0'),
(3, 2002, 'https://p1.ssl.qhimgs1.com/sdr/400__/t010e7a9a90aec45cf1.jpg', 0, b'1'),
(4, 2002, 'https://p2.ssl.qhimgs1.com/sdr/400__/t04cfc01fc916995788.png', 1, b'0'),
(5, 2003, 'https://p0.ssl.qhimgs1.com/sdr/400__/t01c8fc41fc30b25df0.jpg', 0, b'1');

INSERT IGNORE INTO `market_orders`
(`id`, `order_no`, `status`, `buyer_id`, `seller_id`, `product_id`, `pickup`, `amount`, `created_at`, `paid_at`)
VALUES
(3001, 'ORD-20260117-0001', 'pending_payment', 3, 4, 2001, b'0', 3999, NOW(), NULL),
(3002, 'ORD-20260117-0002', 'pending_shipment', 3, 4, 2002, b'1', 4999, NOW(), NOW()),
(3003, 'ORD-20260117-0003', 'shipped', 3, 4, 2003, b'0', 2500, NOW(), NOW()),
(3004, 'ORD-20260117-0004', 'completed', 3, 4, 2001, b'0', 3999, NOW(), NOW()),
(3005, 'ORD-20260117-0005', 'refunded', 3, 4, 2002, b'0', 4999, NOW(), NOW());

INSERT IGNORE INTO `market_product_comments`
(`id`, `product_id`, `sender_id`, `sender_role`, `content`, `created_at`)
VALUES
(9001, 2001, 3, 'buyer', '请问电池健康多少？有没有维修史？', NOW()),
(9002, 2001, 4, 'seller', '电池健康 88%，无维修史。屏幕有轻微划痕，拍照不明显。', NOW());

INSERT IGNORE INTO `market_drafts`
(`id`, `draft_key`, `seller_id`, `category_id`, `device_model_id`, `years_used`, `original_price`, `created_at`)
VALUES
(4001, 'mock-draft-key-123', 2, 1, 101, 1, 5000, NOW());

INSERT IGNORE INTO `market_draft_images` (`id`, `draft_id`, `url`, `sort_order`, `main_image`) VALUES
(5001, 4001, '/media/drafts/mock-draft-key-123/demo1.jpg', 0, b'1'),
(5002, 4001, '/media/drafts/mock-draft-key-123/demo2.jpg', 1, b'0');
