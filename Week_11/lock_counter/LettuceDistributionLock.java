package traincamp.redis;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.UUID;

public class LettuceDistributionLock implements DistributionLock {

    private static final String LOCK_SUCCESS = "OK";

    private static final int DEFAULT_EXPIRE_TIME = 30000;

    private StatefulRedisConnection<String,String> connection;

    private String key;

    private int expireTime;

    private String lockValue;

    public LettuceDistributionLock(StatefulRedisConnection<String,String> connection, String key, int expireTime) {
        this.connection = connection;
        this.key = key;
        this.expireTime = expireTime <= 0 ? DEFAULT_EXPIRE_TIME : expireTime;
    }

    @Override
    public void acquire() {
        lockValue = UUID.randomUUID().toString();
        RedisCommands<String, String> commands = connection.sync();
        SetArgs args = SetArgs.Builder.nx().px(expireTime);
        String result = commands.set(key, lockValue, args);
        while (!LOCK_SUCCESS.equals(result)) {
            try {
                Thread.sleep(10);
            }  catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = commands.set(key, lockValue, args);
        }
    }

    @Override
    public boolean tryAcquire() {
        lockValue = UUID.randomUUID().toString();
        RedisCommands<String, String> commands = connection.sync();
        String result = commands.set(key, lockValue, SetArgs.Builder.nx().px(expireTime));
        return LOCK_SUCCESS.equals(result);
    }

    @Override
    public boolean release() {
        RedisCommands<String, String> commands = connection.sync();
        String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String[] keys = new String[]{key};
        String[] args = new String[]{lockValue};
        Boolean result = commands.eval(luaScript, ScriptOutputType.BOOLEAN, keys, args);
        return result;
    }
}
