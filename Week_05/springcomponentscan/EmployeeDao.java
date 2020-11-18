package springcomponentscan;

import org.springframework.stereotype.Component;

/**
 * 模拟项数据库插入一条记录，并且返回影响行数（数值1）
 */
@Component
public class EmployeeDao {

    public int insertEmployee(Employee employee) {
        System.out.println("begin insert a employee record");
        System.out.println(employee);
        return 1;
    }
}
