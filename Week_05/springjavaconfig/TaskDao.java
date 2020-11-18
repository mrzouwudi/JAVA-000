package springjavaconfig;

/**
 * 模拟项数据库插入一条记录，并且返回影响行数（数值1）
 */
public class TaskDao {

    public int insertTask(Task task) {
        System.out.println("begin insert a department record");
        System.out.println(task);
        return 1;
    }
}
