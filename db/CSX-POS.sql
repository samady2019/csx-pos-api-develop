CREATE TABLE `s_users` (
  `user_id` bigint AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(50),
  `password` VARCHAR(255),
  `user_type` VARCHAR(10) COMMENT '0=super_user, 1=vendor, 2=customer',
  `status` VARCHAR(10) COMMENT '1=active, 0=inactive'
);

CREATE TABLE `s_permission` (
  `permission_id` integer AUTO_INCREMENT PRIMARY KEY,
  `permission_name` VARCHAR(50)
);

CREATE TABLE `v_users` (
  `user_id` bigint AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(50),
  `password` VARCHAR(255),
  `user_type` VARCHAR(10) COMMENT '0=super_user, 1=vendor, 2=customer',
  `status` VARCHAR(10) COMMENT '1=active, 0=inactive'
);

CREATE TABLE `employee` (
  `employee_id` bigint AUTO_INCREMENT PRIMARY KEY,
  `user_id` bigint,
  `first_name` VARCHAR(50),
  `last_name` VARCHAR(50),
  `sex` VARCHAR(50),
  `age` VARCHAR(50),
  `address` VARCHAR(50),
  `email` VARCHAR(100),
  `phone` VARCHAR(20),
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `s_role_permission` (
  `role_id` integer,
  `permission_id` integer
);

CREATE TABLE `s_user_permission` (
  `user_id` integer,
  `permission_id` integer
);

CREATE TABLE `s_roles` (
  `role_id` integer AUTO_INCREMENT PRIMARY KEY,
  `name` varchar(255)
);

CREATE TABLE `s_user_roles` (
  `role_id` integer,
  `user_id` integer
);

CREATE TABLE `v_role_permission` (
  `role_id` integer,
  `permission_id` integer
);

CREATE TABLE `v_permission` (
  `permission_id` bigint AUTO_INCREMENT PRIMARY KEY,
  `permission_name` VARCHAR(50)
);

CREATE TABLE `v_user_permission` (
  `user_id` integer,
  `permission_id` integer
);

CREATE TABLE `v_roles` (
  `role_id` integer AUTO_INCREMENT PRIMARY KEY,
  `name` varchar(255)
);

CREATE TABLE `v_user_roles` (
  `role_id` integer,
  `user_id` integer
);


CREATE TABLE `vendors` (
  `vendor_id` bigint AUTO_INCREMENT PRIMARY KEY,
  `user_id` bigint,
  `first_name` VARCHAR(50),
  `last_name` VARCHAR(50),
  `shop_name_en` VARCHAR(255),
  `shop_name_kh` VARCHAR(255),
  `address` VARCHAR(255),
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `customers` (
  `customer_id` bigint AUTO_INCREMENT PRIMARY KEY,
  `user_id` bigint,
  `first_name` VARCHAR(50),
  `last_name` VARCHAR(50),
  `gender` varchar(10),
  `shipping_address` VARCHAR(255),
  `created_by` bigint,
  `updated_by` bigint,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `products` (
  `product_id` bigint AUTO_INCREMENT PRIMARY KEY,
  `product_code` varchar(255),
  `bar_code` varchar(255),
  `brand_id` bigint,
  `category_id` bigint,
  `currency` varchar(50),
  `product_name_en` varchar(255),
  `product_name_kh` varchar(255),
  `type` varchar(255) COMMENT '1=good, 0=Service',
  `status` VARCHAR(10) COMMENT '1=active, 0=inactive',
  `description` TEXT,
  `created_by` bigint,
  `updated_by` bigint,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE `units` (
  `unit_id` bigint AUTO_INCREMENT PRIMARY KEY,
  `p_unit_id` bigint,
  `unit_code` VARCHAR(10),
  `unit_name_en` VARCHAR(50),
  `unit_name_kh` VARCHAR(50),
  `value` DECIMAL(10,4),
  `description` VARCHAR(50)
);

CREATE TABLE `product_unit` (
  `product_id` bigint,
  `unit_id` bigint,
  `cost` DECIMAL(10,4),
  `price` DECIMAL(10,4),
  `default_unit` integer,
  `default_sale` integer,
  `default_purchase` integer,
  PRIMARY KEY (`product_id`, `unit_id`)
);

CREATE TABLE `brands` (
  `brand_id` bigint AUTO_INCREMENT PRIMARY KEY,
  `name_en` varchar(255),
  `name_kh` varchar(255),
  `status` VARCHAR(10) COMMENT '1=active, 0=inactive',
  `created_by` bigint,
  `updated_by` bigint,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `promotions` (
  `id` bigint AUTO_INCREMENT PRIMARY KEY,
  `vendor_id` bigint,
  `name` varchar(255),
  `description` varchar(255),
  `discount_percentage` varchar(50),
  `start_date` date,
  `end_date` date,
  `base_membership_point` int,
  `status` VARCHAR(10) COMMENT '1=active, 0=inactive',
  `created_by` bigint,
  `updated_by` bigint,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `payment_methods` (
  `id` bigint AUTO_INCREMENT PRIMARY KEY,
  `name` varchar(255),
  `description` varchar(255),
  `status` VARCHAR(10) COMMENT '1=active, 0=inactive',
  `created_by` bigint,
  `updated_by` bigint,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `categories` (
  `category_id` bigint AUTO_INCREMENT PRIMARY KEY,
  `p_category_id` bigint,
  `short_name_en` varchar(255),
  `short_name_kh` varchar(255),
  `name_en` varchar(255),
  `name_kh` varchar(255),
  `description_en` varchar(255),
  `description_kh` varchar(255),
  `status` VARCHAR(10) COMMENT '1=active, 0=inactive',
  `created_by` bigint,
  `updated_by` bigint,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `suppliers` (
  `id` bigint AUTO_INCREMENT PRIMARY KEY,
  `email` VARCHAR(100),
  `phone` VARCHAR(20),
  `address` varchar(255),
  `created_by` integer,
  `updated_by` integer,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `memberships` (
  `id` bigint AUTO_INCREMENT PRIMARY KEY,
  `customer_id` bigint,
  `point` int,
  `expired_date` date,
  `created_by` integer,
  `updated_by` integer,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `invoice` (
  `id` bigint AUTO_INCREMENT PRIMARY KEY,
  `invoice_date` date,
  `customer_id` integer,
  `customer_type` VARCHAR(10) COMMENT '1=vendor, 2=customer',
  `payment_method_id` integer,
  `order_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `total_amount` DECIMAL(10,2),
  `status` VARCHAR(10) COMMENT '0=pending, 1=verified, 2=canceled',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `invoice_item` (
  `id` bigint AUTO_INCREMENT PRIMARY KEY,
  `invoice_id` timestamp DEFAULT CURRENT_TIMESTAMP,
  `sid` bigint,
  `price` DECIMAL(10,2),
  `qty` int,
  `total` DECIMAL(10,2)
);

CREATE TABLE `stock` (
  `stock_id` bigint AUTO_INCREMENT PRIMARY KEY,
  `pid` bigint,
  `base_price` DECIMAL(10,2),
  `supply_price` DECIMAL(10,2),
  `qty` int
);

CREATE TABLE `purchase_order` (
  `purchase_order_no` bigint AUTO_INCREMENT PRIMARY KEY,
  `delivery_location` VARCHAR(255),
  `delivery_date` date,
  `net_total` DECIMAL(10,2),
  `created_by_id` VARCHAR(255),
  `po_date` date,
  `finalized_date` date,
  `po_status` boolean COMMENT 'false=pending, true=finalized',
  `supplier_id` bigint
);

CREATE TABLE `purchase_order_item` (
  `product_id` bigint,
  `id` bigint,
  `base_price` DECIMAL(10,2),
  `supply_price` DECIMAL(10,2),
  `qty` int,
  `desciption` VARCHAR(255),
  `purchase_order_no` int,
  PRIMARY KEY (`product_id`, `id`)
);

CREATE TABLE `grn` (
  `id` bigint AUTO_INCREMENT PRIMARY KEY,
  `grn_ref_no` bigint,
  `po_no` bigint,
  `delivery_location` VARCHAR(255),
  `delivery_date` date,
  `net_total` DECIMAL(10,2),
  `receive_user_id` VARCHAR(255),
  `supplier_id` bigint
);

CREATE TABLE `grn_item` (
  `grn_id` int,
  `id` bigint,
  `product_id` bigint,
  `base_price` DECIMAL(10,2),
  `supply_price` DECIMAL(10,2),
  `qty` int,
  `desciption` VARCHAR(255),
  PRIMARY KEY (`grn_id`, `id`)
);

ALTER TABLE `grn` COMMENT = 'good recieve note';

