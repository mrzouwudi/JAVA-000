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
