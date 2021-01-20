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
