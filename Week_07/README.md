# 作业说明
## Week07 作业题目（周六）：
2.（必做）按自己设计的表结构，插入 100 万订单模拟数据，测试不同方式的插入效率

首先是，我设计的订单表结构如下：
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
```
为了加速导入，在导入前只保留一个主键索引，其他索引都删除了。

采用了三种方式进行导入：第一种是使用PreparedStatement的批量执行（addBatch+executeBatch）；第二种是一条插入语句中values后携带多条记录；第三种是，通过调用mysql的LOAD DATA命令进行导入。需要说明的一下第一种方式，采用了并行流，第二种方式使用了线程池。

为了更好的进行导入的时间比较，三种方式都是采用先将1000000订单数据模拟处理，然后一次性导入。下面记录的时间只是导入过程的时间，不包含生成模拟数据的时间。而且三种方式都使用了Hikari连接池且配置一致。
下面是三种方式的具体实现说明和消耗的时间。

（1）使用PreparedStatement的批量执行（addBatch+executeBatch）

先看PreparedStatement的导入部分，下面是源码
```
package traincamp.dbexpone.jdbc;

import lombok.AllArgsConstructor;
import traincamp.dbexpone.entity.Order;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@AllArgsConstructor
public class PrepareStatementDatasourceOrderDao {

    private DataSource dataSource;

    private final static String INSERT_SQL = "INSERT INTO t_order (id, user_id, product_id,\n" +
            "    product_code, product_name, product_pic,\n" +
            "    procuct_introduction, product_amout, product_price,\n" +
            "    discount, total_price, receiver_address,\n" +
            "    receiver_name, receiver_mobile, remark,\n" +
            "    freight, process_state, created_time,\n" +
            "    updated_time, paid_time, deliveried_time\n" +
            "    ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public int insertOrder(Order order) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            int result = insertOrder(order, connection);
            connection.commit();
            return result;
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            rollback(connection);
            return 0;
        } finally {
            closeConn(connection);
        }
    }

    public int insertOrder(Order order, Connection connection) throws SQLException{
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(INSERT_SQL);
            fillOrderInfoIntoPreparedStatement(order, statement);

            int ret = statement.executeUpdate();
            return ret;
        } finally {
            closePreparedStatement(statement);
        }
    }

    private void fillOrderInfoIntoPreparedStatement(Order order, PreparedStatement statement) throws SQLException {
        statement.setLong(1, order.getId());
        statement.setInt(2,order.getUserId());
        statement.setInt(3, order.getProductId());
        statement.setString(4,order.getProductCode());
        statement.setString(5,order.getProductName());
        statement.setString(6,order.getProductPic());
        statement.setString(7,order.getProcuctIntroduction());
        statement.setInt(8, order.getProductAmout());
        statement.setInt(9,order.getProductPrice());
        statement.setInt(10, order.getDiscount());
        statement.setInt(11,order.getTotalPrice());
        statement.setString(12,order.getReceiverAddress());
        statement.setString(13,order.getReceiverName());
        statement.setString(14,order.getReceiverMobile());
        statement.setString(15, order.getRemark());
        statement.setInt(16,order.getFreight());
        statement.setInt(17, order.getProcessState());
        statement.setDate(18,new java.sql.Date(order.getCreatedTime().getTime()));
        java.sql.Date updateTime = order.getUpdatedTime() == null ? null: new java.sql.Date(order.getUpdatedTime().getTime());
        statement.setDate(19,updateTime);
        java.sql.Date paidTime = order.getPaidTime() == null ? null: new java.sql.Date(order.getPaidTime().getTime());
        statement.setDate(20,paidTime);
        java.sql.Date deliveriedTime = order.getDeliveriedTime() == null ? null: new java.sql.Date(order.getDeliveriedTime().getTime());
        statement.setDate(21,deliveriedTime);
    }

    public void insertBatchOrders(List<Order> orders) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            insertBatchOrders(orders, connection);
            connection.commit();
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            rollback(connection);
        } finally {
            closeConn(connection);
        }
    }

    public void insertBatchOrders(List<Order> orders, Connection connection) throws SQLException{
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(INSERT_SQL);
            for(Order order : orders) {
                fillOrderInfoIntoPreparedStatement(order, statement);
                statement.addBatch();
            }
            statement.executeBatch();
        } finally {
            closePreparedStatement(statement);
        }
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    private void closeConn(final Connection conn) {
        if(conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void closePreparedStatement(final PreparedStatement statement) {
        if(statement != null) {
            try{
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeResultSet(final ResultSet resultSet) {
        if(resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
```
其中insertBatchOrders是将一批更新的数据，批量方式加入到PreparedStatement中，并且批量执行。

下面是具体直接执行的入口程序
```
package traincamp.dbexpone.jdbc;

import traincamp.dbexpone.entity.Order;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class DirectJdbcImport {
    public static void main(String[] args) {
        DataSourceConfig config = new DataSourceConfig();
        config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/tinymall?serverTimezone=UTC&characterEncoding=utf-8");
        config.setUsername("root");
        config.setPassword("root");
        config.setMinimumIdle(10);
        config.setMaximumPoolSize(30);
        config.setConnectionTimeout(3000);
        DataSource dataSource = DataSourceFactory.getDataSource(DataSourceFactory.HIKARI_DATASOURCE, config);
        if(dataSource == null) {
            throw new IllegalArgumentException();
        }
        PrepareStatementDatasourceOrderDao orderDao = new PrepareStatementDatasourceOrderDao(dataSource);

        OrderGenerator orderGenerator = new OrderGenerator();
        List<List<Order>> orders = new ArrayList<>(200);
        for (int i=0; i<200; i++){
            orders.add(orderGenerator.generateManyOrders(5000));
        }
        Long start = System.currentTimeMillis();
        orders.stream().parallel().forEach(orderDao::insertBatchOrders);
        Long end = System.currentTimeMillis();
        System.out.println("耗时："+(end - start)+"ms");
    }
}
```
其中DataSourceFactory，可以获取hikari的连接池；OrderGenerator是模拟订单生产器。在向数据库插入时，是一次批量插入5000条，分成200次调用，使用过程中采用了流的并行方式进行。导入过程耗时为45108ms。

下面是其余相关代码
```
package traincamp.dbexpone.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceFactory {
    public final static String HIKARI_DATASOURCE = "hikari";

    private DataSourceFactory(){}

    public static DataSource getDataSource(String dataSourceType, DataSourceConfig dataSourceConfig) {
        switch (dataSourceType) {
            case HIKARI_DATASOURCE :
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(dataSourceConfig.getJdbcUrl());
                config.setUsername(dataSourceConfig.getUsername());
                config.setPassword(dataSourceConfig.getPassword());
                config.setMinimumIdle(dataSourceConfig.getMinimumIdle());
                config.setMaximumPoolSize(dataSourceConfig.getMaximumPoolSize());
                config.setConnectionTimeout(dataSourceConfig.getConnectionTimeout());
                config.setAutoCommit(false);
                return new HikariDataSource(config);
            default:
                return null;

        }
    }
}
```
```
package traincamp.dbexpone.jdbc;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataSourceConfig {
    private String jdbcUrl;
    private String username;
    private String password;
    private int minimumIdle;
    private int maximumPoolSize;
    private int connectionTimeout;
}
```
```
package traincamp.dbexpone.jdbc;

import traincamp.dbexpone.entity.Order;
import traincamp.dbexpone.service.SnowFlakeIdService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class OrderGenerator {

    private SnowFlakeIdService idGenerator = new SnowFlakeIdService();


    private Random random = new Random();

    public List<Order> generateManyOrders(int count){
        List<Order> orders = new ArrayList<>(count);
        for(int i= 0; i < count; i++) {
            orders.add(generateOneOrder());
        }
        return orders;
    }

    public Order generateOneOrder() {
        Order order = new Order();
        order.setId(idGenerator.getId());
        order.setUserId(getRandomId());
        order.setProductId(getRandomId());
        order.setProductName("one product");
        order.setProductCode("1000fff");
        order.setProductPic("http://");
        order.setProcuctIntroduction("some words");
        order.setProductAmout(10);
        order.setProductPrice(300);
        order.setDiscount(0);
        order.setFreight(0);
        int totalPrice = order.getProductAmout() * order.getProductPrice() - order.getDiscount() + order.getFreight();
        order.setTotalPrice(totalPrice);
        order.setReceiverAddress("RPC somewhere");
        order.setReceiverName("someone");
        order.setReceiverMobile("13000013000");
        order.setRemark("");
        order.setProcessState(0);
        order.setCreatedTime(new Date());
        return order;
    }

    private int getRandomId() {
        return random.nextInt(100000);
    }

}
```
```
package traincamp.dbexpone.entity;

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
以上代码放在

（2）使用一条插入语句中values后携带多条记录

采用了Mybatis进行数据库访问，在mapper的xml文件中添加一个方法，values后面使用foreach可以添加多个记录。mapper的xml文件如下：
```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="traincamp.dbexpone.dao.OrderMapper" >
  <resultMap id="BaseResultMap" type="traincamp.dbexpone.entity.Order" >
    <constructor >
      <idArg column="id" jdbcType="BIGINT" javaType="java.lang.Long" />
      <arg column="user_id" jdbcType="INTEGER" javaType="java.lang.Integer" />
      <arg column="product_id" jdbcType="INTEGER" javaType="java.lang.Integer" />
      <arg column="product_code" jdbcType="VARCHAR" javaType="java.lang.String" />
      <arg column="product_name" jdbcType="VARCHAR" javaType="java.lang.String" />
      <arg column="product_pic" jdbcType="VARCHAR" javaType="java.lang.String" />
      <arg column="procuct_introduction" jdbcType="VARCHAR" javaType="java.lang.String" />
      <arg column="product_amout" jdbcType="INTEGER" javaType="java.lang.Integer" />
      <arg column="product_price" jdbcType="INTEGER" javaType="java.lang.Integer" />
      <arg column="discount" jdbcType="INTEGER" javaType="java.lang.Integer" />
      <arg column="total_price" jdbcType="INTEGER" javaType="java.lang.Integer" />
      <arg column="receiver_address" jdbcType="VARCHAR" javaType="java.lang.String" />
      <arg column="receiver_name" jdbcType="VARCHAR" javaType="java.lang.String" />
      <arg column="receiver_mobile" jdbcType="CHAR" javaType="java.lang.String" />
      <arg column="remark" jdbcType="VARCHAR" javaType="java.lang.String" />
      <arg column="freight" jdbcType="INTEGER" javaType="java.lang.Integer" />
      <arg column="process_state" jdbcType="INTEGER" javaType="java.lang.Integer" />
      <arg column="created_time" jdbcType="TIMESTAMP" javaType="java.util.Date" />
      <arg column="updated_time" jdbcType="TIMESTAMP" javaType="java.util.Date" />
      <arg column="paid_time" jdbcType="TIMESTAMP" javaType="java.util.Date" />
      <arg column="deliveried_time" jdbcType="TIMESTAMP" javaType="java.util.Date" />
    </constructor>
  </resultMap>
  <sql id="Base_Column_List" >
    id, user_id, product_id, product_code, product_name, product_pic, procuct_introduction, 
    product_amout, product_price, discount, total_price, receiver_address, receiver_name, 
    receiver_mobile, remark, freight, process_state, created_time, updated_time, paid_time, 
    deliveried_time
  </sql>
  <select id="selectByPrimaryKey" resultMap="BaseResultMap" parameterType="java.lang.Long" >
    select 
    <include refid="Base_Column_List" />
    from t_order
    where id = #{id,jdbcType=BIGINT}
  </select>
  <delete id="deleteByPrimaryKey" parameterType="java.lang.Long" >
    delete from t_order
    where id = #{id,jdbcType=BIGINT}
  </delete>
  <insert id="insert" parameterType="traincamp.dbexpone.entity.Order" >
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
  <insert id="insertSelective" parameterType="traincamp.dbexpone.entity.Order" >
    insert into t_order
    <trim prefix="(" suffix=")" suffixOverrides="," >
      <if test="id != null" >
        id,
      </if>
      <if test="userId != null" >
        user_id,
      </if>
      <if test="productId != null" >
        product_id,
      </if>
      <if test="productCode != null" >
        product_code,
      </if>
      <if test="productName != null" >
        product_name,
      </if>
      <if test="productPic != null" >
        product_pic,
      </if>
      <if test="procuctIntroduction != null" >
        procuct_introduction,
      </if>
      <if test="productAmout != null" >
        product_amout,
      </if>
      <if test="productPrice != null" >
        product_price,
      </if>
      <if test="discount != null" >
        discount,
      </if>
      <if test="totalPrice != null" >
        total_price,
      </if>
      <if test="receiverAddress != null" >
        receiver_address,
      </if>
      <if test="receiverName != null" >
        receiver_name,
      </if>
      <if test="receiverMobile != null" >
        receiver_mobile,
      </if>
      <if test="remark != null" >
        remark,
      </if>
      <if test="freight != null" >
        freight,
      </if>
      <if test="processState != null" >
        process_state,
      </if>
      <if test="createdTime != null" >
        created_time,
      </if>
      <if test="updatedTime != null" >
        updated_time,
      </if>
      <if test="paidTime != null" >
        paid_time,
      </if>
      <if test="deliveriedTime != null" >
        deliveried_time,
      </if>
    </trim>
    <trim prefix="values (" suffix=")" suffixOverrides="," >
      <if test="id != null" >
        #{id,jdbcType=BIGINT},
      </if>
      <if test="userId != null" >
        #{userId,jdbcType=INTEGER},
      </if>
      <if test="productId != null" >
        #{productId,jdbcType=INTEGER},
      </if>
      <if test="productCode != null" >
        #{productCode,jdbcType=VARCHAR},
      </if>
      <if test="productName != null" >
        #{productName,jdbcType=VARCHAR},
      </if>
      <if test="productPic != null" >
        #{productPic,jdbcType=VARCHAR},
      </if>
      <if test="procuctIntroduction != null" >
        #{procuctIntroduction,jdbcType=VARCHAR},
      </if>
      <if test="productAmout != null" >
        #{productAmout,jdbcType=INTEGER},
      </if>
      <if test="productPrice != null" >
        #{productPrice,jdbcType=INTEGER},
      </if>
      <if test="discount != null" >
        #{discount,jdbcType=INTEGER},
      </if>
      <if test="totalPrice != null" >
        #{totalPrice,jdbcType=INTEGER},
      </if>
      <if test="receiverAddress != null" >
        #{receiverAddress,jdbcType=VARCHAR},
      </if>
      <if test="receiverName != null" >
        #{receiverName,jdbcType=VARCHAR},
      </if>
      <if test="receiverMobile != null" >
        #{receiverMobile,jdbcType=CHAR},
      </if>
      <if test="remark != null" >
        #{remark,jdbcType=VARCHAR},
      </if>
      <if test="freight != null" >
        #{freight,jdbcType=INTEGER},
      </if>
      <if test="processState != null" >
        #{processState,jdbcType=INTEGER},
      </if>
      <if test="createdTime != null" >
        #{createdTime,jdbcType=TIMESTAMP},
      </if>
      <if test="updatedTime != null" >
        #{updatedTime,jdbcType=TIMESTAMP},
      </if>
      <if test="paidTime != null" >
        #{paidTime,jdbcType=TIMESTAMP},
      </if>
      <if test="deliveriedTime != null" >
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

  <update id="updateByPrimaryKeySelective" parameterType="traincamp.dbexpone.entity.Order" >
    update t_order
    <set >
      <if test="userId != null" >
        user_id = #{userId,jdbcType=INTEGER},
      </if>
      <if test="productId != null" >
        product_id = #{productId,jdbcType=INTEGER},
      </if>
      <if test="productCode != null" >
        product_code = #{productCode,jdbcType=VARCHAR},
      </if>
      <if test="productName != null" >
        product_name = #{productName,jdbcType=VARCHAR},
      </if>
      <if test="productPic != null" >
        product_pic = #{productPic,jdbcType=VARCHAR},
      </if>
      <if test="procuctIntroduction != null" >
        procuct_introduction = #{procuctIntroduction,jdbcType=VARCHAR},
      </if>
      <if test="productAmout != null" >
        product_amout = #{productAmout,jdbcType=INTEGER},
      </if>
      <if test="productPrice != null" >
        product_price = #{productPrice,jdbcType=INTEGER},
      </if>
      <if test="discount != null" >
        discount = #{discount,jdbcType=INTEGER},
      </if>
      <if test="totalPrice != null" >
        total_price = #{totalPrice,jdbcType=INTEGER},
      </if>
      <if test="receiverAddress != null" >
        receiver_address = #{receiverAddress,jdbcType=VARCHAR},
      </if>
      <if test="receiverName != null" >
        receiver_name = #{receiverName,jdbcType=VARCHAR},
      </if>
      <if test="receiverMobile != null" >
        receiver_mobile = #{receiverMobile,jdbcType=CHAR},
      </if>
      <if test="remark != null" >
        remark = #{remark,jdbcType=VARCHAR},
      </if>
      <if test="freight != null" >
        freight = #{freight,jdbcType=INTEGER},
      </if>
      <if test="processState != null" >
        process_state = #{processState,jdbcType=INTEGER},
      </if>
      <if test="createdTime != null" >
        created_time = #{createdTime,jdbcType=TIMESTAMP},
      </if>
      <if test="updatedTime != null" >
        updated_time = #{updatedTime,jdbcType=TIMESTAMP},
      </if>
      <if test="paidTime != null" >
        paid_time = #{paidTime,jdbcType=TIMESTAMP},
      </if>
      <if test="deliveriedTime != null" >
        deliveried_time = #{deliveriedTime,jdbcType=TIMESTAMP},
      </if>
    </set>
    where id = #{id,jdbcType=BIGINT}
  </update>
  <update id="updateByPrimaryKey" parameterType="traincamp.dbexpone.entity.Order" >
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
可以看下面这段
```
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
```
在dao的文件中添加响应的接口函数。注，为了简化，直接在生成的代码和xml文件中添加的。实际开发应该使用继承的方式区分生产代码和定制代码。
下面dao文件
```
package traincamp.dbexpone.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import traincamp.dbexpone.entity.Order;

import java.util.List;

@Mapper
public interface OrderMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Order record);

    int insertSelective(Order record);

    int insertOrderBatch(@Param("orders") List<Order> orders);

    Order selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);
}
```
其中int insertOrderBatch(@Param("orders") List<Order> orders)是新添加的接口方法。

下面是相关的service类。
```
package traincamp.dbexpone.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import traincamp.dbexpone.dao.OrderMapper;
import traincamp.dbexpone.entity.Order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class OrderService {

    @Autowired
    private SnowFlakeIdService idGenerator;

    @Autowired
    private OrderMapper orderMapper;

    private Random random = new Random();

    public void insertBatchOrders() {
        List<List<Order>> orders = new ArrayList<>(200);
        for (int i=0; i<200; i++){
            orders.add(generateManyOrders(5000));
        }
        ExecutorService executorService = Executors.newFixedThreadPool(30);
        Long start = System.currentTimeMillis();
        //orders.stream().parallel().forEach(orderMapper::insertOrderBatch);
        CountDownLatch countDownLatch = new CountDownLatch(200);
        for(List<Order> orderList : orders) {
            executorService.submit(()->{
                orderMapper.insertOrderBatch(orderList);
                try{
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
                //System.out.println(orderList.get(0).getId());
            });
        }
        executorService.shutdown();
        try {
            countDownLatch.await();
        }catch (Exception e) {
            e.printStackTrace();
        }
        Long end = System.currentTimeMillis();
        System.out.println("耗时："+(end - start)+"ms");
    }

    private List<Order> generateManyOrders(int count){
        List<Order> orders = new ArrayList<>(count);
        for(int i= 0; i < count; i++) {
            orders.add(generateOneOrder());
        }
        return orders;
    }

    public void insertOneOrder() {
        Order order = generateOneOrder();
        orderMapper.insert(order);
    }

    private Order generateOneOrder() {
        Order order = new Order();
        order.setId(idGenerator.getId());
        order.setUserId(getRandomId());
        order.setProductId(getRandomId());
        order.setProductName("one product");
        order.setProductCode("1000fff");
        order.setProductPic("http://");
        order.setProcuctIntroduction("some words");
        order.setProductAmout(10);
        order.setProductPrice(300);
        order.setDiscount(0);
        order.setTotalPrice(3000);
        order.setReceiverAddress("beijing somewhere");
        order.setReceiverName("someone");
        order.setReceiverMobile("13000013000");
        order.setRemark("");
        order.setFreight(0);
        order.setProcessState(0);
        order.setCreatedTime(new Date());
        return order;
    }

    private int getRandomId() {
        return random.nextInt(100000);
    }

    private class Task implements Runnable {
        private List<Order> orders;
        private OrderMapper orderMapper;
        public Task(final List<Order> orders,final OrderMapper orderMapper) {
            this.orders = orders;
            this.orderMapper = orderMapper;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            }catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(orders.get(0).getId());
            //orderMapper.insertOrderBatch(orders);
        }
    }
}
```
开始也使用的是流的并行，后来又尝试使用线程池，感觉差异不大。使用线程池最后导入时耗时64177ms。
其中SnowFlakeIdService是参考雪花算法的方式一个简化版本，代码如下：
```
package traincamp.dbexpone.service;

import org.springframework.stereotype.Service;

@Service
public class SnowFlakeIdService {

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

    public SnowFlakeIdService() {
        this(1L);
    }

    public SnowFlakeIdService(long workId) {
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
这个部分是第六题选做题的部分，将在那里详述。
数据库连接池的配置如下：
```
#connection-pool-setting
spring.datasource.url=jdbc:mysql://localhost:3306/tinymall?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
#connection-pool-config
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.maximum-pool-size=30
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
#mybatis
#mybatis.mapper-locations=classpath*:mapper/**/*.xml
mybatis.mapper-locations=classpath*:mapper/*.xml
mybatis.type-aliases-package=dbexpone.entity
```
以上相关代码放在values目录下

（3）是用LOAD DATA方式导入
这个部分是参考网上文章摸索实现的，发现如果MySQL连接库版本有相关性，只有在5.1.*版本可以使用以下的代码进行，如果是8.0以上的原代码的有些类没有。因此代码的可适用性有待后续检查。
下面是使用PreparedStatement调用LOAD DATA，进行数据导入的源码
```
package loaddata;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementDataLoader {
    private DataSource dataSource;

    private final String loadDataSql = "LOAD DATA LOCAL INFILE 'sql.csv' IGNORE INTO TABLE tinymall.t_order (id, user_id, product_id,\n" +
            "    product_code, product_name, product_pic," +
            "    procuct_introduction, product_amout, product_price," +
            "    discount, total_price, receiver_address," +
            "    receiver_name, receiver_mobile, remark," +
            "    freight, process_state, created_time," +
            "    updated_time, paid_time, deliveried_time" +
            "    )";

    public PreparedStatementDataLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int loadData( InputStream dataStream) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(loadDataSql);
            int result = 0;

            if (statement.isWrapperFor(com.mysql.jdbc.Statement.class)) {
                com.mysql.jdbc.PreparedStatement mysqlStatement = statement.unwrap(com.mysql.jdbc.PreparedStatement.class);
                mysqlStatement.setLocalInfileInputStream(dataStream);
                result = mysqlStatement.executeUpdate();
            }
            connection.commit();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            closePreparedStatement(statement);
            closeConn(connection);
        }
    }

    private void closeConn(final Connection conn) {
        if(conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void closePreparedStatement(final PreparedStatement statement) {
        if(statement != null) {
            try{
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
```
上面可以看到，我是先生成1000000条记录，然后生成一个字节流，然后搭配PreparedStatement调用LOAD DATA，这样可以不用实际导入到一个文件中，在调用LOAD DATA。

下面是调用方法的入口程序的源码
```
package loaddata;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class LoadDataMain {
    public static void main(String[] args) throws Exception{
        InputStream is = generateOrdersIntoInputStream();

        PreparedStatementDataLoader dataLoader = buildPreparedStatementDataLoader();
        try {
            Long start = System.currentTimeMillis();
            int ret = dataLoader.loadData(is);
            Long end = System.currentTimeMillis();
            System.out.println("耗时："+(end - start)+"ms");
            System.out.println(ret);
        } finally {
            is.close();
        }
    }

    private static InputStream generateOrdersIntoInputStream() {
        OrderGenerator orderGenerator = new OrderGenerator();
        List<Order> orders = orderGenerator.generateManyOrders(1_000_000);
        StringBuilder stringBuilder = new StringBuilder(100_000_000);
        orders.stream().forEach((order -> OrderData2StringUtil.fillOrderDataIntoStringBuilder(order, stringBuilder)));
        byte[] bytes = stringBuilder.toString().getBytes();
        return new ByteArrayInputStream(bytes);
    }

    private static PreparedStatementDataLoader buildPreparedStatementDataLoader() {
        DataSourceConfig config = new DataSourceConfig();
        config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/tinymall?serverTimezone=UTC&characterEncoding=utf-8");
        config.setUsername("root");
        config.setPassword("root");
        config.setMinimumIdle(10);
        config.setMaximumPoolSize(30);
        config.setConnectionTimeout(3000);
        DataSource dataSource = DataSourceFactory.getDataSource(DataSourceFactory.HIKARI_DATASOURCE, config);
        if(dataSource == null) {
            throw new IllegalArgumentException();
        }
        return new PreparedStatementDataLoader(dataSource);
    }
}
```
通过LOAD DATA方式导入数据耗时22681ms。
以上部分的源码放在loaddata目录下。

总结：
三种方式导入的结果如下：
采用方案|运行时间
--|--
使用PreparedStatement的批量执行+流的并行操作|45108ms
一条插入语句中values后携带多条记录+线程池多线程提交|64177ms
LOAD DATA方式|22681ms

可以看出，直接调用MySQL的数据导入方式最快，批量操作方式次之，而一次性插入携带多个插入值最慢。

6.（选做）尝试自己做一个 ID 生成器（可以模拟 Seq 或 Snowflake）

```
package traincamp.dbexpone.service;

import org.springframework.stereotype.Service;

@Service
public class SnowFlakeIdService {

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

    public SnowFlakeIdService() {
        this(1L);
    }

    public SnowFlakeIdService(long workId) {
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
这个生成方法是按照网上一些介绍snowflake的资料中的代码稍作修改而成。具体来说有以下几点：
（1）生产的ID占64个bit，最高是0，接下来41位是当前时间戳和某个过去时点的时间戳的差值（我这里用的当时写代码的时间），然后若干bit为机器ID（这里是用2个bit，这个值可以修改），然后剩下的bit位为序列号所占的位数。这些bit加起来是64位，正好对应一个Long型整数。
（2）在构造函数中制定机器ID
（3）getId方法是同步方法，首先查看当前的时间戳和上次更新的时间戳是否相同，如果相同则将序列号加一，如果序列号已经全部用完，即serialNum变回0，则可以自旋一下，到下一个毫秒。如果当前的时间戳和上次更新的时间戳不同时，并不将serialNum设为0，而是timestamp & 1，这样是为了让serialNum的值更随机一下。
