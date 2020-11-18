package jdbc;

import java.util.ArrayList;

public class JdbcDemo {
    public static void main(String[] args) {
        DatasourceConfig config = new DatasourceConfig();
        config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/demo?serverTimezone=UTC&characterEncoding=utf-8");
        config.setUsername("root");
        config.setPassword("");
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(3000);
        DatasourceUserDao userDao = new DatasourceUserDao(config);
        userDao.insertUser(new User(null, "小张"));
        User user = userDao.getUserById(1);
        System.out.println(user);
        userDao.updateUserName(new User(1, "小李"));
        ArrayList<User> users = new ArrayList<>();
        users.add(new User(null, "aaa"));
        users.add(new User(null, "bbb"));
        userDao.insertUsers(users);
        userDao.deleteUserById(1);
    }
}
