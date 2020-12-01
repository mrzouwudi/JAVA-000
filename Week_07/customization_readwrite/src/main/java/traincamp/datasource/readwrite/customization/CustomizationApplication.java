package traincamp.datasource.readwrite.customization;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import traincamp.datasource.readwrite.customization.entity.User;
import traincamp.datasource.readwrite.customization.service.UserService;

import java.util.Date;
import java.util.List;

@SpringBootApplication
@MapperScan("traincamp.datasource.readwrite.customization.dao")
public class CustomizationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomizationApplication.class, args);
    }

}
