package traincamp.shardingdb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("traincamp.shardingdb.dao")
public class ShardingdbApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShardingdbApplication.class, args);
	}

}
