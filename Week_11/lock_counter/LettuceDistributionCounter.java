package traincamp.redis;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class LettuceDistributionCounter implements DistributionCounter {

    private static final String LOCK_SUCCESS = "OK";

    private StatefulRedisConnection<String,String> connection;

    private String key;

    public LettuceDistributionCounter(StatefulRedisConnection<String,String> connection, String key) {
        this.connection = connection;
        this.key = key;
        setCounter(0);
    }

    public LettuceDistributionCounter(StatefulRedisConnection<String,String> connection, String key, long initCount) {
        this.connection = connection;
        this.key = key;
        if (initCount < 0) {
            setCounter(0L);
        } else {
            setCounter(initCount);
        }
    }

    @Override
    public boolean setCounter(long count) {
        RedisCommands<String, String> commands = connection.sync();
        String result = commands.set(key, String.valueOf(count));
        return LOCK_SUCCESS.equals(result);
    }

    @Override
    public Long get() {
        RedisCommands<String, String> commands = connection.sync();
        String value = commands.get(key);
        return Long.valueOf(value);
    }

    @Override
    public Long increase() {
        RedisCommands<String, String> commands = connection.sync();
        return commands.incr(key);
    }

    @Override
    public Long decrease() {
        return decrease(1L);
    }

    @Override
    public Long increase(long change) {
        RedisCommands<String, String> commands = connection.sync();
        return commands.incrby(key, change);
    }

    @Override
    public Long decrease(long change) {
        RedisCommands<String, String> commands = connection.sync();
        String script = "if (redis.call('exists', KEYS[1]) == 0) then return nil; end; " +
                "local counter = redis.call('get', KEYS[1]);if ((counter - ARGV[1]) >= 0) then " +
                "return redis.call('decrby', KEYS[1], ARGV[1]); end; return nil;";
        String[] keys = new String[]{key};
        String[] args = new String[]{String.valueOf(change)};
        Long result = commands.eval(script, ScriptOutputType.INTEGER, keys, args);
        return result;
    }
}
