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
