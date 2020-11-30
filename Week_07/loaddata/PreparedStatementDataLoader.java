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
