-- 集成测试种子数据（仅测试资源，非业务库脚本）
DELETE FROM employee WHERE username = 'admin';

INSERT INTO employee (id, name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user)
VALUES (1, '管理员', 'admin', 'e10adc3949ba59abbe56e057f20f883e', '13800000000', '1', '440301199001010001', 1, NOW(), NOW(), 1, 1);
