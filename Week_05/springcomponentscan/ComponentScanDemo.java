package springcomponentscan;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ComponentScanDemo {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-component.xml");
        EmployeeService employeeService = (EmployeeService)context.getBean("employeeService", EmployeeService.class);
        Employee employee = new Employee(1, "张三");
        employeeService.saveEmployee(employee);
    }
}
