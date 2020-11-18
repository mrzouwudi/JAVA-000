package springxmlconfig;

/**
 * 模拟项数据库插入一条记录，并且返回影响行数（数值1）
 */
public class DepartmentDao {
    public int insetDepartment(Department department) {
        System.out.println("begin insert a department record");
        System.out.println(department);
        return 1;
    }
}
