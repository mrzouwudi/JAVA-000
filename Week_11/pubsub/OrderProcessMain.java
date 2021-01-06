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
