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
