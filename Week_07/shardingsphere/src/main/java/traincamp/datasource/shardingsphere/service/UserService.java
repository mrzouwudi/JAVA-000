package traincamp.datasource.shardingsphere.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import traincamp.datasource.shardingsphere.dao.UserMapper;
import traincamp.datasource.shardingsphere.entity.User;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public void saveUser(User user) {
        userMapper.insert(user);
    }

    public User getUserById(Integer id) {
        User user = userMapper.selectByPrimaryKey(id);
        if(user != null) {
            System.out.println(user.getNickname());
        }
        return user;
    }

    public List<User> getUsers() {
        List<User> users = userMapper.selectByExample(null);
        return users;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<User> saveOneUserAndGetUsers(User user) {
        userMapper.insert(user);
        return userMapper.selectByExample(null);
    }
}
