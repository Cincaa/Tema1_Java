CREATE TABLE if not exists `lab8_spring`.`users`(
    `id`              BIGINT      NOT NULL AUTO_INCREMENT,
    `username`        VARCHAR(45) NOT NULL,
    `full_name`       VARCHAR(45) NULL,
    `user_type`       VARCHAR(45) NULL,
    `account_created` DATETIME NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE if not exists`lab8_spring`.`user_details`(
    `id`                BIGINT      NOT NULL AUTO_INCREMENT,
    `cnp`               VARCHAR(45) NOT NULL,
    `age`               INT NULL,
    `other_information` VARCHAR(255) NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE if not exists `lab8_spring`.`users_user_details`(
     `id`           BIGINT NOT NULL AUTO_INCREMENT,
     `users`        BIGINT NOT NULL,
     `user_details` BIGINT NOT NULL,
     PRIMARY KEY (`id`),
    FOREIGN KEY (`users`) REFERENCES `lab8_spring`.`users` (`id`),
    FOREIGN KEY (`user_details`) REFERENCES `lab8_spring`.`user_details` (`id`)
    );
