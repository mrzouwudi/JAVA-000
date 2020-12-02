package traincamp.datasource.shardingsphere.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import traincamp.datasource.shardingsphere.entity.User;
import traincamp.datasource.shardingsphere.service.UserService;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/hello")
public class HelloController {
    @Autowired
    private UserService userService;

    @GetMapping("/user/{id}")
    public User getUserById(@PathVariable("id") Integer id) {
        User user = userService.getUserById(id);
        return user;
    }

    @GetMapping("/users")
    public List<User> users() {
        return userService.getUsers();
    }

    @GetMapping("/save")
    public List<User> saveUser() {
        User user = generatorUser();
        return userService.saveOneUserAndGetUsers(user);
    }

    private User generatorUser() {
        User user = new User();
        user.setNickname("Alex");
        user.setLoginName("Alex");
        user.setPassword("7c4a8d09ca3762af61e59520943dc26494f8941b");
        user.setMobile("13500135000");
        user.setCreatedTime(new Date());
        user.setIsDelete(Byte.valueOf("0"));
        return user;
    }
}