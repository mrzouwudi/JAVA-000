# 作业说明
## Week05 作业题目（周四）：
1.（选做）使 Java 里的动态代理，实现一个简单的 AOP。
先把接口，和实现该接口的目标类列出来。
```
package aopusingjavaproxy;

public interface IStudent {
    void study();
}
```

```
package aopusingjavaproxy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StudentImpl implements IStudent {

    private int id;
    private String name;

    @Override
    public void study() {
        System.out.println("study 10000 hours.");
    }
}
```
下面在列出，使用Java行动态代理添加AOP操作的类
```
package aopusingjavaproxy;

import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@AllArgsConstructor
public class ProxyAOP implements InvocationHandler {

    private Object target;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        before();
        Object result = method.invoke(target, args);
        after();
        return result;
    }

    private void before() {
        System.out.println("happens before.....");
    }

    private void after() {
        System.out.println("happens after....");
    }
}
```
创建该类的实例时会将目标类的实例传入到构造函数中，调用invoke方法时会在调用目标方法时在前后加入AOP的方法。
下面是使用动态代理的Demo代码
```
package aopusingjavaproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class JavaProxyDemo {
    public static void main(String[] args) {
        IStudent student = new StudentImpl(1,"小张");
        InvocationHandler handler = new ProxyAOP(student);
        IStudent proxyStudent = (IStudent)Proxy.newProxyInstance(student.getClass().getClassLoader(), student.getClass().getInterfaces(), handler);
        proxyStudent.study();
    }
}
```
执行代码输入
```
happens before.....
study 10000 hours.
happens after....
```
以上所有代码在aopusingjavaproxy目录下。

2.（必做）写代码实现 Spring Bean 的装配，方式越多越好（XML、Annotation 都可以）, 提交到 Github。
以下各方式都是模拟一个service调用一个DAO保存一个实体对象，其中dao只是模拟保存另外因为只是实现配置所有没有接口，只是类之间的关联。

方式一：使用xml文件配置文件

先看下实体类、DAOs类以及service类
```
package springxmlconfig;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Department {
    private int id;
    private String name;
}
```

```
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
```

```
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
```

再看下xml配置文件
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="departmentDao" class="springxmlconfig.DepartmentDao"></bean>

    <bean id="departmentService" class="springxmlconfig.DepartmentService">
        <property name="departmentDao" ref="departmentDao"/>
    </bean>
</beans>
```

最后是demo程序
```
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
```

运行输出如下；
```
begin insert a department record
Department(id=1, name=Develop)
```
以上相关源文件和配置文件放在springxmlconfig目录下

方式二：使用groovy配置文件

先看下实体类、DAOs类以及service类
```
package springgroovyconfig;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Article {
    private int id;
    private String title;
    private String content;
}
```
```
package springgroovyconfig;

/**
 * 模拟项数据库插入一条记录，并且返回影响行数（数值1）
 */
public class ArticleDao {
    public int insertArticle(Article article) {
        System.out.println("begin insert a article record");
        System.out.println(article);
        return 1;
    }
}
```
```
package springgroovyconfig;

public class ArticleService {
    private ArticleDao articleDao;

    public ArticleService(ArticleDao articleDao) {
        this.articleDao = articleDao;
    }

    public boolean saveArticle(Article article) {
        int ret = articleDao.insertArticle(article);
        return (ret > 0);
    }
}
```
再看下groovy配置文件
```
import springgroovyconfig.ArticleDao
import springgroovyconfig.ArticleService

beans{
    articleDao(ArticleDao)
    articleService(ArticleService, articleDao)
}
```
最后是demo程序
```
package springgroovyconfig;

import org.springframework.context.support.GenericGroovyApplicationContext;

public class GroovyConfigDemo {
    public static void main(String[] args) {
        GenericGroovyApplicationContext context = new GenericGroovyApplicationContext(
                "classpath:ArticleGroovyConfig.groovy");
        ArticleService articleService = (ArticleService)context.getBean("articleService");
        Article article = new Article(1, "文章标题", "文章内容");
        articleService.saveArticle(article);
    }
}
```
运行输出如下；
```
begin insert a article record
Article(id=1, title=文章标题, content=文章内容)
```
以上相关源文件和配置文件放在springgroovyconfig目录下

方式三：使用FactoryBean的方式，该FactoryBean实现了FactoryBean接口

先看下实体类、DAOs类以及ervice类
```
package springfactorybean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private int id;
    private String name;
    private int price;
    private int amount;
}
```
```
package springfactorybean;

/**
 * 模拟项数据库插入一条记录，并且返回影响行数（数值1）
 */
public class ProductDao {
    public int insertProduct(Product product) {
        System.out.println("begin insert a product record");
        System.out.println(product);
        return 1;
    }
}
```
```
package springfactorybean;

import lombok.Setter;

@Setter
public class ProductService {
    private ProductDao productDao;

    public boolean saveProduct(Product product) {
        int ret = productDao.insertProduct(product);
        return (ret > 0);
    }
}
```

然后是重要的FactoryBean，这个FactoryBean只是实现构造ProductService的bean。这里我使用了@Component的注解，在xml中配置了component-scan，用于发现FactoryBean然后装配指定的Bean。
```
package springfactorybean;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component
public class ProductServiceFactoryBean implements FactoryBean {
    @Override
    public Object getObject() throws Exception {
        ProductService productService = new ProductService();
        productService.setProductDao(new ProductDao());
        return productService;
    }

    @Override
    public Class<?> getObjectType() {
        return ProductService.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
```

配置ProductServiceFactoryBean还是要通过component scan的方式，下面是对应xml文件
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context https://www.springframework.org/schema/context/spring-context.xsd">

    <context:component-scan base-package="springfactorybean"/>

</beans>
```

最后是Demo程序
```
package springfactorybean;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class FactoryBeanDemo {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-factorybean1.xml");
        ProductService productService = (ProductService)context.getBean(ProductService.class);
        Product product = new Product(1, "book", 59, 20);
        productService.saveProduct(product);
    }
}
```
运行结果，如下：
```
begin insert a product record
Product(id=1, name=book, price=59, amount=20)
```

方式四：使用普通的工厂搭配工厂方法进行装配。

这个方式中的实体类，dao类和service类复用了和方式三中的相关类，下面就不展示了，直接看普通工厂的代码，如下：
```
package springfactorybean;

import lombok.Setter;

@Setter
public class ProductServiceFactory {
    private ProductDao productDao;

    public ProductService getProductService() {
        ProductService productService = new ProductService();
        productService.setProductDao(productDao);
        return productService;
    }
}
```
其对应的xml配置文件，如下：
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context https://www.springframework.org/schema/context/spring-context.xsd">

    <bean id="productDao" class="springfactorybean.ProductDao"></bean>
    <bean id="ProductServiceFactory" class="springfactorybean.ProductServiceFactory">
        <property name="productDao" ref="productDao"></property>
    </bean>

    <bean id="productService" class="springfactorybean.ProductService" factory-bean="ProductServiceFactory" factory-method="getProductService"></bean>

</beans>
```
最后是demo程序
```
package springfactorybean;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class FoctoryMethodDemo {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-factorybean2.xml");
        ProductService productService = (ProductService)context.getBean("productService");
        Product product = new Product(1, "book", 59, 20);
        productService.saveProduct(product);
    }

}
```
运行结果如下：
```
begin insert a product record
Product(id=1, name=book, price=59, amount=20)
```
方式三和方式四中的相关源文件和配置文件放在springfactorybean目录下。

方式五：xml文件+component-scan，通过指定扫描的包，自动发现使用注解的类然后进行装配。

先看下实体类、DAO类以及service类，在DAO和service类，主要在DAO类和service类加上了相关注解，用于在扫描时识别出来。
```
package springcomponentscan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    private int id;
    private String name;
}
```
```
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
```
```
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
```

然后是在xml配置文件中使用了 <context:component-scan base-package="springcomponentscan"/>指定包进行扫描，然后装配，如下：
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
            http://www.springframework.org/schema/context
            http://www.springframework.org/schema/context/spring-context-3.0.xsd
            http://www.springframework.org/schema/beans
            http://www.springframework.org/schema/beans/spring-beans.xsd">

    <context:component-scan base-package="springcomponentscan"/>
</beans>
```
最后是demo程序
```
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
```
运行结果如下：
```
begin insert a employee record
Employee(id=1, name=张三)
```
以上相关源文件和配置文件放在springcomponentscan目录下。

方式六：是java代码的零配置方式。

先看下实体类、DAOs类以及service类，在DAO和service类
```
package springjavaconfig;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private int id;
    private String title;
    private String context;
}
```
```
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
```
```
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
```

然后是Java代码的配置类，在这里完成了装配，如下：
```
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
```
最后是demo程序
```
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
```
运行结果如下：
```
begin insert a department record
Task(id=1, title=任务标题, context=任务内容)
```
以上相关源文件放在springjavaconfig目录下。

## Week05 作业题目（周六）：
6.（必做）研究一下 JDBC 接口和数据库连接池，掌握它们的设计和用法：

1）使用 JDBC 原生接口，实现数据库的增删改查操作。

2）使用事务，PrepareStatement 方式，批处理方式，改进上述操作。

3）配置 Hikari 连接池，改进上述操作。

先在本地数据库demo中建表user，user表里包含两个字段id和user_name,id是自增主键，user_name是varchar（20）。
对应的实例类是User，如下：
```
package jdbc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {
    private Integer id;
    private String userName;
}
```

1）使用 JDBC 原生接口，实现数据库的增删改查操作。
```
package jdbc;

import lombok.AllArgsConstructor;
import lombok.Setter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Setter
@AllArgsConstructor
public class StatementJdbcUserDao {
    private String jdbcUrl;
    private String username;
    private String password;
    static {
        try {
            //注册Driver操作对象
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加一个用户
     * @param user
     * @return
     */
    public boolean insertUser(final User user) {
        if(null == user) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        Statement statement = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            statement = conn.createStatement();
            //INSERT INTO user (name) VALUES (?)
            int ret = statement.executeUpdate("INSERT INTO user ( user_name) VALUES (" + user.getUserName() + ")");
            return (ret > 0);
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            return false;
        } finally {
            closeStatement(statement);
            closeConn(conn);
        }
    }

    /**
     * 删除指定用户
     * @param id
     * @return
     */
    public boolean deleteUserById(final Integer id) {
        if (null == id) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        Statement statement = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            statement = conn.createStatement();
            //DELETE FROM user WHERE id = ？
            int ret = statement.executeUpdate("DELETE FROM user WHERE id = " + id);
            return (ret > 0);
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            return false;
        } finally {
            closeStatement(statement);
            closeConn(conn);
        }
    }

    /**
     * 更改用户名字
     * @param user
     * @return
     */
    public boolean updateUserName(final User user) {
        if (null == user || null == user.getId()) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        Statement statement = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            statement = conn.createStatement();
            //UPDATE user SET name = ? WHERE id = ?
            int ret = statement.executeUpdate("UPDATE user SET name = "+user.getUserName()+" WHERE id = " + user.getId());
            return (ret > 0);
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            return false;
        } finally {
            closeStatement(statement);
            closeConn(conn);
        }
    }

    /**
     * 获取指定用户信息
     * @param id
     * @return
     */
    public User getUserById(final Integer id) {
        if(null == id) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            statement = conn.createStatement();
            //SELECT id,name FROM user WHERE id = ?
            resultSet = statement.executeQuery("SELECT id,user_name FROM user WHERE id = " + id);
            if(resultSet.next()) {
                String userName = resultSet.getString(1);
                User user = new User(id, userName);
                return user;
            }
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            return null;
        } finally {
            closeResultSet(resultSet);
            closeStatement(statement);
            closeConn(conn);
        }
        return null;
    }

    /**
     * 获取所有用户信息（实际开发需要分页，这个是示意）
     */
    public List<User> getAllUsers() {
        Connection conn = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<User> result = new ArrayList<>();
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            statement = conn.createStatement();
            //SELECT id,name FROM user
            resultSet = statement.executeQuery("SELECT id,user_name FROM user");
            while (resultSet.next()) {
                Integer id = resultSet.getInt(0);
                String userName = resultSet.getString(1);
                User user = new User(id, userName);
                result.add(user);
            }
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
        } finally {
            closeResultSet(resultSet);
            closeStatement(statement);
            closeConn(conn);
        }
        return result;
    }

    private void closeConn(final Connection conn) {
        if(conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeStatement(final Statement statement) {
        if(statement != null) {
            try{
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeResultSet(final ResultSet resultSet) {
        if(resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
```

2）使用事务，PrepareStatement 方式，批处理方式，改进上述操作。
```
package jdbc;

import lombok.AllArgsConstructor;
import lombok.Setter;

import java.sql.*;
import java.util.List;

/**
 * 使用事务，PrepareStatement 方式，批处理方式
 */
@Setter
@AllArgsConstructor
public class PrepareStatementJdbcUserDao {
    private String jdbcUrl;
    private String username;
    private String password;
    static {
        try {
            //注册Driver操作对象
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加一个用户, 使用PrepareStatement和显式的事务
     * @param user
     * @return
     */
    public boolean insertUser(final User user) {
        if(null == user) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            conn.setAutoCommit(false);
            statement = conn.prepareStatement("INSERT INTO user (user_name) VALUES (?)");
            statement.setString(1, user.getUserName());
            int ret = statement.executeUpdate();
            conn.commit();
            return (ret > 0);
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            rollback(conn);
            return false;
        } finally {
            closePreparedStatement(statement);
            closeConn(conn);
        }
    }

    /**
     * 删除指定用户, 使用PrepareStatement和显式的事务
     * @param id
     * @return
     */
    public boolean deleteUserById(final Integer id) {
        if (null == id) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            conn.setAutoCommit(false);
            statement = conn.prepareStatement("DELETE FROM user WHERE id = ？");
            statement.setInt(1, id);
            int ret = statement.executeUpdate();
            conn.commit();
            return (ret > 0);
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            rollback(conn);
            return false;
        } finally {
            closePreparedStatement(statement);
            closeConn(conn);
        }
    }

    /**
     * 更改用户名字, 使用PrepareStatement和显式的事务
     * @param user
     * @return
     */
    public boolean updateUserName(final User user) {
        if (null == user || null == user.getId()) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            conn.setAutoCommit(false);
            statement = conn.prepareStatement("UPDATE user SET user_name = ? WHERE id = ?");
            statement.setString(1, user.getUserName());
            statement.setInt(2, user.getId());
            int ret = statement.executeUpdate();
            conn.commit();
            return (ret > 0);
        } catch (SQLException e) {
            e.printStackTrace();
            rollback(conn);
            return false;
        } finally {
            closePreparedStatement(statement);
            closeConn(conn);
        }
    }

    /**
     * 获取指定用户信息，使用PrepareStatement，查询不用事务
     * @param id
     * @return
     */
    public User getUserById(final Integer id) {
        if(null == id) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            statement = conn.prepareStatement("SELECT id,user_name FROM user WHERE id = ?");
            statement.setInt(1, id);
            resultSet = statement.executeQuery();
            if(resultSet.next()) {
                String userName = resultSet.getString(2);
                User user = new User(id, userName);
                return user;
            }
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            return null;
        } finally {
            closeResultSet(resultSet);
            closePreparedStatement(statement);
            closeConn(conn);
        }
        return null;
    }

    /**
     * 使用事务，PrepareStatement 方式，批处理方式的方式进行插入
     * @param users
     * @return
     */
    public boolean insertUsers(List<User> users) {
        if(null == users) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, username, password);
            conn.setAutoCommit(false);
            statement = conn.prepareStatement("INSERT INTO user (user_name) VALUES (?)");
            for(User user : users) {
                statement.setString(1, user.getUserName());
                statement.addBatch();
            }
            statement.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            rollback(conn);
            return false;
        } finally {
            closePreparedStatement(statement);
            closeConn(conn);
        }
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    private void closeConn(final Connection conn) {
        if(conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void closePreparedStatement(final PreparedStatement statement) {
        if(statement != null) {
            try{
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeResultSet(final ResultSet resultSet) {
        if(resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
```
其中，insertUsers使用了PreparedStatement的批量执行的相关方法addBatch()和executeBatch()

3）配置 Hikari 连接池，改进上述操作。

为配置Hikari连接池增加一个DatasourceConfig类，如下：
```
package jdbc;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DatasourceConfig {
    private String jdbcUrl;
    private String username;
    private String password;
    private int minimumIdle;
    private int maximumPoolSize;
    private int connectionTimeout;
}
```
配置Hikari datasource后，代码如下：
```
package jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

/**
 * 配置了 Hikari 连接池
 */
public class DatasourceUserDao {
    private DataSource dataSource;

    static {
        try {
            //注册Driver操作对象
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public DatasourceUserDao(DatasourceConfig datasourceConfig) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(datasourceConfig.getJdbcUrl());
        config.setUsername(datasourceConfig.getUsername());
        config.setPassword(datasourceConfig.getPassword());
        config.setMinimumIdle(datasourceConfig.getMinimumIdle());
        config.setMaximumPoolSize(datasourceConfig.getMaximumPoolSize());
        config.setConnectionTimeout(datasourceConfig.getConnectionTimeout());
        config.setAutoCommit(false);
        dataSource = new HikariDataSource(config);
    }

    /**
     * 增加一个用户, 使用PrepareStatement和显式的事务
     * @param user
     * @return
     */
    public boolean insertUser(final User user) {
        if(null == user) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            statement = conn.prepareStatement("INSERT INTO user (user_name) VALUES (?)");
            statement.setString(1, user.getUserName());
            int ret = statement.executeUpdate();
            conn.commit();
            return (ret > 0);
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            rollback(conn);
            return false;
        } finally {
            closePreparedStatement(statement);
            closeConn(conn);
        }
    }

    /**
     * 删除指定用户, 使用PrepareStatement和显式的事务
     * @param id
     * @return
     */
    public boolean deleteUserById(final Integer id) {
        if (null == id) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            statement = conn.prepareStatement("DELETE FROM user WHERE id = ？");
            statement.setInt(1, id);
            int ret = statement.executeUpdate();
            conn.commit();
            return (ret > 0);
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            rollback(conn);
            return false;
        } finally {
            closePreparedStatement(statement);
            closeConn(conn);
        }
    }

    /**
     * 更改用户名字, 使用PrepareStatement和显式的事务
     * @param user
     * @return
     */
    public boolean updateUserName(final User user) {
        if (null == user || null == user.getId()) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            statement = conn.prepareStatement("UPDATE user SET user_name = ? WHERE id = ?");
            statement.setString(1, user.getUserName());
            statement.setInt(2, user.getId());
            int ret = statement.executeUpdate();
            conn.commit();
            return (ret > 0);
        } catch (SQLException e) {
            e.printStackTrace();
            rollback(conn);
            return false;
        } finally {
            closePreparedStatement(statement);
            closeConn(conn);
        }
    }

    /**
     * 获取指定用户信息，使用PrepareStatement，查询不用事务
     * @param id
     * @return
     */
    public User getUserById(final Integer id) {
        if(null == id) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            conn = dataSource.getConnection();
            statement = conn.prepareStatement("SELECT id,user_name FROM user WHERE id = ?");
            statement.setInt(1, id);
            resultSet = statement.executeQuery();
            if(resultSet.next()) {
                String userName = resultSet.getString(2);
                User user = new User(id, userName);
                return user;
            }
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            return null;
        } finally {
            closeResultSet(resultSet);
            closePreparedStatement(statement);
            closeConn(conn);
        }
        return null;
    }

    /**
     * 使用事务，PrepareStatement 方式，批处理方式的方式进行插入
     * @param users
     * @return
     */
    public boolean insertUsers(List<User> users) {
        if(null == users) {
            throw new IllegalArgumentException();
        }
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();
            statement = conn.prepareStatement("INSERT INTO user (user_name) VALUES (?)");
            for(User user : users) {
                statement.setString(1, user.getUserName());
                statement.addBatch();
            }
            statement.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException e) {
            //处理异常信息
            e.printStackTrace();
            rollback(conn);
            return false;
        } finally {
            closePreparedStatement(statement);
            closeConn(conn);
        }
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    private void closeConn(final Connection conn) {
        if(conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void closePreparedStatement(final PreparedStatement statement) {
        if(statement != null) {
            try{
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeResultSet(final ResultSet resultSet) {
        if(resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
```
demo程序如下：
```
package jdbc;

import java.util.ArrayList;

public class JdbcDemo {
    public static void main(String[] args) {
        DatasourceConfig config = new DatasourceConfig();
        config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/demo?serverTimezone=UTC&characterEncoding=utf-8");
        config.setUsername("root");
        config.setPassword("");
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(3000);
        DatasourceUserDao userDao = new DatasourceUserDao(config);
        userDao.insertUser(new User(null, "小张"));
        User user = userDao.getUserById(1);
        System.out.println(user);
        userDao.updateUserName(new User(1, "小李"));
        ArrayList<User> users = new ArrayList<>();
        users.add(new User(null, "aaa"));
        users.add(new User(null, "bbb"));
        userDao.insertUsers(users);
        userDao.deleteUserById(1);
    }
}
```
以上源代码都在jdbc目录下