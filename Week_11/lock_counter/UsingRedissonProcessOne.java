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
