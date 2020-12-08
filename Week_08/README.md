# 作业说明
## Week08 作业题目（周四）：
2.（必做）设计对前面的订单表数据进行水平分库分表，拆分 2 个库，每个库 16 张表。并在新结构在演示常见的增删改查操作。代码、sql 和配置文件，上传到 Github。

（1）在MySql数据库中创建两个数据库orderdb0和orderdb1

（2）通过ShardingSphere proxy各自在刚创建的两个数据库中创建16张表，表名是t_order0到t_order15，步骤如下：
 【a】：配置ShardingSphere proxy，在conf目录下server.yaml配置成如下：
 ```
#governance:
#  name: governance_ds
#  registryCenter:
#    type: ZooKeeper
#    serverLists: localhost:2181
#    props:
#      retryIntervalMilliseconds: 500
#      timeToLiveSeconds: 60
#      maxRetries: 3
#      operationTimeoutMilliseconds: 500
#  overwrite: false

authentication:
 users:
   root:
     password: root
#  sharding:
#    password: sharding 
#    authorizedSchemas: sharding_db

props:
 max-connections-size-per-query: 1
 acceptor-size: 16  # The default value is available processors count * 2.
 executor-size: 16  # Infinite by default.
 proxy-frontend-flush-threshold: 128  # The default value is 128.
   # LOCAL: Proxy will run with LOCAL transaction.
   # XA: Proxy will run with XA transaction.
   # BASE: Proxy will run with B.A.S.E transaction.
 proxy-transaction-type: LOCAL
 proxy-opentracing-enabled: false
 proxy-hint-enabled: false
 query-with-cipher-column: true
 sql-show: true
 check-table-metadata-enabled: false
 ```
 然后是config-sharding.yaml，进行如下配置：
 ```
 schemaName: sharding_db

dataSourceCommon:
 username: root
 password: root
 connectionTimeoutMilliseconds: 30000
 idleTimeoutMilliseconds: 60000
 maxLifetimeMilliseconds: 1800000
 maxPoolSize: 50
 minPoolSize: 1
 maintenanceIntervalMilliseconds: 30000

dataSources:
 db0:
   url: jdbc:mysql://127.0.0.1:3306/orderdb0?serverTimezone=UTC&useSSL=false
 db1:
   url: jdbc:mysql://127.0.0.1:3306/orderdb1?serverTimezone=UTC&useSSL=false

rules:
- !SHARDING
 tables:
   t_order:
     actualDataNodes: db${0..1}.t_order${0..15}
     tableStrategy:
       standard:
         shardingColumn: id
         shardingAlgorithmName: order_inline
     keyGenerateStrategy:
       column: id
       keyGeneratorName: snowflake
 defaultDatabaseStrategy:
   standard:
     shardingColumn: user_id
     shardingAlgorithmName: database_inline
 defaultTableStrategy:
   none:

 shardingAlgorithms:
   database_inline:
     type: INLINE
     props:
       algorithm-expression: db${user_id % 2}
   order_inline:
     type: INLINE
     props:
       algorithm-expression: t_order${id % 16}

 keyGenerators:
   snowflake:
     type: SNOWFLAKE
     props:
       worker-id: 123
 ```
这里在分库分表时，先按用户ID（user_id）取模2，找到对应分库，然后根据订单ID（id）取模16，找到对应的分表。这里有一点说明，虽然订单表的id采用了snowflake（雪花算法）生成，而且实际测试也可以通过客户端正常插入数据，但是因为使用MyBatis进行插入数据后获取不到生成的主键，因此，在后面的程序中直接模拟snowflake的ID生成算法在程序中生成了id，再提交插入。

【b】：启动ShardingSphere proxy，然后代开一个mysql客户端，按照下面的方式链接到proxy上
```
mysql -h127.0.0.1 -uroot -proot -P3307
```
【c】：在这个mysql客户端中，可以通过show databases;查看数据库，可以看到sharding_db这个在proxy配置文件中配置的数据库，然后在这个数据库中，使用下面的sql语句
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
  KEY `user_id_idx` (`user_id`),
  KEY `product_id_idx` (`product_id`),
  KEY `created_idx` (`created_time`),
  KEY `paid_idx` (`paid_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```
创建分表，执行成功后，可以在各自库下，看见创建的16张表。

（3）对订单表增删改查操作。

【a】直接访问ShardingSphere Proxy不需要额外引入包，所以POM文件相较之前没有修改，application.properties文件中要做一定修改，内容如下：
```
#connection-pool-setting
spring.datasource.url=jdbc:mysql://localhost:3307/sharding_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
#connection-pool-config
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.maximum-pool-size=30
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

#mybatis
#mybatis.mapper-locations=classpath*:mapper/**/*.xml
mybatis.mapper-locations=classpath*:mapper/*.xml
mybatis.type-aliases-package=traincamp.shardingdb.entity

server.port=8080
```
主要是数据库链接上直接连接到ShardingSphere-proxy的地址上，当作一个MySql数据源看待。

【b】使用Mybatis进行持久化操作，mapper和bean的部分基本没变，dao的接口做了一下修改，先看mapper文件
```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="traincamp.shardingdb.dao.OrderMapper">
  <resultMap id="BaseResultMap" type="traincamp.shardingdb.entity.Order">
    <constructor>
      <idArg column="id" javaType="java.lang.Long" jdbcType="BIGINT" />
      <arg column="user_id" javaType="java.lang.Integer" jdbcType="INTEGER" />
      <arg column="product_id" javaType="java.lang.Integer" jdbcType="INTEGER" />
      <arg column="product_code" javaType="java.lang.String" jdbcType="VARCHAR" />
      <arg column="product_name" javaType="java.lang.String" jdbcType="VARCHAR" />
      <arg column="product_pic" javaType="java.lang.String" jdbcType="VARCHAR" />
      <arg column="procuct_introduction" javaType="java.lang.String" jdbcType="VARCHAR" />
      <arg column="product_amout" javaType="java.lang.Integer" jdbcType="INTEGER" />
      <arg column="product_price" javaType="java.lang.Integer" jdbcType="INTEGER" />
      <arg column="discount" javaType="java.lang.Integer" jdbcType="INTEGER" />
      <arg column="total_price" javaType="java.lang.Integer" jdbcType="INTEGER" />
      <arg column="receiver_address" javaType="java.lang.String" jdbcType="VARCHAR" />
      <arg column="receiver_name" javaType="java.lang.String" jdbcType="VARCHAR" />
      <arg column="receiver_mobile" javaType="java.lang.String" jdbcType="CHAR" />
      <arg column="remark" javaType="java.lang.String" jdbcType="VARCHAR" />
      <arg column="freight" javaType="java.lang.Integer" jdbcType="INTEGER" />
      <arg column="process_state" javaType="java.lang.Integer" jdbcType="INTEGER" />
      <arg column="created_time" javaType="java.util.Date" jdbcType="TIMESTAMP" />
      <arg column="updated_time" javaType="java.util.Date" jdbcType="TIMESTAMP" />
      <arg column="paid_time" javaType="java.util.Date" jdbcType="TIMESTAMP" />
      <arg column="deliveried_time" javaType="java.util.Date" jdbcType="TIMESTAMP" />
    </constructor>
  </resultMap>
  <sql id="Base_Column_List">
    id, user_id, product_id, product_code, product_name, product_pic, procuct_introduction, 
    product_amout, product_price, discount, total_price, receiver_address, receiver_name, 
    receiver_mobile, remark, freight, process_state, created_time, updated_time, paid_time, 
    deliveried_time
  </sql>
  <select id="selectByPrimaryKey" parameterType="java.lang.Long" resultMap="BaseResultMap">
    select 
    <include refid="Base_Column_List" />
    from t_order
    where id = #{id,jdbcType=BIGINT}
  </select>
  <delete id="deleteByPrimaryKey" parameterType="java.lang.Long">
    delete from t_order
    where id = #{id,jdbcType=BIGINT}
  </delete>
  <insert id="insert" parameterType="traincamp.shardingdb.entity.Order">
    insert into t_order (id, user_id, product_id, 
      product_code, product_name, product_pic, 
      procuct_introduction, product_amout, product_price, 
      discount, total_price, receiver_address, 
      receiver_name, receiver_mobile, remark, 
      freight, process_state, created_time, 
      updated_time, paid_time, deliveried_time
      )
    values (#{id,jdbcType=BIGINT}, #{userId,jdbcType=INTEGER}, #{productId,jdbcType=INTEGER}, 
      #{productCode,jdbcType=VARCHAR}, #{productName,jdbcType=VARCHAR}, #{productPic,jdbcType=VARCHAR}, 
      #{procuctIntroduction,jdbcType=VARCHAR}, #{productAmout,jdbcType=INTEGER}, #{productPrice,jdbcType=INTEGER}, 
      #{discount,jdbcType=INTEGER}, #{totalPrice,jdbcType=INTEGER}, #{receiverAddress,jdbcType=VARCHAR}, 
      #{receiverName,jdbcType=VARCHAR}, #{receiverMobile,jdbcType=CHAR}, #{remark,jdbcType=VARCHAR}, 
      #{freight,jdbcType=INTEGER}, #{processState,jdbcType=INTEGER}, #{createdTime,jdbcType=TIMESTAMP}, 
      #{updatedTime,jdbcType=TIMESTAMP}, #{paidTime,jdbcType=TIMESTAMP}, #{deliveriedTime,jdbcType=TIMESTAMP}
      )
  </insert>
  <insert id="insertSelective" parameterType="traincamp.shardingdb.entity.Order" >
    insert into t_order
    <trim prefix="(" suffix=")" suffixOverrides=",">
      <if test="id != null">
        id,
      </if>
      <if test="userId != null">
        user_id,
      </if>
      <if test="productId != null">
        product_id,
      </if>
      <if test="productCode != null">
        product_code,
      </if>
      <if test="productName != null">
        product_name,
      </if>
      <if test="productPic != null">
        product_pic,
      </if>
      <if test="procuctIntroduction != null">
        procuct_introduction,
      </if>
      <if test="productAmout != null">
        product_amout,
      </if>
      <if test="productPrice != null">
        product_price,
      </if>
      <if test="discount != null">
        discount,
      </if>
      <if test="totalPrice != null">
        total_price,
      </if>
      <if test="receiverAddress != null">
        receiver_address,
      </if>
      <if test="receiverName != null">
        receiver_name,
      </if>
      <if test="receiverMobile != null">
        receiver_mobile,
      </if>
      <if test="remark != null">
        remark,
      </if>
      <if test="freight != null">
        freight,
      </if>
      <if test="processState != null">
        process_state,
      </if>
      <if test="createdTime != null">
        created_time,
      </if>
      <if test="updatedTime != null">
        updated_time,
      </if>
      <if test="paidTime != null">
        paid_time,
      </if>
      <if test="deliveriedTime != null">
        deliveried_time,
      </if>
    </trim>
    <trim prefix="values (" suffix=")" suffixOverrides=",">
      <if test="id != null">
        #{id,jdbcType=BIGINT},
      </if>
      <if test="userId != null">
        #{userId,jdbcType=INTEGER},
      </if>
      <if test="productId != null">
        #{productId,jdbcType=INTEGER},
      </if>
      <if test="productCode != null">
        #{productCode,jdbcType=VARCHAR},
      </if>
      <if test="productName != null">
        #{productName,jdbcType=VARCHAR},
      </if>
      <if test="productPic != null">
        #{productPic,jdbcType=VARCHAR},
      </if>
      <if test="procuctIntroduction != null">
        #{procuctIntroduction,jdbcType=VARCHAR},
      </if>
      <if test="productAmout != null">
        #{productAmout,jdbcType=INTEGER},
      </if>
      <if test="productPrice != null">
        #{productPrice,jdbcType=INTEGER},
      </if>
      <if test="discount != null">
        #{discount,jdbcType=INTEGER},
      </if>
      <if test="totalPrice != null">
        #{totalPrice,jdbcType=INTEGER},
      </if>
      <if test="receiverAddress != null">
        #{receiverAddress,jdbcType=VARCHAR},
      </if>
      <if test="receiverName != null">
        #{receiverName,jdbcType=VARCHAR},
      </if>
      <if test="receiverMobile != null">
        #{receiverMobile,jdbcType=CHAR},
      </if>
      <if test="remark != null">
        #{remark,jdbcType=VARCHAR},
      </if>
      <if test="freight != null">
        #{freight,jdbcType=INTEGER},
      </if>
      <if test="processState != null">
        #{processState,jdbcType=INTEGER},
      </if>
      <if test="createdTime != null">
        #{createdTime,jdbcType=TIMESTAMP},
      </if>
      <if test="updatedTime != null">
        #{updatedTime,jdbcType=TIMESTAMP},
      </if>
      <if test="paidTime != null">
        #{paidTime,jdbcType=TIMESTAMP},
      </if>
      <if test="deliveriedTime != null">
        #{deliveriedTime,jdbcType=TIMESTAMP},
      </if>
    </trim>
  </insert>
  <!-- MySQL下批量保存，可以foreach遍历 mysql支持values(),(),()语法 -->
  <insert id="insertOrderBatch" parameterType="java.util.List">
    insert into t_order (id, user_id, product_id,
    product_code, product_name, product_pic,
    procuct_introduction, product_amout, product_price,
    discount, total_price, receiver_address,
    receiver_name, receiver_mobile, remark,
    freight, process_state, created_time,
    updated_time, paid_time, deliveried_time
    )
    values
    <foreach collection="orders" item="order" separator=",">
      (#{order.id}, #{order.userId}, #{order.productId},
      #{order.productCode}, #{order.productName}, #{order.productPic},
      #{order.procuctIntroduction}, #{order.productAmout}, #{order.productPrice},
      #{order.discount}, #{order.totalPrice}, #{order.receiverAddress},
      #{order.receiverName}, #{order.receiverMobile}, #{order.remark},
      #{order.freight}, #{order.processState}, #{order.createdTime},
      #{order.updatedTime}, #{order.paidTime}, #{order.deliveriedTime}
      )
    </foreach>
  </insert>

  <update id="updateByPrimaryKeySelective" parameterType="traincamp.shardingdb.entity.Order">
    update t_order
    <set>
      <if test="userId != null">
        user_id = #{userId,jdbcType=INTEGER},
      </if>
      <if test="productId != null">
        product_id = #{productId,jdbcType=INTEGER},
      </if>
      <if test="productCode != null">
        product_code = #{productCode,jdbcType=VARCHAR},
      </if>
      <if test="productName != null">
        product_name = #{productName,jdbcType=VARCHAR},
      </if>
      <if test="productPic != null">
        product_pic = #{productPic,jdbcType=VARCHAR},
      </if>
      <if test="procuctIntroduction != null">
        procuct_introduction = #{procuctIntroduction,jdbcType=VARCHAR},
      </if>
      <if test="productAmout != null">
        product_amout = #{productAmout,jdbcType=INTEGER},
      </if>
      <if test="productPrice != null">
        product_price = #{productPrice,jdbcType=INTEGER},
      </if>
      <if test="discount != null">
        discount = #{discount,jdbcType=INTEGER},
      </if>
      <if test="totalPrice != null">
        total_price = #{totalPrice,jdbcType=INTEGER},
      </if>
      <if test="receiverAddress != null">
        receiver_address = #{receiverAddress,jdbcType=VARCHAR},
      </if>
      <if test="receiverName != null">
        receiver_name = #{receiverName,jdbcType=VARCHAR},
      </if>
      <if test="receiverMobile != null">
        receiver_mobile = #{receiverMobile,jdbcType=CHAR},
      </if>
      <if test="remark != null">
        remark = #{remark,jdbcType=VARCHAR},
      </if>
      <if test="freight != null">
        freight = #{freight,jdbcType=INTEGER},
      </if>
      <if test="processState != null">
        process_state = #{processState,jdbcType=INTEGER},
      </if>
      <if test="createdTime != null">
        created_time = #{createdTime,jdbcType=TIMESTAMP},
      </if>
      <if test="updatedTime != null">
        updated_time = #{updatedTime,jdbcType=TIMESTAMP},
      </if>
      <if test="paidTime != null">
        paid_time = #{paidTime,jdbcType=TIMESTAMP},
      </if>
      <if test="deliveriedTime != null">
        deliveried_time = #{deliveriedTime,jdbcType=TIMESTAMP},
      </if>
    </set>
    where id = #{id,jdbcType=BIGINT}
  </update>
  <update id="updateByPrimaryKey" parameterType="traincamp.shardingdb.entity.Order">
    update t_order
    set user_id = #{userId,jdbcType=INTEGER},
      product_id = #{productId,jdbcType=INTEGER},
      product_code = #{productCode,jdbcType=VARCHAR},
      product_name = #{productName,jdbcType=VARCHAR},
      product_pic = #{productPic,jdbcType=VARCHAR},
      procuct_introduction = #{procuctIntroduction,jdbcType=VARCHAR},
      product_amout = #{productAmout,jdbcType=INTEGER},
      product_price = #{productPrice,jdbcType=INTEGER},
      discount = #{discount,jdbcType=INTEGER},
      total_price = #{totalPrice,jdbcType=INTEGER},
      receiver_address = #{receiverAddress,jdbcType=VARCHAR},
      receiver_name = #{receiverName,jdbcType=VARCHAR},
      receiver_mobile = #{receiverMobile,jdbcType=CHAR},
      remark = #{remark,jdbcType=VARCHAR},
      freight = #{freight,jdbcType=INTEGER},
      process_state = #{processState,jdbcType=INTEGER},
      created_time = #{createdTime,jdbcType=TIMESTAMP},
      updated_time = #{updatedTime,jdbcType=TIMESTAMP},
      paid_time = #{paidTime,jdbcType=TIMESTAMP},
      deliveried_time = #{deliveriedTime,jdbcType=TIMESTAMP}
    where id = #{id,jdbcType=BIGINT}
  </update>
</mapper>
```
实体bean为：
```
package traincamp.shardingdb.entity;

import java.util.Date;

public class Order {
    private Long id;

    private Integer userId;

    private Integer productId;

    private String productCode;

    private String productName;

    private String productPic;

    private String procuctIntroduction;

    private Integer productAmout;

    private Integer productPrice;

    private Integer discount;

    private Integer totalPrice;

    private String receiverAddress;

    private String receiverName;

    private String receiverMobile;

    private String remark;

    private Integer freight;

    private Integer processState;

    private Date createdTime;

    private Date updatedTime;

    private Date paidTime;

    private Date deliveriedTime;

    public Order(Long id, Integer userId, Integer productId, String productCode, String productName, String productPic, String procuctIntroduction, Integer productAmout, Integer productPrice, Integer discount, Integer totalPrice, String receiverAddress, String receiverName, String receiverMobile, String remark, Integer freight, Integer processState, Date createdTime, Date updatedTime, Date paidTime, Date deliveriedTime) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.productPic = productPic;
        this.procuctIntroduction = procuctIntroduction;
        this.productAmout = productAmout;
        this.productPrice = productPrice;
        this.discount = discount;
        this.totalPrice = totalPrice;
        this.receiverAddress = receiverAddress;
        this.receiverName = receiverName;
        this.receiverMobile = receiverMobile;
        this.remark = remark;
        this.freight = freight;
        this.processState = processState;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
        this.paidTime = paidTime;
        this.deliveriedTime = deliveriedTime;
    }

    public Order() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode == null ? null : productCode.trim();
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName == null ? null : productName.trim();
    }

    public String getProductPic() {
        return productPic;
    }

    public void setProductPic(String productPic) {
        this.productPic = productPic == null ? null : productPic.trim();
    }

    public String getProcuctIntroduction() {
        return procuctIntroduction;
    }

    public void setProcuctIntroduction(String procuctIntroduction) {
        this.procuctIntroduction = procuctIntroduction == null ? null : procuctIntroduction.trim();
    }

    public Integer getProductAmout() {
        return productAmout;
    }

    public void setProductAmout(Integer productAmout) {
        this.productAmout = productAmout;
    }

    public Integer getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Integer productPrice) {
        this.productPrice = productPrice;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Integer totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress == null ? null : receiverAddress.trim();
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName == null ? null : receiverName.trim();
    }

    public String getReceiverMobile() {
        return receiverMobile;
    }

    public void setReceiverMobile(String receiverMobile) {
        this.receiverMobile = receiverMobile == null ? null : receiverMobile.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Integer getFreight() {
        return freight;
    }

    public void setFreight(Integer freight) {
        this.freight = freight;
    }

    public Integer getProcessState() {
        return processState;
    }

    public void setProcessState(Integer processState) {
        this.processState = processState;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Date getPaidTime() {
        return paidTime;
    }

    public void setPaidTime(Date paidTime) {
        this.paidTime = paidTime;
    }

    public Date getDeliveriedTime() {
        return deliveriedTime;
    }

    public void setDeliveriedTime(Date deliveriedTime) {
        this.deliveriedTime = deliveriedTime;
    }
}
```
除了订单的实体，为了程序需要，还添加了几个实体bean，是商品和快递接收信息的bean。如下：
```
package traincamp.shardingdb.entity;

import java.util.Date;

public class Product {
    private Integer id;

    private String productName;

    private String productCode;

    private String productPic;

    private Integer productPrice;

    private Integer productStorage;

    private String productIntroduction;

    private Integer displayOrder;

    private Byte onSaleFlag;

    private Date createdTime;

    private Date updatedTime;

    public Product(Integer id, String productName, String productCode, String productPic, Integer productPrice, Integer productStorage, String productIntroduction, Integer displayOrder, Byte onSaleFlag, Date createdTime, Date updatedTime) {
        this.id = id;
        this.productName = productName;
        this.productCode = productCode;
        this.productPic = productPic;
        this.productPrice = productPrice;
        this.productStorage = productStorage;
        this.productIntroduction = productIntroduction;
        this.displayOrder = displayOrder;
        this.onSaleFlag = onSaleFlag;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    public Product() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName == null ? null : productName.trim();
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode == null ? null : productCode.trim();
    }

    public String getProductPic() {
        return productPic;
    }

    public void setProductPic(String productPic) {
        this.productPic = productPic == null ? null : productPic.trim();
    }

    public Integer getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Integer productPrice) {
        this.productPrice = productPrice;
    }

    public Integer getProductStorage() {
        return productStorage;
    }

    public void setProductStorage(Integer productStorage) {
        this.productStorage = productStorage;
    }

    public String getProductIntroduction() {
        return productIntroduction;
    }

    public void setProductIntroduction(String productIntroduction) {
        this.productIntroduction = productIntroduction == null ? null : productIntroduction.trim();
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Byte getOnSaleFlag() {
        return onSaleFlag;
    }

    public void setOnSaleFlag(Byte onSaleFlag) {
        this.onSaleFlag = onSaleFlag;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }
}
```
```
package traincamp.shardingdb.entity;

import lombok.Data;

@Data
public class ExpressReceiverInfo {
    private String receiverAddress;

    private String receiverName;

    private String receiverMobile;
}
```
订单的DAO类OrderMapper如下：
```
package traincamp.shardingdb.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import traincamp.shardingdb.entity.Order;

import java.util.Date;
import java.util.List;

@Mapper
public interface OrderMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

    int insertOrderBatch(@Param("orders") List<Order> orders);

    @Select("select process_state from t_order where id = #{id} for update")
    Integer getProcessStateByIdForUpdate(@Param("id") Long id);

    @Update("update t_order set process_state = 1, paid_time = #{date}, updated_time = #{date} where id = #{id}")
    int updatePaidProcessState(@Param("id")Long id, @Param("date") Date date);

}
```
增加两个接口方法是为了更好的模拟更新时需要先加行锁，再进行更新，以及更合理的实现新支付完成状态更新（同时更新支付时间）。

【c】：对应的订单类OrderService，代码如下：
```
package traincamp.shardingdb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import traincamp.shardingdb.constant.OrderConstant;
import traincamp.shardingdb.dao.OrderMapper;
import traincamp.shardingdb.entity.ExpressReceiverInfo;
import traincamp.shardingdb.entity.Order;
import traincamp.shardingdb.entity.Product;

import java.util.Date;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private IdGeneratorService idGeneratorService;

    /**
     * 保存订单，这里进行一些简化处理，订单只能购买一种商品，这就不用另外的详情表处理了，而且没有折扣和运费，则总价就是单价乘数量
     * @param userId  用户Id
     * @param product  购买的商品
     * @param amount  购买的数量
     * @param receiverInfo   快递接收人的信息
     * @return
     */
    public Order saveNormalOrder(Integer userId, Product product, int amount, ExpressReceiverInfo receiverInfo) {
        Order order = new Order();
        order.setId(idGeneratorService.getId());
        order.setUserId(userId);
        order.setProductId(product.getId());
        order.setProductCode(product.getProductCode());
        order.setProcuctIntroduction(product.getProductIntroduction());
        order.setProductName(product.getProductName());
        order.setProductPic(product.getProductPic());
        order.setProductPrice(product.getProductPrice());
        order.setProductAmout(amount);
        int totalPrice = amount * product.getProductPrice();
        order.setTotalPrice(totalPrice);
        order.setDiscount(0);
        order.setFreight(0);
        order.setRemark("");
        order.setReceiverName(receiverInfo.getReceiverName());
        order.setReceiverAddress(receiverInfo.getReceiverAddress());
        order.setReceiverMobile(receiverInfo.getReceiverMobile());
        order.setProcessState(OrderConstant.PROCESS_STATE_CREATED);
        Date now = new Date();
        order.setCreatedTime(now);
        order.setUpdatedTime(now);
        orderMapper.insertSelective(order);
        //orderMapper.insert(order);
        return order;
    }

    /**
     * 更新订单状态，为已支付状态，同时更新支付时间。为了进行幂等性操作，使用for update先加行锁，再进行更新。
     * @param orderId
     * @return
     */
    public boolean updatePaidState(Long orderId) {
        Integer state = orderMapper.getProcessStateByIdForUpdate(orderId);
        if(state.equals(OrderConstant.PROCESS_STATE_CREATED)) {
            int ret = orderMapper.updatePaidProcessState(orderId, new Date());
            return (ret > 0);
        }
        return false;
    }

    /**
     * 根据订单Id获取订单信息
     * @param orderId
     * @return
     */
    public Order getOrderById(Long orderId) {
        return orderMapper.selectByPrimaryKey(orderId);
    }

    /**
     * 删除订单。本方法纯粹是为例演示删除的功能的使用，实际应用中订单是不能被删除的，甚至连逻辑删除也没有
     */
    public boolean deleteOrder(Long orderId) {
        int ret = orderMapper.deleteByPrimaryKey(orderId);
        return (ret > 0);
    }
}
```
增删改查操作中，因为insert操作时，如果采用proxy生成的id，在插入后获取不到生产的id，所以后来采用之前自定义的Id生成器。另外，更新操作选择更新支付状态和支付时间这个典型的订单更新操作，而且采用了先select for update再update的方式。实际应用中删除操作在订单操作中基本上是不出现的，这里仅仅是为了演示用的。Id生成器代码如下：
```
package traincamp.shardingdb.service;

import org.springframework.stereotype.Service;

@Service
public class IdGeneratorService {

    //系统上线时间
    private final long startTime = 1601256017000L;
    //机器Id
    private long workId;
    //序列号
    private long serialNum = 0;

    //得到左移位
    private final long serialNumBits = 20L;
    private final long workIdBits = 2L;

    private final long workIdShift = serialNumBits;
    private final long timestampShift = workIdShift + workIdBits;

    private long lastTimeStamp = 0L;

    private long serialNumMax = -1 ^ (-1L << serialNumBits);

    public IdGeneratorService() {
        this(1L);
    }

    public IdGeneratorService(long workId) {
        this.workId = workId;
    }

    public synchronized long getId() {
        long timestamp = System.currentTimeMillis();
        if( timestamp == lastTimeStamp) {
            serialNum = (serialNum + 1) & serialNumMax;
            if (serialNum == 0) {
                timestamp = waitNextMillis(timestamp);
            }
        } else {
            serialNum = timestamp & 1;
        }
        lastTimeStamp = timestamp;
        return ((timestamp - startTime) << timestampShift)
                | (workId << workIdShift)
                | serialNum;
    }

    private long waitNextMillis(long timestamp) {
        long nowTimestamp = System.currentTimeMillis();
        while ( timestamp >= nowTimestamp) {
            nowTimestamp = System.currentTimeMillis();
        }
        return nowTimestamp;
    }
}
```
【d】：controller类OrderController，这个类为了方便进行操作的实践，以下链接对应了增删改查：
操作|访问链接|说明
--|--|--
增加|/order/save?uid=&pid=&amount=|模拟下单接口。uid是用户ID，pid是商品id，amount是购买数量
查询|/order/get?id=|获取指定订单接口。id是订单id，可以获得相关订单信息
更新|/order/pay?id=|模拟支付订单。id是订单id
删除|/order/delete?id=|删除订单，这个接口纯粹是为了演示。id是订单id

OrderController代码如下：
```
package traincamp.shardingdb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import traincamp.shardingdb.entity.ExpressReceiverInfo;
import traincamp.shardingdb.entity.Order;
import traincamp.shardingdb.entity.Product;
import traincamp.shardingdb.service.OrderService;
import traincamp.shardingdb.service.ProductService;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @GetMapping("/save")
    public Order saveOrder(@RequestParam("uid") Integer userId,
                           @RequestParam("pid") Integer productId,
                           @RequestParam("amount") int amount) {
        Product product = productService.getProductById(productId);
        ExpressReceiverInfo receiverInfo = generateReceiverInfo();
        return orderService.saveNormalOrder(userId, product, amount, receiverInfo);
    }

    private ExpressReceiverInfo generateReceiverInfo() {
        ExpressReceiverInfo receiverInfo = new ExpressReceiverInfo();
        receiverInfo.setReceiverName("张三");
        receiverInfo.setReceiverAddress("某市某区某街道123号");
        receiverInfo.setReceiverMobile("13500135000");
        return receiverInfo;
    }

    @GetMapping("/pay")
    public String updatePaidState(@RequestParam("id") Long orderId) {
        boolean success = orderService.updatePaidState(orderId);
        return success ? "支付成功！" : "支付失败！";
    }

    @GetMapping("/get")
    public Order getOrderById(@RequestParam("id") Long orderId) {
        Order order = orderService.getOrderById(orderId);
        if(order != null) {
            return order;
        }
        return new Order();
    }

    @GetMapping("/delete")
    public String deleteById(@RequestParam("id") Long orderId) {
        boolean success = orderService.deleteOrder(orderId);
        return success ? "删除成功！" : "删除失败！";
    }

}
```

（4）执行情况说明：
运行ShardingSphere Proxy，然后运行Springboot应用，可以在浏览器（或者Postman）中访问http://localhost:8080/save?uid=10&pid=101&amount=10, 可以获得一下信息
```
{"id":25825334734618624,"userId":10,"productId":101,"productCode":"6553221233","productName":"商品A","productPic":"http://somesite.com/staic/product.jpg","procuctIntroduction":"一段描述","productAmout":10,"productPrice":15000,"discount":0,"totalPrice":150000,"receiverAddress":"某市某区某街道123号","receiverName":"张三","receiverMobile":"13500135000","remark":"","freight":0,"processState":0,"createdTime":"2020-12-08T07:40:45.000+00:00","updatedTime":"2020-12-08T07:40:45.000+00:00","paidTime":null,"deliveriedTime":null}
```
根据上面信息查看orderdb0中表t_order0中，可以查询该条记录。

访问http://localhost:8080/get?id=25825334734618624，可以获得和上面一样的信息。


访问http://localhost:8080/pay?id=25825334734618624，通过proxy中查看sql，可以看到：
```
[INFO ] 15:44:25.244 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: db0 ::: update t_order0 set process_state = 1, paid_time = '2020-12-08 15:44:25.231', updated_time = '2020-12-08 15:44:25.231' where id = 25825334734618624
[INFO ] 15:44:25.244 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: db1 ::: update t_order0 set process_state = 1, paid_time = '2020-12-08 15:44:25.231', updated_time = '2020-12-08 15:44:25.231' where id = 25825334734618624
```
可以看到，是同是像两个库中同时发送update的sql。这时再次通过上面的接口可以获得订单的信息如下：
```
{"id":25825334734618624,"userId":10,"productId":101,"productCode":"6553221233","productName":"商品A","productPic":"http://somesite.com/staic/product.jpg","procuctIntroduction":"一段描述","productAmout":10,"productPrice":15000,"discount":0,"totalPrice":150000,"receiverAddress":"某市某区某街道123号","receiverName":"张三","receiverMobile":"13500135000","remark":"","freight":0,"processState":1,"createdTime":"2020-12-08T07:44:25.000+00:00","updatedTime":"2020-12-08T07:44:25.000+00:00","paidTime":"2020-12-08T07:44:25.000+00:00","deliveriedTime":null}
```
此时可以查看proxy中sql的情况如下：
```
[INFO ] 15:46:02.263 [ShardingSphere-Command-3] ShardingSphere-SQL - Logic SQL: select

    id, user_id, product_id, product_code, product_name, product_pic, procuct_introduction,
    product_amout, product_price, discount, total_price, receiver_address, receiver_name,
    receiver_mobile, remark, freight, process_state, created_time, updated_time, paid_time,
    deliveried_time

    from t_order
    where id = 25825334734618624
[INFO ] 15:46:02.264 [ShardingSphere-Command-3] ShardingSphere-SQL - SQLStatement: MySQLSelectStatement(limit=Optional.empty, lock=Optional.empty)
[INFO ] 15:46:02.269 [ShardingSphere-Command-3] ShardingSphere-SQL - Actual SQL: db0 ::: select

    id, user_id, product_id, product_code, product_name, product_pic, procuct_introduction,
    product_amout, product_price, discount, total_price, receiver_address, receiver_name,
    receiver_mobile, remark, freight, process_state, created_time, updated_time, paid_time,
    deliveried_time

    from t_order0
    where id = 25825334734618624
[INFO ] 15:46:02.270 [ShardingSphere-Command-3] ShardingSphere-SQL - Actual SQL: db1 ::: select

    id, user_id, product_id, product_code, product_name, product_pic, procuct_introduction,
    product_amout, product_price, discount, total_price, receiver_address, receiver_name,
    receiver_mobile, remark, freight, process_state, created_time, updated_time, paid_time,
    deliveried_time

    from t_order0
    where id = 25825334734618624
```

最后，访问http://localhost:8080/delete?id=25825334734618624，执行后，在查看数据库orderdb0中表t_order0中，该记录已删除。


（5） 两个数据库的导出的sql放在/sharding/sql目录下

ShardingSphere proxy的配置文件放在/sharding/proxy_conf目录下

相关代码工程放在/sharding/shardingdb目录下