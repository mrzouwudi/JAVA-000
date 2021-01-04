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
