package traincamp.dbexpone.jdbc;

import traincamp.dbexpone.entity.Order;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class DirectJdbcImport {
    public static void main(String[] args) {
        DataSourceConfig config = new DataSourceConfig();
        config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/tinymall?serverTimezone=UTC&characterEncoding=utf-8");
        config.setUsername("root");
        config.setPassword("root");
        config.setMinimumIdle(10);
        config.setMaximumPoolSize(30);
        config.setConnectionTimeout(3000);
        DataSource dataSource = DataSourceFactory.getDataSource(DataSourceFactory.HIKARI_DATASOURCE, config);
        if(dataSource == null) {
            throw new IllegalArgumentException();
        }
        PrepareStatementDatasourceOrderDao orderDao = new PrepareStatementDatasourceOrderDao(dataSource);

        OrderGenerator orderGenerator = new OrderGenerator();
        List<List<Order>> orders = new ArrayList<>(200);
        for (int i=0; i<200; i++){
            orders.add(orderGenerator.generateManyOrders(5000));
        }
        Long start = System.currentTimeMillis();
        orders.stream().parallel().forEach(orderDao::insertBatchOrders);
        Long end = System.currentTimeMillis();
        System.out.println("耗时："+(end - start)+"ms");
    }
}
