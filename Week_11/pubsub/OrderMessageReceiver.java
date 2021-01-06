package traincamp.redis.pubsub.jedis;

import redis.clients.jedis.JedisPubSub;

public class OrderMessageReceiver extends JedisPubSub {

    private OrderProcessor orderProcessor;

    public OrderMessageReceiver(OrderProcessor orderProcessor) {
        this.orderProcessor = orderProcessor;
    }

    @Override
    public void onMessage(String channel, String message) {
        System.out.println("收到订单：" + message + "准备处理");
        orderProcessor.processOrderMessage(message);
    }
}
