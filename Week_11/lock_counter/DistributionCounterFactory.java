package traincamp.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class DistributionCounterFactory {

    public static final String DCOUNTER_JEDIS = "jedis";

    public static final String DCOUNTER_LETTUCE = "luttuce";

    private DistributionCounterFactory(){}

    public static DistributionCounter getCounter(String type, String key) {
        return getCounter(type, key, 0L);
    }

    public static DistributionCounter getCounter(String type, String key, long initCount) {
        switch (type) {
            case DCOUNTER_JEDIS:
                Jedis jedisClient = new Jedis("localhost");
                return new JedisDistributionCounter(jedisClient, key, initCount);
            case DCOUNTER_LETTUCE:
                RedisURI uri = RedisURI.builder()
                        .withHost("localhost")
                        .withPort(6379)
                        .withTimeout(Duration.of(10, ChronoUnit.SECONDS))
                        .build();
                RedisClient client = RedisClient.create(uri);
                StatefulRedisConnection<String,String> connection = client.connect();
                return new LettuceDistributionCounter(connection, key, initCount);
            default:
                return null;
        }
    }
}
