package traincamp.datasource.readwrite.customization.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import traincamp.datasource.readwrite.customization.entity.User;
import traincamp.datasource.readwrite.customization.service.UserService;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @Autowired
    private UserService userService;

    @GetMapping("/save")
    public String saveUser() {
        User user = generatorUser();
        userService.insertUser(user);
        return "save one user";
    }

    @GetMapping("/user/{id}")
    public User getUserById(@PathVariable("id") Integer id) {
        User user =  userService.getUserById(id);
        if(user == null) {
            System.out.println("user is null");
        }
        return user;
    }

    @GetMapping("/users")
    public List<User> users() {
        return userService.getUsers();
    }

    private User generatorUser() {
        User user = new User();
        user.setNickname("Alice");
        user.setLoginName("Alice");
        user.setPassword("7c4a8d09ca3762af61e59520943dc26494f8941b");
        user.setMobile("13500135000");
        user.setCreatedTime(new Date());
        user.setIsDelete(Byte.valueOf("0"));
        return user;
    }
}
