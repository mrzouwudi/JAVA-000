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
