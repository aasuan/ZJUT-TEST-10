-- 苍穹外卖集成测试初始化数据
-- 管理员用户密码: 123456 (MD5加密后的值)
-- MD5("123456") = e10adc3949ba59abbe56e057f20f883e

INSERT INTO employee (id, name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user)
VALUES (1, '管理员', 'admin', 'e10adc3949ba59abbe56e057f20f883e', '13800138000', '1', '110101199001011234', 1, NOW(), NOW(), 0, 0)
ON DUPLICATE KEY UPDATE 
    password = VALUES(password),
    update_time = NOW();

-- 初始化分类数据
INSERT INTO category (id, name, type, sort, status, create_time, update_time, create_user, update_user)
VALUES 
    (1, '热销菜品', 1, 1, 1, NOW(), NOW(), 1, 1),
    (2, '新品上市', 1, 2, 1, NOW(), NOW(), 1, 1)
ON DUPLICATE KEY UPDATE 
    update_time = NOW();

-- 初始化菜品数据
INSERT INTO dish (id, name, category_id, price, image, description, status, create_time, update_time, create_user, update_user)
VALUES 
    (1, '宫保鸡丁', 1, 28.00, 'gongbao.jpg', '经典川菜，鲜香可口', 1, NOW(), NOW(), 1, 1),
    (2, '鱼香肉丝', 1, 26.00, 'yuxiang.jpg', '酸甜微辣，下饭神器', 1, NOW(), NOW(), 1, 1)
ON DUPLICATE KEY UPDATE 
    update_time = NOW();

-- 初始化套餐数据
INSERT INTO setmeal (id, name, category_id, price, image, description, status, create_time, update_time, create_user, update_user)
VALUES 
    (1, '超值套餐A', 2, 58.00, 'setmeal_a.jpg', '包含宫保鸡丁+鱼香肉丝+米饭', 1, NOW(), NOW(), 1, 1)
ON DUPLICATE KEY UPDATE 
    update_time = NOW();

-- 初始化套餐菜品关联
INSERT INTO setmeal_dish (setmeal_id, dish_id, name, price, copies)
VALUES 
    (1, 1, '宫保鸡丁', 28.00, 1),
    (1, 2, '鱼香肉丝', 26.00, 1)
ON DUPLICATE KEY UPDATE 
    price = VALUES(price),
    copies = VALUES(copies);
