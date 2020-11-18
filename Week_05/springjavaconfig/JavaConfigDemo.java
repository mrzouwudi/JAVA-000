package springjavaconfig;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class JavaConfigDemo {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TaskConfig.class);
        TaskService taskService = (TaskService)context.getBean("taskService");
        Task task = new Task(1, "任务标题", "任务内容");
        taskService.saveTask(task);
    }
}
