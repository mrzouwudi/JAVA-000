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
