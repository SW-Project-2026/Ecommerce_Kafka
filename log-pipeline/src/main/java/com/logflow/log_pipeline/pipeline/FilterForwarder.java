package com.logflow.log_pipeline.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class FilterForwarder {

    private static final Logger log = LoggerFactory.getLogger(FilterForwarder.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    public FilterForwarder(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "START_TOPIC", groupId = "filter-forward-group")
    public void forwardToFilter(String message, Acknowledgment ack) {
        try {
            kafkaTemplate.send("FILTER_TOPIC", message);
            log.info("FILTER_TOPIC 전송 완료");
            ack.acknowledge();

        } catch (Exception e) {
            log.error("FILTER_TOPIC 전송 오류 - message: {} error: {}", message, e.getMessage());
            ack.acknowledge();
        }
    }
}