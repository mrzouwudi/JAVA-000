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
