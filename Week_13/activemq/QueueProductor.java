package traincamp.mq.activemq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Scanner;

public class QueueProductor {
    public static void main(String[] args) {
        try {
            // 创建连接和会话
            //ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://127.0.0.1:62000");
            ActiveMQConnection conn = (ActiveMQConnection) factory.createConnection();
            conn.start();
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination destination = new ActiveMQQueue("test.queue");
            // 创建生产者
            MessageProducer producer = session.createProducer(destination);

            System.out.println("productor begin to work: input some words to send, input 'end' to exit!");
            while (true) {
                Scanner scanner = new Scanner(System.in);
                String inputStr =scanner.next();
                if("end".equals(inputStr)) {
                    break;
                }
                //生产一个消息，发送到ActiveMQ
                TextMessage message = session.createTextMessage("message:" + inputStr);
                producer.send(message);
            }

            session.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
