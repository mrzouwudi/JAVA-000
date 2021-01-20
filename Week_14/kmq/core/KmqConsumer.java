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
