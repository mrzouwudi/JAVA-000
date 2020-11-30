package loaddata;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataSourceConfig {
    private String jdbcUrl;
    private String username;
    private String password;
    private int minimumIdle;
    private int maximumPoolSize;
    private int connectionTimeout;
}
