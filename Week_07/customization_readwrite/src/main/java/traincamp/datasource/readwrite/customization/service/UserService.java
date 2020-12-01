package traincamp.datasource.readwrite.customization.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import traincamp.datasource.readwrite.customization.annotation.Master;
import traincamp.datasource.readwrite.customization.dao.UserMapper;
import traincamp.datasource.readwrite.customization.entity.User;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Master
    public void saveUser(User user) {
        userMapper.insert(user);
    }

    public void insertUser(User user) {
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
        users.stream().forEach(System.out::println);
        return users;
    }
}
