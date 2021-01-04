package traincamp.redis;

import redis.clients.jedis.Jedis;

import java.util.Collections;

public class JedisDistributionCounter implements DistributionCounter{

    private static final String LOCK_SUCCESS = "OK";

    private Jedis client;

    private String key;

    public JedisDistributionCounter(Jedis client, String key) {
        this.client = client;
        this.key = key;
        setCounter(0L);
    }

    public JedisDistributionCounter(Jedis client, String key, long initCount) {
        this.client = client;
        this.key = key;
        if (initCount < 0) {
            setCounter(0L);
        } else {
            setCounter(initCount);
        }
    }

    @Override
    public boolean setCounter(long count) {
        if (count < 0) {
            return false;
        }
        String result = client.set(key, String.valueOf(count));
        return LOCK_SUCCESS.equals(result);
    }

    @Override
    public Long get() {
        String value = client.get(key);
        return Long.valueOf(value);
    }

    @Override
    public Long increase() {
        return client.incr(key);
    }

    @Override
    public Long decrease() {
        return decrease( 1L );
    }

    @Override
    public Long increase(long change) {
        return client.incrBy(key, change);
    }

    @Override
    public Long decrease(long change) {
        String script = "if (redis.call('exists', KEYS[1]) == 0) then return nil; end; " +
                "local counter = redis.call('get', KEYS[1]);if ((counter - ARGV[1]) >= 0) then " +
                "return redis.call('decrby', KEYS[1], ARGV[1]); end; return nil;";
        Object result = client.eval(script, Collections.singletonList(key), Collections.singletonList(String.valueOf(change)));
        return (Long)result;
    }
}
