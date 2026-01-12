CREATE TABLE `t_orders`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `order_number` varchar(255) DEFAULT NULL,
    `sku_code` varchar(255),
    `price` DECIMAL(19, 2),
    `quantity` INT(11),
    PRIMARY KEY (`id`)
);