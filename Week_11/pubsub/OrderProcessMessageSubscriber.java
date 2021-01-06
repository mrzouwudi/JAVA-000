package traincamp.redis.pubsub.jedis;

import redis.clients.jedis.JedisPubSub;

public class OrderProcessMessageSubscriber extends JedisPubSub {
    @Override
    public void onMessage(String channel, String message) {
        System.out.println("订单已处理，订单号是" + message);
    }

}
