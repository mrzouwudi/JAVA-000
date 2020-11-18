package other.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import other.bean.Klass;
import other.bean.School;
import other.bean.Student;

@Configuration
@ConditionalOnProperty(prefix = "myschool", name = "enabled", havingValue = "true",
        matchIfMissing = true)
public class SchoolConfig {
    @Bean
    @ConditionalOnMissingBean(Klass.class)
    public Klass klass() {
        return new Klass();
    }

    @Bean("student100")
    public Student student100(){
        return new Student(1112,"kkkk");
    }

    @Bean
    @ConditionalOnMissingBean(School.class)
    @ConditionalOnProperty(
            prefix="myschool",
            name="school",
            matchIfMissing = true
    )
    public School school(){
        School school = new School();

        return school;
    }

}
