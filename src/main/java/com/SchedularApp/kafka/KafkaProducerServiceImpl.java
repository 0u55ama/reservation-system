package com.SchedularApp.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KafkaProducerServiceImpl implements KafkaProducerService{

    @Autowired
    private KafkaTemplate<String, Map<String, String>> kafkaTemplate;

    public void sendMessage(String topic, Map<String, String> message) {
        kafkaTemplate.send(topic, message);
    }
}
