package traincamp.datasource.readwrite.customization.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import traincamp.datasource.readwrite.customization.entity.User;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    UserService userService;

    @Test
    public void testInsert() {
        User user = new User();
        user.setNickname("Alice");
        user.setLoginName("Alice");
        user.setPassword("7c4a8d09ca3762af61e59520943dc26494f8941b");
        user.setMobile("135001350000");
        user.setCreatedTime(new Date());
        user.setIsDelete(Byte.valueOf("0"));
        userService.insertUser(user);
    }
}
