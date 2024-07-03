CREATE DATABASE IF NOT EXISTS `oj` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;

USE `oj`;

DROP TABLE IF EXISTS user;
CREATE TABLE IF NOT EXISTS user (
                                    id VARCHAR(20) NOT NULL,
    username VARCHAR(45) NOT NULL,
    password VARCHAR(60) NOT NULL,
    section VARCHAR(45) DEFAULT 'none',
--     deleted INT DEFAULT 0,
    version INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    user_role VARCHAR(20) DEFAULT 'ROLE_USER',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB;

DROP TABLE IF EXISTS grade;
CREATE TABLE IF NOT EXISTS grade (
                                     file_name VARCHAR(100) NOT NULL,
    student_id VARCHAR(20) NOT NULL,
    language VARCHAR(10) NOT NULL,
    lab_id INT NOT NULL,
    section VARCHAR(45) NOT NULL,
    score DOUBLE NOT NULL,
--     deleted INT DEFAULT 0,
    version INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    PRIMARY KEY (file_name)
    ) ENGINE = InnoDB;

DROP TABLE IF EXISTS invitation_code;
CREATE TABLE IF NOT EXISTS invitation_code (
                                               section VARCHAR(20) NOT NULL,
    code VARCHAR(20) NOT NULL,
    version INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    PRIMARY KEY (section)
    ) ENGINE = InnoDB;

INSERT INTO user (id,username, password, create_time, update_time, user_role) VALUES ('admin','admin', '$2a$10$3RCHQ9PlfBf55heBFQLNj.Ew0/qqxN1oh8uAIyFyvsyvSCwYOvPSy', now(), now(), 'ROLE_ADMIN');
