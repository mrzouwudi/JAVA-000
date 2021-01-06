# 作业说明
## Week11 作业题目：
4.（必做）基于 Redis 封装分布式数据操作：

- 在 Java 中实现一个简单的分布式锁；

- 在 Java 中实现一个分布式计数器，模拟减库存。

（1) 简单的分布式锁

本题的实现是通过Jedis和Lettuce各自实现一个简单分布式锁的接口；而Redisson已经有分布式锁的多个工具类可以使用，因此直接对Redisson的可重入分布式锁也进行了相关实践，作为比较。

首先，本题实现的简单分布锁只考虑两种使用方式，一是，阻塞等待获取锁然后加锁进行相关操作最后释放锁，二是，尝试获取锁，如果获取成功，加锁，使用完毕释放，如果获取失败直接返回。这里不考虑可重入性，自动续期等特性。因此简单分布锁的接口如下：

```java
package traincamp.redis;

public interface DistributionLock {
    /**
     * 获取分布式锁，如果锁已被其他进程取得，则每隔10ms继续获取，直到获取成功
     */
    void acquire();

    /**
     * 尝试获取分布式锁，获取成功则返回true，如果锁已被其他进程取得，则返回false
     * @return
     */
    boolean tryAcquire();

    /**
     * 释放锁，如果对应的锁存在则释放成功返回true，如果锁不存在，或锁值已不匹配，则释放失败返回false
     * @return
     */
    boolean release();
}
```

先看一下，Jedis的实现，代码如下：

```java
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
```

构造函数中会传入Jedis的客户端，用户使用分布式锁时的键，分布式锁保持的时间（即如果没有释放时最长存在时间）。

acquire方法是获取锁，且是阻塞方式，如果获取不到，会每隔10ms再尝试获取一次直至获取成功。这里的获取成功，就是通过“SET dlock my_random_value NX PX 30000  ”这条操作语句是否成功来判断的，其中dlock键是用户创建锁时传入的键名称，my_random_value是用UUID生成的，PX后面的时间可以在创建锁时指定，如果是0或负数则使用缺省值（30000）。下面tryAcquire的方法也是同样的方式获取锁，不同之处就是tryAcquire获取锁不成功，就直接返回false，不在进行重试，就是快速失败。

release方法是通过lua脚本进行的，这是为了保证原子性操作。脚本就是参考讲义里的。这里面是要判断键值是否就是上锁时的随机字串，主要是防止误删除其他进程上的锁。出现这种情形是这样的，本进程上锁没释放，超时锁自动删除，其他进程上锁，这时本进程进行释放操作；另外一种情形是本进程因为编码问题进行了两次释放操作。

然后，再看一下Lettuce的实现，其实和Jedis大同小异，代码如下：

```java
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
```

Lettuce和Jedis的实现思路上是一样的。这里Lettuce的实现方式采用的是同步方式，没有使用异步和Reactive的方式，后面可以尝试一下，看看有什么不太一样的地方。另外这里，Lua脚本没有使用evalsha的方式，后面也可以尝试一下。

为了更好和用户代码的解耦，另外写了一个工厂类，如下：

```java
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
```

进接着看一下测试代码，因为获取锁有两种方式，一种是阻塞等待锁，另一种是尝试获取，获取不到立即返回，所以，也针对这两种情况分别写了测试代码。为了模拟两个进程同时获取锁，使用两个不同的类模拟进程一和进程二，其中进程一会通过sleep持有锁的时间相对比较长，进程二只是获取锁输出一段文字。下面是阻塞式获取锁时，进程一和进程二的类的代码：

```java
package traincamp.redis;

public class UsingBlockLockProcessOne {
    public static void main(String[] args) {
//        DistributionLock lock = DistributionLockFactory.getLock(DistributionLockFactory.DLOCK_JEDIS,
//                "distributeLock", 30000);
        DistributionLock lock = DistributionLockFactory.getLock(DistributionLockFactory.DLOCK_LETTUCE,
                "distributeLock", 30000);
        Long start = System.currentTimeMillis();
        System.out.println("process one begin to do something ...., and now is "+ start);
        lock.acquire();
        try {  //do something but spent many time
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("process one end, do this job use "+ (System.currentTimeMillis() - start) + " ms" );
        } finally {
            lock.release();
        }
    }
}
```

```java
package traincamp.redis;

public class UsingBlockLockProcessTwo {
    public static void main(String[] args) {
//        DistributionLock lock = DistributionLockFactory.getLock(DistributionLockFactory.DLOCK_JEDIS,
//                "distributeLock", 30000);
        DistributionLock lock = DistributionLockFactory.getLock(DistributionLockFactory.DLOCK_LETTUCE,
                "distributeLock", 30000);
        Long start = System.currentTimeMillis();
        System.out.println("process two begin to acquire lock ...., and now is "+ start);
        lock.acquire();
        try {  //acquire the lock do something quickly
            long end = System.currentTimeMillis();
            System.out.println("process two begin to do something ...., and now is " + end);
            System.out.println("process two end, spend time is " + (end -start) + "ms");
        } finally {
            lock.release();
        }
    }
}
```

可以看到创建锁是通过工程创建的，可以选择Jedis或者Lettuce的实现方式。先运行UsingBlockLockProcessOne，会先获得锁然后sleep10秒，锁的有效期是30秒。控制台输出：

```
process one begin to do something ...., and now is 1609733877197
process one end, do this job use 10041 ms
```

紧接着运行UsingBlockLockProcessTwo，会看见控制台输出：

```
process two begin to acquire lock ...., and now is 1609733883339
process two begin to do something ...., and now is 1609733887249
process two end, spend time is 3910ms
```

可以看到，进程二确实是阻塞等待线程一完成之后再支持的。

然后，再来看一下，快速失败的方式获取锁，下面是进程一和进程二的代码：

```java
package traincamp.redis;

public class UsingTryLockProcessOne {
    public static void main(String[] args) {
//        DistributionLock lock = DistributionLockFactory.getLock(DistributionLockFactory.DLOCK_JEDIS,
//                "distributeLock", 30000);
        DistributionLock lock = DistributionLockFactory.getLock(DistributionLockFactory.DLOCK_LETTUCE,
                "distributeLock", 30000);
        if(null == lock) {
            System.out.println("get lock fail!!!");
            System.exit(1);
        }
        boolean locked = lock.tryAcquire();
        try{
           if(locked) {
               System.out.println("process one aquery lock.....");
               Long start = System.currentTimeMillis();
               System.out.println("process one begin to do something ...., and now is "+ start);
               try {
                   Thread.sleep(10000);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
               System.out.println("process one end, do this job use "+ (System.currentTimeMillis() - start) + " ms" );
           } else {
               System.out.println("process one tryAcquire lock fail !!!");
           }
        } finally {
           if(locked) {
               boolean released = lock.release();
               if(released) {
                   System.out.println("process one release the distribution lock...");
               } else {
                   System.out.println("process one release the distribution lock fail !!!");
               }
           }
        }
    }
}
```

```java
package traincamp.redis;

public class UsingTryLockProcessTwo {
    public static void main(String[] args) {
//        DistributionLock lock = DistributionLockFactory.getLock(DistributionLockFactory.DLOCK_JEDIS,
//                "distributeLock", 30000);
        DistributionLock lock = DistributionLockFactory.getLock(DistributionLockFactory.DLOCK_LETTUCE,
                "distributeLock", 30000);
        if(null == lock) {
            System.out.println("get lock fail!!!");
            System.exit(1);
        }
        boolean locked = lock.tryAcquire();
        if(locked) {
            try {
                System.out.println("process two aquery lock.....");
                System.out.println("process two begin to do something ...., and now is " + System.currentTimeMillis());
                System.out.println("process two end, now is " + System.currentTimeMillis());
            } finally {
                lock.release();
            }
        } else {
            System.out.println("process two tryAcquire lock fail!!!, and now is " + System.currentTimeMillis());
        }
    }
}
```

同样先执行线程一，控制台输出：

```
process one aquery lock.....
process one begin to do something ...., and now is 1609734184075
process one end, do this job use 10000 ms
process one release the distribution lock...
```

紧接着执行进程二，控制台输出：

```
process two tryAcquire lock fail!!!, and now is 1609734188864
```

可以看到获取锁失败立刻返回，并通知失败。

------

在Redisson中是有分布式锁相关工具类，而且实现的非常全面，包含可以重入，可以自动续期等功能，下面也是使用两个类模拟进程一和进程二。代码如下：

```java
package traincamp.redis;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;

public class UsingRedissonProcessOne {
    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");;
        RedissonClient redisson = Redisson.create(config);
        RLock lock = redisson.getLock("distributeLock");
        long start = System.currentTimeMillis();
        System.out.println("process one begin to do something ...., and now is "+ start);
        lock.lock(30, TimeUnit.SECONDS);
        try {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long end = System.currentTimeMillis();
            System.out.println("end, now is " + end);
            System.out.println("process one end, do this job use "+ (end - start) + " ms" );

        } finally {
            lock.unlock();
        }
        redisson.shutdown();
    }
}
```

```java
package traincamp.redis;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;

public class UsingRedissonProcessTwo {
    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");;
        RedissonClient redisson = Redisson.create(config);
        RLock lock = redisson.getLock("distributeLock");
        Long start = System.currentTimeMillis();
        System.out.println("process two begin to tryAcquire lock ...., and now is "+ start);
        //lock.lock();
        try {
            boolean res = lock.tryLock(1, 10, TimeUnit.SECONDS);
            if (res) {
                try {
                    long end = System.currentTimeMillis();
                    System.out.println("process two begin to do something ...., and now is " + end);
                    System.out.println("process two end, spend time is " + (end -start) + "ms");
                } finally {
                    lock.unlock();
                }
            } else {
                System.out.println("process two tryAcquire lock fail!!!, and now is " + System.currentTimeMillis());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        redisson.shutdown();
    }
}
```

Redisson中如果使用lock方式是阻塞方式，使用tryLock是尝试获取，且可以指定一定时间获取不断则失败的方式。Redisson是采用了Hash方式保存分布式锁的值，并且包含了引用计数，这样在重新加锁时增加引用计数，unlock时减少引用计数，达到可重入的功能。另外，采用一个后台进程作为看门狗，定时检查持有锁的进程，然后完成自动续期。

（2）分布式计数器，模拟减库存

这个部分，依然是使用Jedis和Lettuce进行实现的。和前面分布式锁一样，同样是有计数器接口，实现类，工厂类和测试程序这四部分。先看一下接口，代码如下：

```java
package traincamp.redis;

public interface DistributionCounter {

    /**
     * 设置计数器的制定值，如果数值为负数或者设置失败，则返回false，设置成功返回true
     * @param count
     * @return 设置成功返回true，失败返回false
     */
    boolean setCounter(long count);

    /**
     * 获取计数值
     * @return 当前计数值
     */
    Long get();

    /**
     * 计数加一，如果添加成功则返回计数值，否则返回null
     * @return 当前计数值
     */
    Long increase();

    /**
     * 计数减一，如果计数器相减之后会变为负数则失败，返回null，如果设置失败返回null。如果设置成功返回当前计数
     * @return 如果减少失败返回null，成功返回当前计数
     */
    Long decrease();

    /**
     * 计数增加指定值，如果添加成功则返回true，否则返回false
     * @param change
     * @return 当前计数值
     */
    Long increase(long change);

    /**
     * 计数器减少指定值，如果计数器相减之后会变为负数则失败，返回false，如果设置失败返回false。如果设置成功返回true
     * @param change
     * @return 如果减少失败返回null，成功返回当前计数
     */
    Long decrease(long change);
}
```

有六个方法，setCounter是设定计数值，get是获取当前计数值，increase是增加计数值，可以指定增加的数量，decrease是减少计数值，可以指定减少的数量。考虑会用于库存使用的场景，因此，计数值不能为负值，如果可能出现负值，则该操作失败。

下面分别是Jedis和Lettuce的实现

```java
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
```

```java
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
```

Jedis和Lettuce的实现中为了保证decrease操作的原子性（因为有获取比较操作多个步骤），依然采用了Lua脚本的方式，脚本如下：

```lua
if (redis.call('exists', KEYS[1]) == 0) then
	return nil;
    end;
local counter = redis.call('get', KEYS[1]);
if ((counter - ARGV[1]) >= 0) then 
	return redis.call('decrby', KEYS[1], ARGV[1]);
    end;
return nil;
```

先查看是否存在该键，如果有获取该键的值，然后比较相减后是否会小于0，不小于0，再通过decrby的操作。其他情况都返回nil。

接着通过工厂类解耦用户代码和具体实现，工厂类如下：

```java
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
```

这个部分和分布式锁基本上是一样的，这里就不多说了。

最后测试代码，如下：

```java
package traincamp.redis;

public class DistributionCounterExam {

    private static  String DISTRIBUTION_INVENTORY = "inventory:skuid";
    public static void main(String[] args) {
        DistributionCounter inventory = DistributionCounterFactory.getCounter(DistributionCounterFactory.DCOUNTER_JEDIS,
                DISTRIBUTION_INVENTORY, 50);
//        DistributionCounter inventory = DistributionCounterFactory.getCounter(DistributionCounterFactory.DCOUNTER_LETTUCE,
//                DISTRIBUTION_INVENTORY, 50);
        Long number = inventory.get();
        System.out.println("initial inventory is " + number);
        number = inventory.increase(50);
        System.out.println("after increase 50, current inventory is " + number);
        number = inventory.decrease(10);
        System.out.println("after decrease 10, current inventory is " + number);
        number = inventory.decrease(110);
        if(number != null) {
            System.out.println("decrease 110, current inventory is " + number);
        } else {
            System.out.println("decrease 110, but inventory is not enough!!!!");
        }
    }
}
```

测试代码使用计数器模拟库存，先初始化库存50，然后增加50库存，这时库存为100，紧接着减少库存10，变为90，然后再减少库存110，这时因为库存会变成负数，所以操作失败。下面是控制台的输出：

```
initial inventory is 50
after increase 50, current inventory is 100
after decrease 10, current inventory is 90
decrease 110, but inventory is not enough!!!!
```

注：以上分布式锁和分布式计数器的代码都放在lock_counter目录下，此外还包括pom文件。



**5.（必做）**基于 Redis 的 PubSub 实现订单异步处理

这题主要是实践Redis的PubSub机制。因此订单处理部分都全部简化，主要是有一个订单创建者触发创建一个订单（这里完全简化就只有一个订单号），然后向Redis的一个命名为“order.create”的channel发送这个订单信息（只有一个订单号）。一个该channel的接收者接收这个信息后，模拟处理订单，然后向一个命名为“order.process”的channel发送订单处理完的信息（其实还是这个订单号）。订单创建者在启动时同时订阅“order.process”这个channel因此，会收到信息，并且打印出来。下面是代码的具体说明。

OrderCreatorMain，模拟订单起始创建

```java
package traincamp.redis.pubsub.jedis;

import redis.clients.jedis.Jedis;

import java.util.Scanner;

public class OrderCreatorMain {
    public static void main(String[] args) {
        Jedis client = new Jedis("127.0.0.1",6379);
        new Thread(new OrderReceiver()).start();
        IdGeneratorService idGenerator = new IdGeneratorService();
        System.out.println("开始模拟接收用户订单");
        long orderId = idGenerator.getId();
        client.publish(OrderChannelConstant.CREATE_CHANNEL, String.valueOf(orderId));
        do {
            Scanner inp = new Scanner(System.in);
            String str = inp.next();
            if ("end".equals(str)) {
                client.disconnect();
                System.exit(0);
            }
            orderId = idGenerator.getId();
            client.publish(OrderChannelConstant.CREATE_CHANNEL, String.valueOf(orderId));
        }while(true);
    }
}
```

因为订阅的时候是阻塞的，因此需要在另一个线程中完成订阅，OrderReceiver类实现了Runable接口，并且在run方法中对“order.process”这个channel进行订阅。OrderCreatorMain中，使用另一个Jedis客户端往“order.create”publish消息订单消息（就只有一个订单号）。如果控制台输入“end”这个模拟程序就会退出，如果是其他的输入，就会模拟生成一个起始订单信息异步处理。

OrderReceiver

```java
package traincamp.redis.pubsub.jedis;

import redis.clients.jedis.Jedis;

public class OrderReceiver implements Runnable {
    @Override
    public void run() {
        Jedis client = new Jedis("127.0.0.1",6379);
        try {
            OrderProcessMessageSubscriber processMessageReceiver = new OrderProcessMessageSubscriber();
            client.subscribe(processMessageReceiver, OrderChannelConstant.PROCESS_CHANNEL);
        } finally {
            client.disconnect();
        }
    }
}
```

这个类的作用前面已经说了。可以看到OrderProcessMessageSubscriber这个类是处理接收到消息。

OrderProcessMessageSubscriber代码如下：

```java
package traincamp.redis.pubsub.jedis;

import redis.clients.jedis.JedisPubSub;

public class OrderProcessMessageSubscriber extends JedisPubSub {
    @Override
    public void onMessage(String channel, String message) {
        System.out.println("订单已处理，订单号是" + message);
    }

}
```

OrderProcessMessageSubscriber类继承自JedisPubSub，这里只是接收消息，因此执行实现onMessage这个方法。这里只是简单模拟，因此输出一下接收的信息即可。

接下来看一下，异步订单执行的部分。

OrderProcessMain类

```java
package traincamp.redis.pubsub.jedis;

import redis.clients.jedis.Jedis;

public class OrderProcessMain {
    public static void main(String[] args) {
        Jedis client = new Jedis("127.0.0.1",6379);
        OrderProcessor orderProcessor = new OrderProcessor();
        OrderMessageReceiver listener = new OrderMessageReceiver(orderProcessor);
        try {
            client.subscribe(listener, OrderChannelConstant.CREATE_CHANNEL);
        } finally {
            client.disconnect();
        }

    }
}
```

这类是订单异步执行的启动类，很简单主要就是要监听order.create这个channel发送过来的信息。

OrderMessageReceiver

```java
package traincamp.redis.pubsub.jedis;

import redis.clients.jedis.JedisPubSub;

public class OrderMessageReceiver extends JedisPubSub {

    private OrderProcessor orderProcessor;

    public OrderMessageReceiver(OrderProcessor orderProcessor) {
        this.orderProcessor = orderProcessor;
    }

    @Override
    public void onMessage(String channel, String message) {
        System.out.println("收到订单：" + message + "准备处理");
        orderProcessor.processOrderMessage(message);
    }
}
```

这个类也是继承自JedisPubSub，可以看到收到消息后就将信息文本交由OrderProcessor处理。

```java
package traincamp.redis.pubsub.jedis;

import redis.clients.jedis.Jedis;

public class OrderProcessor {

    private Jedis client;
    public OrderProcessor() {
        this.client = new Jedis("127.0.0.1",6379);
    }

    public void processOrderMessage(String orderMessage) {
        System.out.println("解析订单信息");
        System.out.println("验证库存情况");
        System.out.println("保存订单");
        System.out.println("发送已创建的订单信息");
        client.publish(OrderChannelConstant.PROCESS_CHANNEL, orderMessage );
    }
}
```

OrderProcessor类是具体处理订单的类，因为是极其简单的模拟，因此只是输出几行话代表进行处理。最后将处理后信息发送到“order.process”这个channel中。前面也看到了OrderReceiver中对这个channel进行了订阅，OrderProcessMessageSubscriber会处理收到的信息。

```java
package traincamp.redis.pubsub.jedis;

public class OrderChannelConstant {
    public static final String CREATE_CHANNEL = "order.create";
    public static final String PROCESS_CHANNEL = "order.process";
}
```

OrderChannelConstant定义相关常量。

```java
package traincamp.redis.pubsub.jedis;

public class IdGeneratorService {

    //系统上线时间
    private final long startTime = 1601256017000L;
    //机器Id
    private long workId;
    //序列号
    private long serialNum = 0;

    //得到左移位
    private final long serialNumBits = 20L;
    private final long workIdBits = 2L;

    private final long workIdShift = serialNumBits;
    private final long timestampShift = workIdShift + workIdBits;

    private long lastTimeStamp = 0L;

    private long serialNumMax = -1 ^ (-1L << serialNumBits);

    public IdGeneratorService() {
        this(1L);
    }

    public IdGeneratorService(long workId) {
        this.workId = workId;
    }

    public synchronized long getId() {
        long timestamp = System.currentTimeMillis();
        if( timestamp == lastTimeStamp) {
            serialNum = (serialNum + 1) & serialNumMax;
            if (serialNum == 0) {
                timestamp = waitNextMillis(timestamp);
            }
        } else {
            serialNum = timestamp & 1;
        }
        lastTimeStamp = timestamp;
        return ((timestamp - startTime) << timestampShift)
                | (workId << workIdShift)
                | serialNum;
    }

    private long waitNextMillis(long timestamp) {
        long nowTimestamp = System.currentTimeMillis();
        while ( timestamp >= nowTimestamp) {
            nowTimestamp = System.currentTimeMillis();
        }
        return nowTimestamp;
    }
}
```

IdGeneratorService，订单号生产器。

这个模拟非常简单。按道理应该定义一个订单的实体类，发送订单消息时可以进行序列化（比如json的序列化），收到消息是进行反序列化。因为这里主要是进行redis的PubSub的实践，这个部分就没有实现。

以上代码在pubsub目录下。

