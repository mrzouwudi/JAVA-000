package traincamp.redis.pubsub.jedis;

import redis.clients.jedis.Jedis;

public class OrderProcessor {

    private Jedis client;
    public OrderProcessor() {
        this.client = new Jedis("127.0.0.1",6379);
    }

    public void processOrderMessage(String orderMessage) {
        System.out.println("解析订单信息");
        System.out.println("验证库存情况");
        System.out.println("保存订单");
        System.out.println("发送已创建的订单信息");
        client.publish(OrderChannelConstant.PROCESS_CHANNEL, orderMessage );
    }
}
