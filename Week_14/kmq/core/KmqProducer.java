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
