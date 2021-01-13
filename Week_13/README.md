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

## 周六作业：

**1.（必做）**搭建一个 3 节点 Kafka 集群，测试功能和性能；实现 spring kafka 下对 kafka 集群的操作，将代码提交到 github。

（a）准备

搭建的环境是在windows下

在kafka官网下载（http://kafka.apache.org/downloads）下载二进制包 [kafka_2.13-2.7.0.tgz](https://www.apache.org/dyn/closer.cgi?path=/kafka/2.7.0/kafka_2.13-2.7.0.tgz) 。

下载后放到合适位置解压。

（b）配置

首先配置zookeeper，主要是指定dataDir和dataLogDir

```
dataDir=F:\\software\\kafka_2.13-2.7.0\\data\\zoo_data
dataLogDir=F:\\software\\kafka_2.13-2.7.0\\data\\zoo_log
clientPort=2181
maxClientCnxns=0
admin.enableServer=false
```

添加三个kafka的配置文件：kafka9001.properties、kafka9002.properties、kafka9003.properties，内容如下：

```
broker.id=1
num.network.threads=3
num.io.threads=8
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600
log.dirs=F:\\software\\kafka_2.13-2.7.0\\data\\kafka_log1
num.partitions=1
num.recovery.threads.per.data.dir=1
offsets.topic.replication.factor=1
transaction.state.log.replication.factor=1
transaction.state.log.min.isr=1
log.retention.hours=168
log.segment.bytes=1073741824
log.retention.check.interval.ms=300000
zookeeper.connect=localhost:2181
zookeeper.connection.timeout.ms=6000000
delete.topic.enable=true
group.initial.rebalance.delay.ms=0
message.max.bytes=5000000
replica.fetch.max.bytes=5000000
listeners=PLAINTEXT://localhost:9001
broker.list=localhost:9001,localhost:9002,localhost:9003
```

```
broker.id=2
num.network.threads=3
num.io.threads=8
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600
log.dirs=F:\\software\\kafka_2.13-2.7.0\\data\\kafka_log2
num.partitions=1
num.recovery.threads.per.data.dir=1
offsets.topic.replication.factor=1
transaction.state.log.replication.factor=1
transaction.state.log.min.isr=1
log.retention.hours=168
log.segment.bytes=1073741824
log.retention.check.interval.ms=300000
zookeeper.connect=localhost:2181
zookeeper.connection.timeout.ms=6000000
delete.topic.enable=true
group.initial.rebalance.delay.ms=0
message.max.bytes=5000000
replica.fetch.max.bytes=5000000
listeners=PLAINTEXT://localhost:9002
broker.list=localhost:9001,localhost:9002,localhost:9003
```

```
broker.id=3
num.network.threads=3
num.io.threads=8
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600
log.dirs=F:\\software\\kafka_2.13-2.7.0\\data\\kafka_log3
num.partitions=1
num.recovery.threads.per.data.dir=1
offsets.topic.replication.factor=1
transaction.state.log.replication.factor=1
transaction.state.log.min.isr=1
log.retention.hours=168
log.segment.bytes=1073741824
log.retention.check.interval.ms=300000
zookeeper.connect=localhost:2181
zookeeper.connection.timeout.ms=6000000
delete.topic.enable=true
group.initial.rebalance.delay.ms=0
message.max.bytes=5000000
replica.fetch.max.bytes=5000000
listeners=PLAINTEXT://localhost:9003
broker.list=localhost:9001,localhost:9002,localhost:9003
```

（c）运行

先运行zookeeper，进入kafka安装目录下的bin\windows目录下，执行

```
zookeeper-server-start.bat ../../config/zookeeper.properties
```

然后在依次在不同控制台终端执行

```
kafka-server-start.bat kafka9001.properties
kafka-server-start.bat kafka9002.properties
kafka-server-start.bat kafka9003.properties
```

这里偷了个懒，直接把三个kafka配置文件放在bin\windows目录下

（d）测试功能和性能

开启一个控制台，执行

```
kafka-topics.bat --zookeeper localhost:2181 --create --topic test32 --partitions 3 --replication-factor 2
kafka-console-producer.bat --bootstrap-server localhost:9003 --topic test32
```

然后打开三个控制台依次执行

```
kafka-console-consumer.bat --bootstrap-server localhost:9001 --topic test32 --from-beginning
kafka-console-consumer.bat --bootstrap-server localhost:9002 --topic test32 --from-beginning
kafka-console-consumer.bat --bootstrap-server localhost:9003 --topic test32 --from-beginning
```

然后在第一个producer的控制输入字符串并回车，这时观察三个consumer的控制台都有输出，正是producer发出的字符串

打开控制台执行

```
kafka-producer-perf-test.bat --topic test32 --num-records 100000 --record-size 1000 --throughput 2000 --producer-props bootstrap.servers=localhost:9002
kafka-consumer-perf-test.bat --bootstrap-server localhost:9002 --topic test32 --fetch-size 1048576 --messages 100000 --threads 1
```

进行性能测试。

（e）实现 spring kafka 下对 kafka 集群的操作

【1】producer的说明

先创建一个springboot工程，选择spring-kafka模块。这时pom中包含了下面的依赖

```
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.8.2</version>
        </dependency>
```

主要是spring-kafka的依赖，另外，为了对消息进行json序列化使用了GSON。

application.yml配置文件如下：

```yaml
spring:
  kafka:
    bootstrap-servers: http://127.0.0.1:9001,http://127.0.0.1:9002,http://127.0.0.1:9003
    producer:
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      batch-size: 16384
      retries: 0
      buffer-memory: 33554432
server:
  port: 8080
```

生产者类KafkaProducer如下：

```java
package traincamp.mq.kafka;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private Gson gson = new GsonBuilder().create();

    //发送消息方法
    public void send() {
        Message message = new Message();
        message.setId(System.currentTimeMillis());
        message.setMsg(UUID.randomUUID().toString());
        message.setSendTime(new Date());
        log.info("+++++++++++++++++++++  message = {}", gson.toJson(message));
        kafkaTemplate.send("topic.test",gson.toJson(message));
    }
}
```

消息的pojo类

```java
package traincamp.mq.kafka;

import lombok.Data;

import java.util.Date;

@Data
public class Message {
    private Long id;    //id
    private String msg; //消息
    private Date sendTime;  //时间戳
}
```

测试类

```java
package traincamp.mq.kafka;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class KafkaProducerTest {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    public void kafkaProducer(){
        this.kafkaProducer.send();
    }

    //@Test
    public void contextLoads() {
    }

}
```

可以直接运行测试进行消息发送。

【1】consumer的说明

先创建一个springboot工程，选择spring-kafka模块。这时pom中包含了下面的依赖

```
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.8.2</version>
        </dependency>
```

主要是spring-kafka的依赖，另外，为了对消息进行json序列化使用了GSON。

application.yml配置文件如下：

```yaml
server:
  port: 8099
spring:
  kafka:
    bootstrap-servers: http://127.0.0.1:9001,http://127.0.0.1:9002,http://127.0.0.1:9003
    consumer:
      group-id: test-consumer-group
      auto-offset-reset: earliest
      enable-auto-commit: true
      auto-commit-interval: 20000
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```

可以看到consumer和producer的配置上还是有比较明显区别的。

消费者类KafkaConsumer如下：

```java
package traincamp.mq.kafkaconsumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class KafkaConsumer {

    @KafkaListener(topics = {"topic.test"})
    public void consumer(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            log.info("----------------- record =" + record);
            log.info("------------------ message =" + message);
        }
    }
}
```

注意注解 @KafkaListener，其中topic的值一定和producer发送的topic对应上

【3】执行

先将consumer的springboot启动起来。然后将producer的单元测试类KafkaProducerTest执行，可以在consumer的控制台观察以下输出：

```
2021-01-13 21:18:44.793  INFO 18456 --- [ntainer#0-0-C-1] t.mq.kafkaconsumer.KafkaConsumer         : ----------------- record =ConsumerRecord(topic = topic.test, partition = 0, leaderEpoch = 0, offset = 1, CreateTime = 1610543924764, serialized key size = -1, serialized value size = 102, headers = RecordHeaders(headers = [], isReadOnly = false), key = null, value = {"id":1610543923922,"msg":"3df39b85-8902-4b9b-8e67-6b294e6cb19b","sendTime":"Jan 13, 2021 9:18:44 PM"})
2021-01-13 21:18:44.793  INFO 18456 --- [ntainer#0-0-C-1] t.mq.kafkaconsumer.KafkaConsumer         : ------------------ message ={"id":1610543923922,"msg":"3df39b85-8902-4b9b-8e67-6b294e6cb19b","sendTime":"Jan 13, 2021 9:18:44 PM"}
```

说明发送接收成功。

注：producer的工程代码在kafka-producer目录下，consumer的工程代码在kafka-consumer目录下。

最后注明：代码部分的实现参考了https://blog.csdn.net/jucks2611/article/details/80817476