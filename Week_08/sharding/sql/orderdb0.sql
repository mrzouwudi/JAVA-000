/*
SQLyog Ultimate v9.63 
MySQL - 5.7.23 : Database - orderdb0
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`orderdb0` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin */;

USE `orderdb0`;

/*Table structure for table `t_order0` */

DROP TABLE IF EXISTS `t_order0`;

CREATE TABLE `t_order0` (
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
  `receiver_address` varchar(200) NOT NULL COMMENT '接收人地址',
  `receiver_name` varchar(30) NOT NULL COMMENT '接收人姓名',
  `receiver_mobile` char(11) DEFAULT NULL COMMENT '接收人手机',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `freight` int(11) NOT NULL DEFAULT '0' COMMENT '快递运费，以分为单位',
  `process_state` mediumint(9) NOT NULL DEFAULT '0' COMMENT '处理状态，0-下单未支付，1-已支付，待发货，2-已发货待收货，3收货，4取消订单，5退货',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '订单更新时间',
  `paid_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `deliveried_time` timestamp NULL DEFAULT NULL COMMENT '发货时间',
  PRIMARY KEY (`id`),
  KEY `user_id_idx_t_order0` (`user_id`),
  KEY `product_id_idx_t_order0` (`product_id`),
  KEY `created_idx_t_order0` (`created_time`),
  KEY `paid_idx_t_order0` (`paid_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `t_order1` */

DROP TABLE IF EXISTS `t_order1`;

CREATE TABLE `t_order1` (
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
  `receiver_address` varchar(200) NOT NULL COMMENT '接收人地址',
  `receiver_name` varchar(30) NOT NULL COMMENT '接收人姓名',
  `receiver_mobile` char(11) DEFAULT NULL COMMENT '接收人手机',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `freight` int(11) NOT NULL DEFAULT '0' COMMENT '快递运费，以分为单位',
  `process_state` mediumint(9) NOT NULL DEFAULT '0' COMMENT '处理状态，0-下单未支付，1-已支付，待发货，2-已发货待收货，3收货，4取消订单，5退货',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '订单更新时间',
  `paid_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `deliveried_time` timestamp NULL DEFAULT NULL COMMENT '发货时间',
  PRIMARY KEY (`id`),
  KEY `user_id_idx_t_order1` (`user_id`),
  KEY `product_id_idx_t_order1` (`product_id`),
  KEY `created_idx_t_order1` (`created_time`),
  KEY `paid_idx_t_order1` (`paid_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `t_order10` */

DROP TABLE IF EXISTS `t_order10`;

CREATE TABLE `t_order10` (
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
  `receiver_address` varchar(200) NOT NULL COMMENT '接收人地址',
  `receiver_name` varchar(30) NOT NULL COMMENT '接收人姓名',
  `receiver_mobile` char(11) DEFAULT NULL COMMENT '接收人手机',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `freight` int(11) NOT NULL DEFAULT '0' COMMENT '快递运费，以分为单位',
  `process_state` mediumint(9) NOT NULL DEFAULT '0' COMMENT '处理状态，0-下单未支付，1-已支付，待发货，2-已发货待收货，3收货，4取消订单，5退货',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '订单更新时间',
  `paid_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `deliveried_time` timestamp NULL DEFAULT NULL COMMENT '发货时间',
  PRIMARY KEY (`id`),
  KEY `user_id_idx_t_order10` (`user_id`),
  KEY `product_id_idx_t_order10` (`product_id`),
  KEY `created_idx_t_order10` (`created_time`),
  KEY `paid_idx_t_order10` (`paid_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `t_order11` */

DROP TABLE IF EXISTS `t_order11`;

CREATE TABLE `t_order11` (
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
  `receiver_address` varchar(200) NOT NULL COMMENT '接收人地址',
  `receiver_name` varchar(30) NOT NULL COMMENT '接收人姓名',
  `receiver_mobile` char(11) DEFAULT NULL COMMENT '接收人手机',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `freight` int(11) NOT NULL DEFAULT '0' COMMENT '快递运费，以分为单位',
  `process_state` mediumint(9) NOT NULL DEFAULT '0' COMMENT '处理状态，0-下单未支付，1-已支付，待发货，2-已发货待收货，3收货，4取消订单，5退货',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '订单更新时间',
  `paid_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `deliveried_time` timestamp NULL DEFAULT NULL COMMENT '发货时间',
  PRIMARY KEY (`id`),
  KEY `user_id_idx_t_order11` (`user_id`),
  KEY `product_id_idx_t_order11` (`product_id`),
  KEY `created_idx_t_order11` (`created_time`),
  KEY `paid_idx_t_order11` (`paid_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `t_order12` */

DROP TABLE IF EXISTS `t_order12`;

CREATE TABLE `t_order12` (
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
  `receiver_address` varchar(200) NOT NULL COMMENT '接收人地址',
  `receiver_name` varchar(30) NOT NULL COMMENT '接收人姓名',
  `receiver_mobile` char(11) DEFAULT NULL COMMENT '接收人手机',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `freight` int(11) NOT NULL DEFAULT '0' COMMENT '快递运费，以分为单位',
  `process_state` mediumint(9) NOT NULL DEFAULT '0' COMMENT '处理状态，0-下单未支付，1-已支付，待发货，2-已发货待收货，3收货，4取消订单，5退货',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '订单更新时间',
  `paid_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `deliveried_time` timestamp NULL DEFAULT NULL COMMENT '发货时间',
  PRIMARY KEY (`id`),
  KEY `user_id_idx_t_order12` (`user_id`),
  KEY `product_id_idx_t_order12` (`product_id`),
  KEY `created_idx_t_order12` (`created_time`),
  KEY `paid_idx_t_order12` (`paid_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `t_order13` */

DROP TABLE IF EXISTS `t_order13`;

CREATE TABLE `t_order13` (
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
  `receiver_address` varchar(200) NOT NULL COMMENT '接收人地址',
  `receiver_name` varchar(30) NOT NULL COMMENT '接收人姓名',
  `receiver_mobile` char(11) DEFAULT NULL COMMENT '接收人手机',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `freight` int(11) NOT NULL DEFAULT '0' COMMENT '快递运费，以分为单位',
  `process_state` mediumint(9) NOT NULL DEFAULT '0' COMMENT '处理状态，0-下单未支付，1-已支付，待发货，2-已发货待收货，3收货，4取消订单，5退货',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '订单更新时间',
  `paid_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `deliveried_time` timestamp NULL DEFAULT NULL COMMENT '发货时间',
  PRIMARY KEY (`id`),
  KEY `user_id_idx_t_order13` (`user_id`),
  KEY `product_id_idx_t_order13` (`product_id`),
  KEY `created_idx_t_order13` (`created_time`),
  KEY `paid_idx_t_order13` (`paid_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `t_order14` */

DROP TABLE IF EXISTS `t_order14`;

CREATE TABLE `t_order14` (
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
  `receiver_address` varchar(200) NOT NULL COMMENT '接收人地址',
  `receiver_name` varchar(30) NOT NULL COMMENT '接收人姓名',
  `receiver_mobile` char(11) DEFAULT NULL COMMENT '接收人手机',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `freight` int(11) NOT NULL DEFAULT '0' COMMENT '快递运费，以分为单位',
  `process_state` mediumint(9) NOT NULL DEFAULT '0' COMMENT '处理状态，0-下单未支付，1-已支付，待发货，2-已发货待收货，3收货，4取消订单，5退货',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '订单更新时间',
  `paid_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `deliveried_time` timestamp NULL DEFAULT NULL COMMENT '发货时间',
  PRIMARY KEY (`id`),
  KEY `user_id_idx_t_order14` (`user_id`),
  KEY `product_id_idx_t_order14` (`product_id`),
  KEY `created_idx_t_order14` (`created_time`),
  KEY `paid_idx_t_order14` (`paid_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `t_order15` */

DROP TABLE IF EXISTS `t_order15`;

CREATE TABLE `t_order15` (
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
  `receiver_address` varchar(200) NOT NULL COMMENT '接收人地址',
  `receiver_name` varchar(30) NOT NULL COMMENT '接收人姓名',
  `receiver_mobile` char(11) DEFAULT NULL COMMENT '接收人手机',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `freight` int(11) NOT NULL DEFAULT '0' COMMENT '快递运费，以分为单位',
  `process_state` mediumint(9) NOT NULL DEFAULT '0' COMMENT '处理状态，0-下单未支付，1-已支付，待发货，2-已发货待收货，3收货，4取消订单，5退货',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '订单更新时间',
  `paid_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `deliveried_time` timestamp NULL DEFAULT NULL COMMENT '发货时间',
  PRIMARY KEY (`id`),
  KEY `user_id_idx_t_order15` (`user_id`),
  KEY `product_id_idx_t_order15` (`product_id`),
  KEY `created_idx_t_order15` (`created_time`),
  KEY `paid_idx_t_order15` (`paid_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `t_order2` */

DROP TABLE IF EXISTS `t_order2`;

CREATE TABLE `t_order2` (
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
  `receiver_address` varchar(200) NOT NULL COMMENT '接收人地址',
  `receiver_name` varchar(30) NOT NULL COMMENT '接收人姓名',
  `receiver_mobile` char(11) DEFAULT NULL COMMENT '接收人手机',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `freight` int(11) NOT NULL DEFAULT '0' COMMENT '快递运费，以分为单位',
  `process_state` mediumint(9) NOT NULL DEFAULT '0' COMMENT '处理状态，0-下单未支付，1-已支付，待发货，2-已发货待收货，3收货，4取消订单，5退货',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '订单更新时间',
  `paid_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `deliveried_time` timestamp NULL DEFAULT NULL COMMENT '发货时间',
  PRIMARY KEY (`id`),
  KEY `user_id_idx_t_order2` (`user_id`),
  KEY `product_id_idx_t_order2` (`product_id`),
  KEY `created_idx_t_order2` (`created_time`),
  KEY `paid_idx_t_order2` (`paid_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `t_order3` */

DROP TABLE IF EXISTS `t_order3`;

CREATE TABLE `t_order3` (
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
  `receiver_address` varchar(200) NOT NULL COMMENT '接收人地址',
  `receiver_name` varchar(30) NOT NULL COMMENT '接收人姓名',
  `receiver_mobile` char(11) DEFAULT NULL COMMENT '接收人手机',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `freight` int(11) NOT NULL DEFAULT '0' COMMENT '快递运费，以分为单位',
  `process_state` mediumint(9) NOT NULL DEFAULT '0' COMMENT '处理状态，0-下单未支付，1-已支付，待发货，2-已发货待收货，3收货，4取消订单，5退货',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '订单更新时间',
  `paid_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `deliveried_time` timestamp NULL DEFAULT NULL COMMENT '发货时间',
  PRIMARY KEY (`id`),
  KEY `user_id_idx_t_order3` (`user_id`),
  KEY `product_id_idx_t_order3` (`product_id`),
  KEY `created_idx_t_order3` (`created_time`),
  KEY `paid_idx_t_order3` (`paid_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `t_order4` */

DROP TABLE IF EXISTS `t_order4`;

CREATE TABLE `t_order4` (
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
  `receiver_address` varchar(200) NOT NULL COMMENT '接收人地址',
  `receiver_name` varchar(30) NOT NULL COMMENT '接收人姓名',
  `receiver_mobile` char(11) DEFAULT NULL COMMENT '接收人手机',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `freight` int(11) NOT NULL DEFAULT '0' COMMENT '快递运费，以分为单位',
  `process_state` mediumint(9) NOT NULL DEFAULT '0' COMMENT '处理状态，0-下单未支付，1-已支付，待发货，2-已发货待收货，3收货，4取消订单，5退货',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '订单更新时间',
  `paid_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `deliveried_time` timestamp NULL DEFAULT NULL COMMENT '发货时间',
  PRIMARY KEY (`id`),
  KEY `user_id_idx_t_order4` (`user_id`),
  KEY `product_id_idx_t_order4` (`product_id`),
  KEY `created_idx_t_order4` (`created_time`),
  KEY `paid_idx_t_order4` (`paid_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `t_order5` */

DROP TABLE IF EXISTS `t_order5`;

CREATE TABLE `t_order5` (
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
  `receiver_address` varchar(200) NOT NULL COMMENT '接收人地址',
  `receiver_name` varchar(30) NOT NULL COMMENT '接收人姓名',
  `receiver_mobile` char(11) DEFAULT NULL COMMENT '接收人手机',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `freight` int(11) NOT NULL DEFAULT '0' COMMENT '快递运费，以分为单位',
  `process_state` mediumint(9) NOT NULL DEFAULT '0' COMMENT '处理状态，0-下单未支付，1-已支付，待发货，2-已发货待收货，3收货，4取消订单，5退货',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '订单更新时间',
  `paid_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `deliveried_time` timestamp NULL DEFAULT NULL COMMENT '发货时间',
  PRIMARY KEY (`id`),
  KEY `user_id_idx_t_order5` (`user_id`),
  KEY `product_id_idx_t_order5` (`product_id`),
  KEY `created_idx_t_order5` (`created_time`),
  KEY `paid_idx_t_order5` (`paid_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `t_order6` */

DROP TABLE IF EXISTS `t_order6`;

CREATE TABLE `t_order6` (
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
  `receiver_address` varchar(200) NOT NULL COMMENT '接收人地址',
  `receiver_name` varchar(30) NOT NULL COMMENT '接收人姓名',
  `receiver_mobile` char(11) DEFAULT NULL COMMENT '接收人手机',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `freight` int(11) NOT NULL DEFAULT '0' COMMENT '快递运费，以分为单位',
  `process_state` mediumint(9) NOT NULL DEFAULT '0' COMMENT '处理状态，0-下单未支付，1-已支付，待发货，2-已发货待收货，3收货，4取消订单，5退货',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '订单更新时间',
  `paid_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `deliveried_time` timestamp NULL DEFAULT NULL COMMENT '发货时间',
  PRIMARY KEY (`id`),
  KEY `user_id_idx_t_order6` (`user_id`),
  KEY `product_id_idx_t_order6` (`product_id`),
  KEY `created_idx_t_order6` (`created_time`),
  KEY `paid_idx_t_order6` (`paid_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `t_order7` */

DROP TABLE IF EXISTS `t_order7`;

CREATE TABLE `t_order7` (
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
  `receiver_address` varchar(200) NOT NULL COMMENT '接收人地址',
  `receiver_name` varchar(30) NOT NULL COMMENT '接收人姓名',
  `receiver_mobile` char(11) DEFAULT NULL COMMENT '接收人手机',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `freight` int(11) NOT NULL DEFAULT '0' COMMENT '快递运费，以分为单位',
  `process_state` mediumint(9) NOT NULL DEFAULT '0' COMMENT '处理状态，0-下单未支付，1-已支付，待发货，2-已发货待收货，3收货，4取消订单，5退货',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '订单更新时间',
  `paid_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `deliveried_time` timestamp NULL DEFAULT NULL COMMENT '发货时间',
  PRIMARY KEY (`id`),
  KEY `user_id_idx_t_order7` (`user_id`),
  KEY `product_id_idx_t_order7` (`product_id`),
  KEY `created_idx_t_order7` (`created_time`),
  KEY `paid_idx_t_order7` (`paid_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `t_order8` */

DROP TABLE IF EXISTS `t_order8`;

CREATE TABLE `t_order8` (
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
  `receiver_address` varchar(200) NOT NULL COMMENT '接收人地址',
  `receiver_name` varchar(30) NOT NULL COMMENT '接收人姓名',
  `receiver_mobile` char(11) DEFAULT NULL COMMENT '接收人手机',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `freight` int(11) NOT NULL DEFAULT '0' COMMENT '快递运费，以分为单位',
  `process_state` mediumint(9) NOT NULL DEFAULT '0' COMMENT '处理状态，0-下单未支付，1-已支付，待发货，2-已发货待收货，3收货，4取消订单，5退货',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '订单更新时间',
  `paid_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `deliveried_time` timestamp NULL DEFAULT NULL COMMENT '发货时间',
  PRIMARY KEY (`id`),
  KEY `user_id_idx_t_order8` (`user_id`),
  KEY `product_id_idx_t_order8` (`product_id`),
  KEY `created_idx_t_order8` (`created_time`),
  KEY `paid_idx_t_order8` (`paid_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `t_order9` */

DROP TABLE IF EXISTS `t_order9`;

CREATE TABLE `t_order9` (
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
  `receiver_address` varchar(200) NOT NULL COMMENT '接收人地址',
  `receiver_name` varchar(30) NOT NULL COMMENT '接收人姓名',
  `receiver_mobile` char(11) DEFAULT NULL COMMENT '接收人手机',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `freight` int(11) NOT NULL DEFAULT '0' COMMENT '快递运费，以分为单位',
  `process_state` mediumint(9) NOT NULL DEFAULT '0' COMMENT '处理状态，0-下单未支付，1-已支付，待发货，2-已发货待收货，3收货，4取消订单，5退货',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_time` timestamp NULL DEFAULT NULL COMMENT '订单更新时间',
  `paid_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `deliveried_time` timestamp NULL DEFAULT NULL COMMENT '发货时间',
  PRIMARY KEY (`id`),
  KEY `user_id_idx_t_order9` (`user_id`),
  KEY `product_id_idx_t_order9` (`product_id`),
  KEY `created_idx_t_order9` (`created_time`),
  KEY `paid_idx_t_order9` (`paid_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
