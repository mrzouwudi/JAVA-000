package jdbc;

import lombok.AllArgsConstructor;
import lombok.Setter;

import java.sql.*;
import java.util.List;

/**
 * 使用事务，PrepareStatement 方式，批处理方式
 */
@Setter
@AllArgsConstructor
public class PrepareStatementJdbcUserDao {
    private String jdbcUrl;
    private String username;
    private String password;
    static {
        try {
            //注册Driver操作对象
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加一个用户, 使用PrepareStatement和显式的事务
     * @param user
     * @return
     */
    public boolean insertUser(final User user) {
        if(null == user) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            conn.setAutoCommit(false);
            statement = conn.prepareStatement("INSERT INTO user (user_name) VALUES (?)");
            statement.setString(1, user.getUserName());
            int ret = statement.executeUpdate();
            conn.commit();
            return (ret > 0);
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            rollback(conn);
            return false;
        } finally {
            closePreparedStatement(statement);
            closeConn(conn);
        }
    }

    /**
     * 删除指定用户, 使用PrepareStatement和显式的事务
     * @param id
     * @return
     */
    public boolean deleteUserById(final Integer id) {
        if (null == id) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            conn.setAutoCommit(false);
            statement = conn.prepareStatement("DELETE FROM user WHERE id = ？");
            statement.setInt(1, id);
            int ret = statement.executeUpdate();
            conn.commit();
            return (ret > 0);
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            rollback(conn);
            return false;
        } finally {
            closePreparedStatement(statement);
            closeConn(conn);
        }
    }

    /**
     * 更改用户名字, 使用PrepareStatement和显式的事务
     * @param user
     * @return
     */
    public boolean updateUserName(final User user) {
        if (null == user || null == user.getId()) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            conn.setAutoCommit(false);
            statement = conn.prepareStatement("UPDATE user SET user_name = ? WHERE id = ?");
            statement.setString(1, user.getUserName());
            statement.setInt(2, user.getId());
            int ret = statement.executeUpdate();
            conn.commit();
            return (ret > 0);
        } catch (SQLException e) {
            e.printStackTrace();
            rollback(conn);
            return false;
        } finally {
            closePreparedStatement(statement);
            closeConn(conn);
        }
    }

    /**
     * 获取指定用户信息，使用PrepareStatement，查询不用事务
     * @param id
     * @return
     */
    public User getUserById(final Integer id) {
        if(null == id) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            statement = conn.prepareStatement("SELECT id,user_name FROM user WHERE id = ?");
            statement.setInt(1, id);
            resultSet = statement.executeQuery();
            if(resultSet.next()) {
                String userName = resultSet.getString(2);
                User user = new User(id, userName);
                return user;
            }
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            return null;
        } finally {
            closeResultSet(resultSet);
            closePreparedStatement(statement);
            closeConn(conn);
        }
        return null;
    }

    /**
     * 使用事务，PrepareStatement 方式，批处理方式的方式进行插入
     * @param users
     * @return
     */
    public boolean insertUsers(List<User> users) {
        if(null == users) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            conn.setAutoCommit(false);
            statement = conn.prepareStatement("INSERT INTO user (user_name) VALUES (?)");
            for(User user : users) {
                statement.setString(1, user.getUserName());
                statement.addBatch();
            }
            statement.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            rollback(conn);
            return false;
        } finally {
            closePreparedStatement(statement);
            closeConn(conn);
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
