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
