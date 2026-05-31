package com.logflow.log_pipeline.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    // fluentd에서 받은 원본 행동 로그 토픽
    @Bean
    public NewTopic startTopic() {
        return TopicBuilder.name("START_TOPIC").partitions(3).replicas(3).build();
    }

    // START_TOPIC 데이터를 필터링용으로 전달하는 토픽
    @Bean
    public NewTopic filterTopic() {
        return TopicBuilder.name("FILTER_TOPIC").partitions(3).replicas(3).build();
    }

    // BE에서 생성된 캠페인 정보를 받는 토픽
    @Bean
    public NewTopic campaignTopic() {
        return TopicBuilder.name("CAMPAIGN_TOPIC").partitions(3).replicas(3).build();
    }

    // 필터링 성공 결과 토픽
    @Bean
    public NewTopic filterSuccessTopic() {
        return TopicBuilder.name("FILTER_SUCCESS_TOPIC").partitions(3).replicas(3).build();
    }
}