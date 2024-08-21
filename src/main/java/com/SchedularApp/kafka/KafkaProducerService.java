package com.SchedularApp.kafka;

public interface KafkaProducerService {
    public void sendMessage(String topic, Object message);
}
