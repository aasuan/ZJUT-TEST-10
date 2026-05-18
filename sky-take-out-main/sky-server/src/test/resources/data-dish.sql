INSERT INTO category (id, type, name, sort, status) VALUES
(1, 1, '热销套餐',   1, 1),
(2, 2, '超值套餐',   2, 1),
(3, 1, '限时特惠',   3, 0),
(4, 2, '商务套餐',   4, 1),
(5, 1, '人气单品',   5, 1);

ALTER TABLE category ALTER COLUMN id RESTART WITH 6;

INSERT INTO dish (id, name, category_id, price, image, description, status) VALUES
(1,  '宫保鸡丁',   1, 28.00, 'gongbao.jpg',   '经典川菜',        1),
(2,  '鱼香肉丝',   1, 32.00, 'yuxiang.jpg',   '四川风味',        1),
(3,  '麻婆豆腐',   1, 18.00, 'mapo.jpg',      '麻辣鲜香',        0),
(4,  '回锅肉',     1, 35.00, 'huiguo.jpg',    '家常味道',        1),
(5,  '水煮鱼',     1, 48.00, 'shuizhu.jpg',   '重庆特色',        1),
(6,  '烤鸭半只',   2, 68.00, 'kaoya.jpg',     '北京烤鸭',        1),
(7,  '红烧肉',     2, 38.00, 'hongshao.jpg',  '上海本帮菜',      1),
(8,  '糖醋排骨',   2, 42.00, 'tangcu.jpg',    '酸甜可口',        0),
(9,  '清蒸鲈鱼',   2, 58.00, 'qingzheng.jpg', '粤式清蒸',        1),
(10, '干煸四季豆', 2, 22.00, 'ganbian.jpg',   '素菜经典',        1),
(11, '白米饭',     3, 2.00,  'rice.jpg',      '主食',            1),
(12, '蛋炒饭',     3, 12.00, 'danchaofan.jpg','炒饭',            1),
(13, '牛肉面',     3, 18.00, 'niumian.jpg',   '面食',            0),
(14, '酸辣粉',     3, 15.00, 'suanla.jpg',    '重庆小吃',        1),
(15, '炸鸡翅',     4, 20.00, 'zhaji.jpg',     '小吃',            1),
(16, '春卷',       4, 16.00, 'chunjuan.jpg',  '传统点心',        1),
(17, '饺子',       4, 22.00, 'jiaozi.jpg',    '手工水饺',        0),
(18, '鸡丝凉面',   5, 16.00, 'jisi.jpg',      '凉菜',            1),
(19, '蒜泥白肉',   5, 26.00, 'suanni.jpg',    '川味凉菜',        1),
(20, '拍黄瓜',     5, 8.00,  'paiguang.jpg',  '开胃小菜',        1);

ALTER TABLE dish ALTER COLUMN id RESTART WITH 21;
