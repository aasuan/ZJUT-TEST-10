-- MySQL 语法 - 与 OrderMapper.xml 中 orders 表字段对齐（测试用）
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    number VARCHAR(64),
    status INT,
    user_id BIGINT,
    address_book_id BIGINT,
    order_time TIMESTAMP,
    checkout_time TIMESTAMP,
    pay_method INT,
    pay_status INT,
    amount DECIMAL(10, 2),
    remark VARCHAR(255),
    phone VARCHAR(32),
    address VARCHAR(255),
    user_name VARCHAR(64),
    consignee VARCHAR(64),
    cancel_reason VARCHAR(255),
    rejection_reason VARCHAR(255),
    cancel_time TIMESTAMP,
    estimated_delivery_time TIMESTAMP,
    delivery_status INT,
    delivery_time TIMESTAMP,
    pack_amount INT DEFAULT 0,
    tableware_number INT DEFAULT 0,
    tableware_status INT,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_order_time (order_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 套餐表
CREATE TABLE IF NOT EXISTS setmeal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    category_id BIGINT,
    price DECIMAL(10, 2),
    image VARCHAR(255),
    description VARCHAR(255),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_user BIGINT,
    INDEX idx_category_id (category_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 套餐菜品关联表
CREATE TABLE IF NOT EXISTS setmeal_dish (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setmeal_id BIGINT NOT NULL,
    dish_id BIGINT NOT NULL,
    name VARCHAR(64),
    price DECIMAL(10, 2),
    copies INT DEFAULT 1,
    INDEX idx_setmeal_id (setmeal_id),
    INDEX idx_dish_id (dish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 菜品表
CREATE TABLE IF NOT EXISTS dish (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    category_id BIGINT,
    price DECIMAL(10, 2),
    image VARCHAR(255),
    description VARCHAR(255),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_user BIGINT,
    INDEX idx_category_id (category_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;