package loaddata;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceFactory {
    public final static String HIKARI_DATASOURCE = "hikari";

    private DataSourceFactory(){}

    public static DataSource getDataSource(String dataSourceType, DataSourceConfig dataSourceConfig) {
        switch (dataSourceType) {
            case HIKARI_DATASOURCE :
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(dataSourceConfig.getJdbcUrl());
                config.setUsername(dataSourceConfig.getUsername());
                config.setPassword(dataSourceConfig.getPassword());
                config.setMinimumIdle(dataSourceConfig.getMinimumIdle());
                config.setMaximumPoolSize(dataSourceConfig.getMaximumPoolSize());
                config.setConnectionTimeout(dataSourceConfig.getConnectionTimeout());
                config.setAutoCommit(false);
                return new HikariDataSource(config);
            default:
                return null;

        }
    }
}
