package loaddata;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class LoadDataMain {
    public static void main(String[] args) throws Exception{
        InputStream is = generateOrdersIntoInputStream();

        PreparedStatementDataLoader dataLoader = buildPreparedStatementDataLoader();
        try {
            Long start = System.currentTimeMillis();
            int ret = dataLoader.loadData(is);
            Long end = System.currentTimeMillis();
            System.out.println("耗时："+(end - start)+"ms");
            System.out.println(ret);
        } finally {
            is.close();
        }
    }

    private static InputStream generateOrdersIntoInputStream() {
        OrderGenerator orderGenerator = new OrderGenerator();
        List<Order> orders = orderGenerator.generateManyOrders(1_000_000);
        StringBuilder stringBuilder = new StringBuilder(100_000_000);
        orders.stream().forEach((order -> OrderData2StringUtil.fillOrderDataIntoStringBuilder(order, stringBuilder)));
        byte[] bytes = stringBuilder.toString().getBytes();
        return new ByteArrayInputStream(bytes);
    }

    private static PreparedStatementDataLoader buildPreparedStatementDataLoader() {
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
        return new PreparedStatementDataLoader(dataSource);
    }
}
