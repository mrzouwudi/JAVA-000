package springcomponentscan;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeDao employeeDao;

    public boolean saveEmployee(Employee employee) {
        int ret = employeeDao.insertEmployee(employee);
        return (ret > 0);
    }
}
