package traincamp.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class DistributionLockFactory {

    public static final String DLOCK_JEDIS = "jedis";

    public static final String DLOCK_LETTUCE = "luttuce";

    private DistributionLockFactory(){}

    public static DistributionLock getLock(String type, String key, int expireTime) {
        switch (type) {
            case DLOCK_JEDIS:
                Jedis jedis = new Jedis("localhost");
                return new JedisDistributionLock(jedis, key, expireTime);
            case DLOCK_LETTUCE:
                RedisURI uri = RedisURI.builder()
                        .withHost("localhost")
                        .withPort(6379)
                        .withTimeout(Duration.of(10, ChronoUnit.SECONDS))
                        .build();
                RedisClient client = RedisClient.create(uri);
                StatefulRedisConnection<String,String> connection = client.connect();
                return new LettuceDistributionLock(connection,key, expireTime);
            default:
                return null;
        }
    }
}
