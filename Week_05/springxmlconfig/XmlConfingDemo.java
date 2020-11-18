package springxmlconfig;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class XmlConfingDemo {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        DepartmentService departmentService = (DepartmentService)context.getBean("departmentService");
        Department department = new Department(1, "Develop");
        departmentService.saveDepartment(department);
    }
}
