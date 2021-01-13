# 作业说明
## Week13 作业题目：
## 周四作业：
1.（必做）搭建 ActiveMQ 服务，基于 JMS，写代码分别实现对于 queue 和 topic 的消息生产和消费，代码提交到 github。

（1）环境说明：

操作系统：Windows 10

JDK：jdk 1.8

（2）搭建 ActiveMQ 服务

本人实践了两种方式：一是直接下载二进制包，启动服务；二是使用嵌入式ActiveMQ服务。下面依次说明：

【a】：在官网下载页（http://activemq.apache.org/components/classic/download/）下载最新的windows下的[apache-activemq-5.16.0-bin.zip](http://www.apache.org/dyn/closer.cgi?filename=/activemq/5.16.0/apache-activemq-5.16.0-bin.zip&action=download) zip包，下载好后在合适的目录下解压，然后进入目录 bin\win64\，运行activemq.bat，启动服务。

此时可以访问http://localhost:8161访问管理页面，使用确实用户名和密码（admin/admin）进入管理界面，可以查看queue和topic的情况。通过tcp://localhost:61616来连接消息服务器

【b】：使用嵌入式ActiveMQ服务。建立一个Maven工程，需要在POM文件中引入下面的依赖：

```
        <dependency>
            <groupId>org.apache.activemq</groupId>
            <artifactId>activemq-all</artifactId>
            <version>5.16.0</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.11.2</version>
        </dependency>
```

如果没有jackson-databind会报错。

下面是嵌入的ActiveMQ的代码：

```java
package traincamp.mq.activemq;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.jmx.ManagementContext;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;

public class ActiveMqServer {
    public static void main(String[] args) {
        try {
            BrokerService brokerService = new BrokerService();
            brokerService.setBrokerName("EmbedMQ");
            brokerService.addConnector("tcp://localhost:62000");
            brokerService.setManagementContext(new ManagementContext());
            brokerService.start();

            String inputStr = null;
            do {
                Scanner scanner = new Scanner(System.in);
                inputStr = scanner.next();
            } while(!"end".equals(inputStr));
            brokerService.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

代码分为两部分，前面是嵌入一个ActiveMQ，可以通过tcp://localhost:62000来连接消息服务器。下面一个部分是为了进行控制的代码，和嵌入式的ActiveMQ太多直接关系。

（3）实现对于 queue的消息生产和消费

注：queue和topic的消息生产者（/消费者）的实现代码和接近，为了分开说明queue和topic因此保留代码上的冗余。

对于queue的消息生产者的代码，如下:

```java
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
```

对于queue的消息消费者的代码，如下:

```java
package traincamp.mq.activemq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

import javax.jms.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class QueueConsumer {
    public static void main(String[] args) {
        try {
            // 创建连接和会话
            //ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://127.0.0.1:62000");
            ActiveMQConnection conn = (ActiveMQConnection) factory.createConnection();
            conn.start();
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination destination = new ActiveMQQueue("test.queue");

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

            System.out.println("queue consumer begin to work: if input 'end' then will exit!");
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
```

这里，生产者和消费者都是连的嵌入的ActiveMQ上的，消费者可以启动多个进程实例，但是只能有一个实例接收到消息进行消费。可以从控制台输出看。而且，如果有多个消费者实例，接收消息的实例并不是都是同一个实例，会发送到不同的实例上，同样可以看不同消费者的实例的控制台输出，体现了这点。

（4）实现对于 topic的消息生产和消费

对于topic的消息生产者的代码，如下:

```java
package traincamp.mq.activemq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;

import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Scanner;

public class TopicProductor {
    public static void main(String[] args) {
        try {
            // 创建连接和会话
            //ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://127.0.0.1:62000");
            ActiveMQConnection conn = (ActiveMQConnection) factory.createConnection();
            conn.start();
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination destination = new ActiveMQTopic("test.topic");
            // 创建生产者
            MessageProducer producer = session.createProducer(destination);

            System.out.println("Topic productor begin to work: input some words to send, input 'end' to exit!");
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
```

对于topic的消息消费者的代码，如下:

```java
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
```

这里，生产者和消费者都是连的嵌入的ActiveMQ上的，消费者可以启动多个进程实例，所有的消费者实例都可以收到发出的消息，这点可以从各消费者的控制台输出观察到。

注：以上代码和POM文件放在activemq目录下。