package springjavaconfig;

import lombok.Setter;

@Setter
public class TaskService {
    private TaskDao taskDao;

    public boolean saveTask(Task task) {
        int ret = taskDao.insertTask(task);
        return (ret > 0);
    }
}
