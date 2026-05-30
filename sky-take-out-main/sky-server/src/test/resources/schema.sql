-- H2 兼容语法 - 测试用
SET NON_KEYWORDS VALUE;

-- 购物车表
CREATE TABLE IF NOT EXISTS shopping_cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64),
    image VARCHAR(255),
    user_id BIGINT,
    dish_id BIGINT,
    setmeal_id BIGINT,
    dish_flavor VARCHAR(64),
    number INT,
    amount DECIMAL(10, 2),
    create_time TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_shopping_cart_user_id ON shopping_cart(user_id);

-- 订单表
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
    tableware_status INT
);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_order_time ON orders(order_time);

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
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_user BIGINT
);
CREATE INDEX IF NOT EXISTS idx_setmeal_category_id ON setmeal(category_id);
CREATE INDEX IF NOT EXISTS idx_setmeal_status ON setmeal(status);

-- 套餐菜品关联表
CREATE TABLE IF NOT EXISTS setmeal_dish (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setmeal_id BIGINT NOT NULL,
    dish_id BIGINT NOT NULL,
    name VARCHAR(64),
    price DECIMAL(10, 2),
    copies INT DEFAULT 1
);
CREATE INDEX IF NOT EXISTS idx_setmeal_dish_setmeal_id ON setmeal_dish(setmeal_id);
CREATE INDEX IF NOT EXISTS idx_setmeal_dish_dish_id ON setmeal_dish(dish_id);

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
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_user BIGINT
);
CREATE INDEX IF NOT EXISTS idx_dish_category_id ON dish(category_id);
CREATE INDEX IF NOT EXISTS idx_dish_status ON dish(status);

-- 订单明细表
CREATE TABLE IF NOT EXISTS order_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64),
    image VARCHAR(255),
    order_id BIGINT,
    dish_id BIGINT,
    setmeal_id BIGINT,
    dish_flavor VARCHAR(64),
    number INT,
    amount DECIMAL(10, 2)
);
CREATE INDEX IF NOT EXISTS idx_order_detail_order_id ON order_detail(order_id);
