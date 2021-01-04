package traincamp.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.UUID;

public class JedisDistributionLock implements DistributionLock{

    private static final String LOCK_SUCCESS = "OK";

    private static final Long RELEASE_SUCCESS = 1L;

    private static final int DEFAULT_EXPIRE_TIME = 30000;

    private Jedis client;

    private String key;

    private int expireTime;

    private String lockValue;

    public JedisDistributionLock(Jedis client, String key, int expireTime) {
        this.client = client;
        this.key = key;
        this.expireTime = expireTime <= 0 ? DEFAULT_EXPIRE_TIME : expireTime;
    }

    @Override
    public void acquire() {
        lockValue = UUID.randomUUID().toString();
        SetParams params = new SetParams();
        params.nx().px(expireTime);
        String result = client.set(key, lockValue, params);
        while (!LOCK_SUCCESS.equals(result)) {
            try {
                Thread.sleep(10);
            }  catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = client.set(key, lockValue, params);
        }
    }

    @Override
    public boolean tryAcquire() {
        lockValue = UUID.randomUUID().toString();
        SetParams params = new SetParams();
        params.nx().px(expireTime);
        String result = client.set(key, lockValue, params);
        return LOCK_SUCCESS.equals(result);
    }

    @Override
    public boolean release() {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = client.eval(script, Collections.singletonList(key), Collections.singletonList(lockValue));
        return RELEASE_SUCCESS.equals(result);
    }
}
