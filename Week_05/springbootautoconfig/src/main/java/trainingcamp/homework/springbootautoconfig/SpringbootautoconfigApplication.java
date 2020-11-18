package trainingcamp.homework.springbootautoconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import other.bean.School;

@SpringBootApplication
public class SpringbootautoconfigApplication implements CommandLineRunner {

    @Autowired
    School school;

    public static void main(String[] args) {
        SpringApplication.run(SpringbootautoconfigApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(school);
    }
}
