/*
SQLyog Ultimate v9.63 
MySQL - 5.7.23 : Database - tinymall
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`tinymall` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

USE `tinymall`;

/*Table structure for table `t_order` */

DROP TABLE IF EXISTS `t_order`;

CREATE TABLE `t_order` (
  `id` bigint(20) NOT NULL COMMENT '订单标识，可以由程序生成',
  `user_id` int(11) NOT NULL COMMENT '用户标识',
  `product_id` int(11) NOT NULL COMMENT '商品标识',
  `product_code` varchar(50) DEFAULT NULL COMMENT '商品编码',
  `product_name` varchar(30) NOT NULL COMMENT '商品名称',
  `product_pic` varchar(255) DEFAULT NULL COMMENT '商品图片文件路径',
  `procuct_introduction` varchar(255) DEFAULT NULL COMMENT '商品描述',
  `product_amout` int(11) NOT NULL COMMENT '商品数量',
  `product_price` int(11) NOT NULL COMMENT '商品售价，以分为单位',
  `discount` int(11) NOT NULL DEFAULT '0' COMMENT '折扣，以分为单位',
  `total_price` int(11) NOT NULL COMMENT '订单支付总价，含运费扣除折扣，以分为单位',
  `receiver_address` varchar(200) NOT NULL COMMENT '收货人地址',
  `receiver_name` varchar(30) NOT NULL COMMENT '收货人姓名',
  `receiver_mobile` char(11) DEFAULT NULL COMMENT '收货人手机',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `freight` int(11) NOT NULL DEFAULT '0' COMMENT '快递运费，以分为单位',
  `process_state` mediumint(9) NOT NULL DEFAULT '0' COMMENT '处理状态，0-下单未支付，1-已支付，待发货，2-已发货待收货，3-收货，4-取消订单，5-退货',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '订单更新时间',
  `paid_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `deliveried_time` timestamp NULL DEFAULT NULL COMMENT '发货时间',
  PRIMARY KEY (`id`),
  KEY `user_id_idx` (`user_id`),
  KEY `product_id_idx` (`product_id`),
  KEY `created_idx` (`created_time`),
  KEY `paid_idx` (`paid_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `t_product` */

DROP TABLE IF EXISTS `t_product`;

CREATE TABLE `t_product` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '商品标识，自增主键',
  `product_name` varchar(30) NOT NULL COMMENT '商品名称',
  `product_code` varchar(50) NOT NULL COMMENT '商品编码',
  `product_pic` varchar(255) NOT NULL COMMENT '商品图片的文件路径',
  `product_price` int(11) NOT NULL COMMENT '商品价格，以分为单位',
  `product_storage` int(11) NOT NULL COMMENT '商品库存',
  `product_introduction` varchar(255) DEFAULT NULL COMMENT '商品简介',
  `display_order` mediumint(9) NOT NULL DEFAULT '100' COMMENT '显示排序，数大显示靠前',
  `on_sale_flag` tinyint(4) DEFAULT '0' COMMENT '上架标志，0-下架，1-上架',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  KEY `id_idx` (`id`),
  KEY `display_order_idx` (`display_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `t_user` */

DROP TABLE IF EXISTS `t_user`;

CREATE TABLE `t_user` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '用户标识，主键',
  `nickname` varchar(30) DEFAULT NULL COMMENT '用户昵称',
  `login_name` varchar(30) DEFAULT NULL COMMENT '用户登录名',
  `password` char(40) DEFAULT NULL COMMENT '登录密码，用sha-1散列',
  `mobile` char(11) DEFAULT NULL COMMENT '11位手机号码',
  `created_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `is_delete` tinyint(4) DEFAULT NULL COMMENT '删除标志，0-未删除，1-已删除',
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
