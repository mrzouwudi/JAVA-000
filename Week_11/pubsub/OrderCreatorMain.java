package traincamp.redis.pubsub.jedis;

import redis.clients.jedis.Jedis;

import java.util.Scanner;

public class OrderCreatorMain {
    public static void main(String[] args) {
        Jedis client = new Jedis("127.0.0.1",6379);
        new Thread(new OrderReceiver()).start();
        IdGeneratorService idGenerator = new IdGeneratorService();
        System.out.println("开始模拟接收用户订单");
        long orderId = idGenerator.getId();
        client.publish(OrderChannelConstant.CREATE_CHANNEL, String.valueOf(orderId));
        do {
            Scanner inp = new Scanner(System.in);
            String str = inp.next();
            if ("end".equals(str)) {
                client.disconnect();
                System.exit(0);
            }
            orderId = idGenerator.getId();
            client.publish(OrderChannelConstant.CREATE_CHANNEL, String.valueOf(orderId));
        }while(true);
    }
}
