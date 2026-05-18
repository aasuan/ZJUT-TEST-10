DROP TABLE IF EXISTS employee;

CREATE TABLE employee (
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
