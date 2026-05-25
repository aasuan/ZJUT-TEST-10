-- M5 集成测试基础数据（user_id / dish_id 等使用 9001，避免与业务数据冲突）

DELETE FROM order_detail WHERE order_id IN (SELECT id FROM (SELECT id FROM orders WHERE user_id = 9001) t);
DELETE FROM orders WHERE user_id = 9001;
DELETE FROM shopping_cart WHERE user_id = 9001;
DELETE FROM address_book WHERE user_id = 9001;
DELETE FROM dish_flavor WHERE dish_id = 9001;
DELETE FROM dish WHERE id = 9001;
DELETE FROM category WHERE id = 9001;
DELETE FROM user WHERE id = 9001;

INSERT INTO employee (id, name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user)
VALUES (1, '管理员', 'admin', 'e10adc3949ba59abbe56e057f20f883e', '13812312312', '1', '110101199001011234', 1, NOW(), NOW(), 1, 1)
ON DUPLICATE KEY UPDATE password = 'e10adc3949ba59abbe56e057f20f883e', status = 1;

INSERT INTO category (id, type, name, sort, status, create_time, update_time, create_user, update_user)
VALUES (9001, 1, '集成测试分类', 1, 1, NOW(), NOW(), 1, 1);

INSERT INTO dish (id, name, category_id, price, image, description, status, create_time, update_time, create_user, update_user)
VALUES (9001, '集成测试菜品', 9001, 10.00, '', '集成测试', 1, NOW(), NOW(), 1, 1);

INSERT INTO user (id, openid, name, phone, sex, create_time)
VALUES (9001, 'm5-integration-openid', '集成测试用户', '13900009001', '1', NOW());

INSERT INTO address_book (id, user_id, consignee, sex, phone, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default)
VALUES (9001, 9001, '测试收货人', '1', '13900009001', '110000', '北京市', '110100', '北京市', '110101', '东城区', '测试地址1号', '家', 1);
