package trainingcamp.homework.springbootautoconfig.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import other.config.SchoolConfig;

@Configuration
@Import(SchoolConfig.class)
public class MySchooAutoConfig {
}