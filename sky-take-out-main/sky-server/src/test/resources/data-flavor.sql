INSERT INTO dish (id, name, category_id, price, status) VALUES
(1, '宫保鸡丁', 1, 28.00, 1),
(2, '白米饭',   2, 2.00,  1),
(3, '鱼香肉丝', 1, 32.00, 1);

ALTER TABLE dish ALTER COLUMN id RESTART WITH 4;

INSERT INTO dish_flavor (dish_id, name, value) VALUES
(1, '辣度', '微辣'),
(1, '口味', '咸鲜'),
(3, '分量', '大份');
