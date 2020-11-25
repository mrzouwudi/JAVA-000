# 作业说明
## Week06 作业题目（周六）：
2.（必做）基于电商交易场景（用户、商品、订单），设计一套简单的表结构，提交 DDL 的 SQL 文件到 Github（后面 2 周的作业依然要是用到这个表结构）。

（1）用户表
```
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
```
因为是简化的设计，只保留基础信息，自增主键id，显示的用户名；登录的用户名和密码，用于推送短信的手机号；创建时间，更新时间，以及表示逻辑删除的标志。其中密码是经过散列后进行保存，40个字符可以保存SHA1的散列字符串。

（2）商品表
```
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
```
id是自增主键；product_name是商品名称；product_code是商品编码，主要是用于商品实际管理使用，比如进行盘点，进货；product_pic是图片的文件路径，可以是完整的http链接或是相对路径，这里出于简化，不考虑有多张商品主图用于详情页进行轮播；product_price商品单价，以分为单位；product_storage商品库存，其实是展示的库存，这里出于简化没有设计安全库存等字段；product_introduction商品简述，这里简化设计没有放商品详情，如果实际应该单独有商品详情表，有个字段是text或clob，不适合放在商品表；display_order显示排序，可以手工设置显示的次序（比如列表页）；on_sale_flag上下架标志，默认是下架；created_time创建时间，updated_time更新时间

（3）订单表
```
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
```
订单表设计为了简化设计，假设是一个订单只能买一种商品，因此省去订单详情表（t_order_item）。id是主键，这个id由程序生成，比如雪花算法；user_id是用户标识，关联到用户表；product_id是商品标识，可以关联到商品表；product_code，product_name，procuct_introduction，product_price组成商品快照；product_amout，是购买的商品数量；discount是折扣，以分为单位，这里优惠直接体现为折扣掉的金额；total_price是订单支付时的总价，简化设计后total_price=product_amout*product_price + freight - discount，其中freight是运费，discount和freight的缺省值是0；receiver_address收货人地址，这里简化设计，没有将地址拆成省市街道，并和相应的区域表进行关联；receiver_name收货人姓名；receiver_mobile收货人电话；remark备注；process_state表示订单的各种状态，0-下单未支付，1-已支付，待发货，2-已发货待收货，3-收货，4-取消订单，5-退货；created_time下单时间，updated_time更新时间；paid_time支付时间；deliveried_time发货时间。