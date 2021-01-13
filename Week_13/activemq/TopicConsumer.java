package traincamp.mq.activemq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;

import javax.jms.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class TopicConsumer {
    public static void main(String[] args) {
        try {
            // 创建连接和会话
            //ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://127.0.0.1:62000");
            ActiveMQConnection conn = (ActiveMQConnection) factory.createConnection();
            conn.start();
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination destination = new ActiveMQTopic("test.topic");

            // 创建消费者
            MessageConsumer consumer = session.createConsumer(destination);
            final AtomicInteger count = new AtomicInteger(0);
            MessageListener listener = new MessageListener() {
                public void onMessage(Message message) {
                    try {
                        System.out.println(count.incrementAndGet() + " => receive from " + destination.toString() + ": " + message);
                        message.acknowledge(); // 前面所有未被确认的消息全部都确认。

                    } catch (Exception e) {
                        e.printStackTrace(); // 不要吞任何这里的异常，
                    }
                }
            };
            // 绑定消息监听器
            consumer.setMessageListener(listener);

            System.out.println("topic consumer begin to work: if input 'end' then will exit!");
            String inputStr = null;
            do {
                Scanner scanner = new Scanner(System.in);
                inputStr = scanner.next();
            } while(!"end".equals(inputStr));
            // 程序退出时进行处理
            session.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
