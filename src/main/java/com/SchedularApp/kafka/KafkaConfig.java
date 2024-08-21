package com.SchedularApp.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic bookingConfirmationEmailTopic() {
        return TopicBuilder.name("booking-confirmation-email")
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "604800000") // 7 days retention
                .build();
    }

    @Bean
    public NewTopic bookingConfirmationWhatsAppTopic() {
        return TopicBuilder.name("booking-confirmation-whatsapp")
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "604800000") // 7 days retention
                .build();
    }

}
