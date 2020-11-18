package springxmlconfig;

import lombok.Setter;

@Setter
public class DepartmentService {

    private DepartmentDao departmentDao;

    public boolean saveDepartment(Department department) {
        int ret = departmentDao.insetDepartment(department);
        return (ret > 0);
    }
}
