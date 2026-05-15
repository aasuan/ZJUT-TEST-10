-- H2 建表脚本，供 Mapper 层单元测试使用

CREATE TABLE IF NOT EXISTS shopping_cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64),
    image VARCHAR(255),
    user_id BIGINT NOT NULL,
    dish_id BIGINT,
    setmeal_id BIGINT,
    dish_flavor VARCHAR(200),
    number INT DEFAULT 1,
    amount DECIMAL(10, 2),
    create_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64),
    image VARCHAR(255),
    order_id BIGINT NOT NULL,
    dish_id BIGINT,
    setmeal_id BIGINT,
    dish_flavor VARCHAR(200),
    number INT DEFAULT 1,
    amount DECIMAL(10, 2)
);
