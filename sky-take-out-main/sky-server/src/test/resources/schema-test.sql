-- H2 兼容语法 - test profile 专用（仅建表，无删表）
SET NON_KEYWORDS VALUE;

CREATE TABLE IF NOT EXISTS employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100),
    phone VARCHAR(20),
    sex VARCHAR(10),
    id_number VARCHAR(50),
    status INT,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    create_user BIGINT,
    update_user BIGINT,
    CONSTRAINT uk_employee_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    number VARCHAR(64), status INT, user_id BIGINT, address_book_id BIGINT,
    order_time TIMESTAMP, checkout_time TIMESTAMP, pay_method INT, pay_status INT,
    amount DECIMAL(10, 2), remark VARCHAR(255), phone VARCHAR(32), address VARCHAR(255),
    user_name VARCHAR(64), consignee VARCHAR(64), cancel_reason VARCHAR(255),
    rejection_reason VARCHAR(255), cancel_time TIMESTAMP, estimated_delivery_time TIMESTAMP,
    delivery_status INT, delivery_time TIMESTAMP, pack_amount INT DEFAULT 0,
    tableware_number INT DEFAULT 0, tableware_status INT
);

CREATE TABLE IF NOT EXISTS order_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64), image VARCHAR(255), order_id BIGINT, dish_id BIGINT,
    setmeal_id BIGINT, dish_flavor VARCHAR(64), number INT, amount DECIMAL(10, 2)
);

CREATE TABLE IF NOT EXISTS setmeal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL, category_id BIGINT, price DECIMAL(10, 2),
    image VARCHAR(255), description VARCHAR(255), status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT, update_user BIGINT
);

CREATE TABLE IF NOT EXISTS setmeal_dish (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setmeal_id BIGINT NOT NULL, dish_id BIGINT NOT NULL,
    name VARCHAR(64), price DECIMAL(10, 2), copies INT DEFAULT 1
);

CREATE TABLE IF NOT EXISTS dish (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL, category_id BIGINT, price DECIMAL(10, 2),
    image VARCHAR(255), description VARCHAR(255), status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT, update_user BIGINT
);

CREATE TABLE IF NOT EXISTS shopping_cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64), image VARCHAR(255), user_id BIGINT, dish_id BIGINT,
    setmeal_id BIGINT, dish_flavor VARCHAR(64), number INT, amount DECIMAL(10, 2),
    create_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type INT, name VARCHAR(100), sort INT, status INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT, update_user BIGINT
);

CREATE TABLE IF NOT EXISTS dish_flavor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dish_id BIGINT, name VARCHAR(50), value VARCHAR(200)
);
