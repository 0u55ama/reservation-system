package com.SchedularApp.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;


@EnableKafka
@Configuration
public class KafkaProducerConfig {

    @Bean
    public KafkaTemplate<String, Map<String, String>> kafkaTemplate() {
        KafkaTemplate<String, Map<String, String>> kafkaTemplate = new KafkaTemplate<>(producerFactory());
        kafkaTemplate.setTransactionIdPrefix("booking-tx-");  // Transactional ID
        return kafkaTemplate;
//        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String,  Map<String, String>> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "booking-tx-");
        return new DefaultKafkaProducerFactory<>(configProps);
    }
}