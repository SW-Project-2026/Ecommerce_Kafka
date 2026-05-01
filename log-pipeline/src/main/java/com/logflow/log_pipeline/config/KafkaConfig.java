package com.logflow.log_pipeline.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic startTopic() {
        return TopicBuilder.name("START_TOPIC")
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic filterTopic() {
        return TopicBuilder.name("FILTER_TOPIC")
                .partitions(3)
                .replicas(3)
                .build();
    }
}