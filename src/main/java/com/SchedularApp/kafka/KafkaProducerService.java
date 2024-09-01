package com.SchedularApp.kafka;

import java.util.Map;

public interface KafkaProducerService {
    public void sendMessage(String topic, Map<String, String> message);
}
