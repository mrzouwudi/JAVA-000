package springjavaconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskConfig {

    @Bean("taskDao")
    public TaskDao taskDao() {
        return new TaskDao();
    }
    @Bean("taskService")
    public TaskService taskService() {
        TaskService taskService = new TaskService();
        taskService.setTaskDao(taskDao());
        return taskService;
    }
}
