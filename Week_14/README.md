# 作业说明
## Week14 作业题目：
## 周四作业：
2.（必做）思考和设计自定义 MQ 第二个版本或第三个版本，写代码实现其中至少一个功能点，把设计思路和实现代码，提交到 GitHub。

本题实现的是版本二，去掉内存Queue， 设计自定义Queue， 实现消息确认和消费offset
1） 自定义内存Message数组模拟Queue。
2） 使用指针记录当前消息写入位置。
3） 对于每个命名消费者， 用指针记录消费位置。  

设计思路：

一：对Kmq的改造

1）有关数组的部分

1.  使用数组，数组类型KmqMessage[]，而且不进行数组扩展的操作，当数组存满后，不能保存，即保存失败。
2. 保存到数组的消息，不进行删除，即没有清除策略的使用，也就不会使用环形数组。

2）使用指针记录当前消息写入位置。

使用两个下标的指针：

currentWriteIndex ： 这个是指向当前可写的下标，初始值为0，即从小标为0的位置开始写，最大写到下标为数组length-1的位置。

currentCanReadMaxIndex ：这个指向当前可以读到的消息最大下标位置。

这两个下标指针的关系是，在每次事务性操作过程的前后都必须保持这个等式成立：

```
currentWriteIndex = currentCanReadMaxIndex + 1
```

3）对于每个命名消费者， 用指针记录消费位置

使用ConcurrentHashMap<String, AtomicInteger>，来保存每个命名消费者，和其目前应读取信息的偏移量。

为了能保存这个对应关系，在Kmq中添加了一个方法registerConsumer(String consumerId)。当消费者订阅某个主题（topic）时，生成一个UUID字符串作为自己的标识，然后注册到这个主题的Kmq实例中，在Kmq实例中，设置初始偏移量为0，并将消费者标识和初始偏移量保存这个map中。

4）读写消息的改造

读写这块主要是参照ArrayBlockingQueue实现的

1. 考虑到可能有多个生产者发消息，因此往数组中写入消息时使用锁进行控制。当数组满时，不进行任何写入操作，直接返回false。
2. 读消息时，因为每个消费者读取的消息偏移量是不同的，因此，原先拉取信息的方法中增加一个参数，传入消费者的标识。此外，因为拉取信息时不会对数组进行修改操作，只有可能修改每个消费者各自的偏移量，所以，拉取方法poll(String consumerId)不使用锁，如果读取不到直接返回null。而在poll(String consumerId, long timeout)方法中，当没有可读取的消息时可以适当等待指定的毫秒数，是否可以获取新消息，这个等待过程，是通过在写锁的一个condition上await指定时间，如果这个condition因为有新消息写入后发出signalAll而被重新唤醒，就会获得新消息返回，否则，等待时间过去后返回null。这里有几个问题没有考虑，一是消费者的获取消息后手工确认，二是一次拉取获取多个消息，三是没有考虑清除策略对读操作可能引起的并发问题。

5）事务的简单实现

事务的简单实现，就是实现了三个和事务相关的方法：beginTransaction、commitTransaction和abortTransaction。

其中beginTransaction方法就是获取取锁；commitTransaction是判断如果有新加入的消息则先更新currentCanReadMaxIndex，然后给condition发通知（使用signalAll），最后释放锁；abortTransaction是先将事务开启后写入的消息都清除，然后设置currentWriteIndex为事务起始的数值（即currentCanReadMaxIndex+1），最后释放锁。使用事务时不能再用send方法而是使用sendWithoutLock方法。使用事务后，保证只有一个线程写入，相当于保证了消息的有序性。

二：对KmqProducer的改造

对KmqProducer的改造，主要是围绕在事务这个方面。

1. 增加一个boolean类型的属性autoCommit，指明是否在发消息时自动进行提交，为false时必须和事务方法配合使用，或者说使用事务方法时，必须先调用setAutoCommit(false)。调用setAutoCommit(true)后，不需要在使用事务方法，直接调用send方法。
2. send方法中根据当前属性autoCommit值，调用Kmq的send方法或sendWithoutLock方法
3. 增加三个方法beginTransaction(String topic)、commitTransaction(String topic)、abortTransaction(String topic)方法，这个就是对Kmq的三个事务方法的包装。

三：对KmqConsumer的改造

对KmqConsumer的改造主要围绕在consumer在读取数据时需要保留各自的读取的offset，因此需要在订阅主题时给相应的Kmq实例注册consumer标识，在拉取信息将自己的标识传过去。

四：对Demo程序的修改

这里主要是为了演示producer的事务方式发送消息，如下：

```java
producer.setAutoCommit(false);
producer.beginTransaction(topic);
try {
     producer.send(topic, new KmqMessage(null, new Order(100000L + c, System.currentTimeMillis(), "USD2CNY", 6.52d)));
     producer.send(topic, new KmqMessage(null, new Order(100000L + c + 1, System.currentTimeMillis(), "USD2CNY", 6.52d)));
     producer.commitTransaction(topic);
} catch (Exception e){
     producer.abortTransaction(topic);
     e.printStackTrace();
}
```

源代码：

Kmq

```java
package io.kimmking.kmq.core;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public final class Kmq {

    private String topic;

    private int capacity;

    private final KmqMessage[] queue;

    private int currentCanReadMaxIndex = -1;

    private int currentWriteIndex = 0;

    int count;

    final ReentrantLock lock;

    private final Condition notEmpty;

    private ConcurrentHashMap<String, AtomicInteger> consumerOffsetIndicators;

    public Kmq(String topic, int capacity) {
        if(capacity <= 0) {
            throw new IllegalArgumentException();
        }
        this.topic = topic;
        this.capacity = capacity;
        this.queue = new KmqMessage[capacity];
        consumerOffsetIndicators = new ConcurrentHashMap<>();
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
    }

    public void registerConsumer(String consumerId) {
        consumerOffsetIndicators.putIfAbsent(consumerId, new AtomicInteger(0));
    }

    public boolean send(KmqMessage message) {
        checkNotNull(message);
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            boolean success = enqueue(message);
            if(success) {
                currentCanReadMaxIndex++;
                notEmpty.signalAll();
            }
            return success;
        } finally {
            lock.unlock();
        }
    }

    public KmqMessage poll(String consumerId) {
        if (consumerOffsetIndicators.containsKey(consumerId)) {
            AtomicInteger offsetIndicator = consumerOffsetIndicators.get(consumerId);
            int offset = offsetIndicator.get();
            if(offset <= currentCanReadMaxIndex ) {
                return getKmqMessageByOffset(offsetIndicator);
            } else {
                return null;
            }
        }
        return null;
    }

    @SneakyThrows
    public KmqMessage poll(String consumerId, long timeout) {
        if (consumerOffsetIndicators.containsKey(consumerId)) {
            AtomicInteger offsetIndicator = consumerOffsetIndicators.get(consumerId);
            int offset = offsetIndicator.get();
            if(offset <= currentCanReadMaxIndex ) {
                return getKmqMessageByOffset(offsetIndicator);
            } else {
                return poll(offsetIndicator, timeout, TimeUnit.MILLISECONDS);
            }
        }
        return null;
    }

    private KmqMessage getKmqMessageByOffset(AtomicInteger offsetIndicator) {
        int offset = offsetIndicator.getAndIncrement();
        return queue[offset];
    }

    private KmqMessage poll(AtomicInteger offsetIndicator, long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        int offset = offsetIndicator.get();
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (offset > currentCanReadMaxIndex) {
                if (nanos <= 0)
                    return null;
                nanos = notEmpty.awaitNanos(nanos);
            }
            return getKmqMessageByOffset(offsetIndicator);
        } finally {
            lock.unlock();
        }
    }


    private static void checkNotNull(Object v) {
        if (v == null)
            throw new NullPointerException();
    }

    private boolean enqueue(KmqMessage message) {
        final Object[] items = this.queue;
        if( currentWriteIndex < items.length) {
            items[currentWriteIndex] = message;
            currentWriteIndex++;
            count++;
            return true;
        }
        return false;
    }

    public void beginTransaction() {
        lock.lock();
    }

    public void commitTransaction() {
        int newReadMaxIndex = currentWriteIndex - 1;
        if(currentCanReadMaxIndex < newReadMaxIndex) {
            currentCanReadMaxIndex = newReadMaxIndex;
            notEmpty.signalAll();
        }
        lock.unlock();
    }

    public void abortTransaction() {
        for(int i = (currentWriteIndex-1); i > currentCanReadMaxIndex; i--) {
            queue[i] = null;
        }
        currentWriteIndex = currentCanReadMaxIndex + 1;
        lock.unlock();
    }

    public boolean sendWithoutLock(KmqMessage message) {
        checkNotNull(message);
        return enqueue(message);
    }
}
```

具体说明在前基本都说了，这里不再赘述，以下同。

KmqProducer类：

```java
package io.kimmking.kmq.core;

public class KmqProducer {

    private KmqBroker broker;

    private boolean autoCommit = true;

    public KmqProducer(KmqBroker broker) {
        this.broker = broker;
    }

    public boolean send(String topic, KmqMessage message) {
        Kmq kmq = this.broker.findKmq(topic);
        if (null == kmq) throw new RuntimeException("Topic[" + topic + "] doesn't exist.");
        return autoCommit ? kmq.send(message) : kmq.sendWithoutLock(message);
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public void beginTransaction(String topic) {
        Kmq kmq = this.broker.findKmq(topic);
        if (null == kmq) throw new RuntimeException("Topic[" + topic + "] doesn't exist.");
        kmq.beginTransaction();
    }

    public void commitTransaction(String topic) {
        Kmq kmq = this.broker.findKmq(topic);
        if (null == kmq) throw new RuntimeException("Topic[" + topic + "] doesn't exist.");
        kmq.commitTransaction();
    }

    public void abortTransaction(String topic) {
        Kmq kmq = this.broker.findKmq(topic);
        if (null == kmq) throw new RuntimeException("Topic[" + topic + "] doesn't exist.");
        kmq.abortTransaction();
    }
}
```

KmqConsumer类：

```java
package io.kimmking.kmq.core;

import java.util.UUID;

public class KmqConsumer<T> {

    private final KmqBroker broker;

    private Kmq kmq;

    private String consumerId;

    public KmqConsumer(KmqBroker broker) {
        this.broker = broker;
    }

    public void subscribe(String topic) {
        this.kmq = this.broker.findKmq(topic);
        if (null == kmq) throw new RuntimeException("Topic[" + topic + "] doesn't exist.");
        consumerId = UUID.randomUUID().toString();
        kmq.registerConsumer(consumerId);
    }

    public KmqMessage<T> poll(long timeout) {
        return kmq.poll(consumerId, timeout);
    }

}
```

KmqDemo类

```java
package io.kimmking.kmq.demo;

import io.kimmking.kmq.core.KmqBroker;
import io.kimmking.kmq.core.KmqConsumer;
import io.kimmking.kmq.core.KmqMessage;
import io.kimmking.kmq.core.KmqProducer;

import lombok.SneakyThrows;

public class KmqDemo {

    @SneakyThrows
    public static void main(String[] args) {

        String topic = "kk.test";
        KmqBroker broker = new KmqBroker();
        broker.createTopic(topic);

        KmqConsumer consumer = broker.createConsumer();
        consumer.subscribe(topic);
        final boolean[] flag = new boolean[1];
        flag[0] = true;
        new Thread(() -> {
            while (flag[0]) {
                KmqMessage<Order> message = consumer.poll(100);
                if(null != message) {
                    System.out.println(message.getBody());
                }
            }
            System.out.println("程序退出。");
        }).start();

        KmqProducer producer = broker.createProducer();
        for (int i = 0; i < 1000; i++) {
            Order order = new Order(1000L + i, System.currentTimeMillis(), "USD2CNY", 6.51d);
            producer.send(topic, new KmqMessage(null, order));
        }
        Thread.sleep(500);
        System.out.println("点击任何键，发送两条消息；点击q或e，退出程序。");
        producer.setAutoCommit(false);
        while (true) {
            char c = (char) System.in.read();
            if(c > 20) {
                System.out.println(c);
                //producer.send(topic, new KmqMessage(null, new Order(100000L + c, System.currentTimeMillis(), "USD2CNY", 6.52d)));
                producer.beginTransaction(topic);
                try {
                    producer.send(topic, new KmqMessage(null, new Order(100000L + c, System.currentTimeMillis(), "USD2CNY", 6.52d)));
                    producer.send(topic, new KmqMessage(null, new Order(100000L + c + 1, System.currentTimeMillis(), "USD2CNY", 6.52d)));
                    producer.commitTransaction(topic);
                } catch (Exception e){
                    producer.abortTransaction(topic);
                    e.printStackTrace();
                }
            }

            if( c == 'q' || c == 'e') break;
        }

        flag[0] = false;

    }
}
```

执行KmqDemo，可以看见发的1000条信息正常接收，在控制发“1”并回车，可以看见接收到两条消息（为了演示事务，发送了两条）。

注相关代码放在kmq目录下，其中子目录core下是文件Kmq.java、KmqBroker.java、KmqConsumer.java、KmqMessage.java和KmqProducer.java；子目录demo下是KmqDemo.java和Order.java