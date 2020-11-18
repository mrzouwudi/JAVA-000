package jdbc;

import lombok.AllArgsConstructor;
import lombok.Setter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Setter
@AllArgsConstructor
public class StatementJdbcUserDao {
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
     * 增加一个用户
     * @param user
     * @return
     */
    public boolean insertUser(final User user) {
        if(null == user) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        Statement statement = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            statement = conn.createStatement();
            //INSERT INTO user (name) VALUES (?)
            int ret = statement.executeUpdate("INSERT INTO user ( user_name) VALUES (" + user.getUserName() + ")");
            return (ret > 0);
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            return false;
        } finally {
            closeStatement(statement);
            closeConn(conn);
        }
    }

    /**
     * 删除指定用户
     * @param id
     * @return
     */
    public boolean deleteUserById(final Integer id) {
        if (null == id) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        Statement statement = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            statement = conn.createStatement();
            //DELETE FROM user WHERE id = ？
            int ret = statement.executeUpdate("DELETE FROM user WHERE id = " + id);
            return (ret > 0);
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            return false;
        } finally {
            closeStatement(statement);
            closeConn(conn);
        }
    }

    /**
     * 更改用户名字
     * @param user
     * @return
     */
    public boolean updateUserName(final User user) {
        if (null == user || null == user.getId()) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        Statement statement = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            statement = conn.createStatement();
            //UPDATE user SET name = ? WHERE id = ?
            int ret = statement.executeUpdate("UPDATE user SET name = "+user.getUserName()+" WHERE id = " + user.getId());
            return (ret > 0);
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            return false;
        } finally {
            closeStatement(statement);
            closeConn(conn);
        }
    }

    /**
     * 获取指定用户信息
     * @param id
     * @return
     */
    public User getUserById(final Integer id) {
        if(null == id) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            statement = conn.createStatement();
            //SELECT id,name FROM user WHERE id = ?
            resultSet = statement.executeQuery("SELECT id,user_name FROM user WHERE id = " + id);
            if(resultSet.next()) {
                String userName = resultSet.getString(1);
                User user = new User(id, userName);
                return user;
            }
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            return null;
        } finally {
            closeResultSet(resultSet);
            closeStatement(statement);
            closeConn(conn);
        }
        return null;
    }

    /**
     * 获取所有用户信息（实际开发需要分页，这个是示意）
     */
    public List<User> getAllUsers() {
        Connection conn = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<User> result = new ArrayList<>();
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            statement = conn.createStatement();
            //SELECT id,name FROM user
            resultSet = statement.executeQuery("SELECT id,user_name FROM user");
            while (resultSet.next()) {
                Integer id = resultSet.getInt(0);
                String userName = resultSet.getString(1);
                User user = new User(id, userName);
                result.add(user);
            }
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
        } finally {
            closeResultSet(resultSet);
            closeStatement(statement);
            closeConn(conn);
        }
        return result;
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

    private void closeStatement(final Statement statement) {
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
