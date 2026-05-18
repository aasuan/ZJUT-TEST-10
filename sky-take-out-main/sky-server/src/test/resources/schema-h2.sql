SET NON_KEYWORDS VALUE;

DROP TABLE IF EXISTS dish_flavor;
DROP TABLE IF EXISTS dish;
DROP TABLE IF EXISTS category;

CREATE TABLE category (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    type        INT,
    name        VARCHAR(100),
    sort        INT,
    status      INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_user BIGINT
);

CREATE TABLE dish (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100),
    category_id BIGINT,
    price       DECIMAL(10,2),
    image       VARCHAR(200),
    description VARCHAR(500),
    status      INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_user BIGINT
);

CREATE TABLE dish_flavor (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    dish_id BIGINT,
    name    VARCHAR(50),
    value   VARCHAR(200)
);
