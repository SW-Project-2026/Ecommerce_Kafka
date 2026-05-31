package com.logflow.log_pipeline.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FilterForwarder {

    private static final Logger log = LoggerFactory.getLogger(FilterForwarder.class);

    private final FilterMatchingService filterMatchingService;
    private final ObjectMapper objectMapper;

    // FILTER_TOPIC 구독 → history_id 추출 → TRIGGERED 캠페인 필터 매칭 수행
    @KafkaListener(topics = "FILTER_TOPIC", groupId = "filter-match-group")
    public void processFilter(String message, Acknowledgment ack) {
        try {
            JsonNode logNode = objectMapper.readTree(message);
            Long historyId   = logNode.get("history_id").asLong();

            // 실시간 캠페인 필터 매칭 수행
            filterMatchingService.match(historyId, logNode, message);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("FILTER_TOPIC 처리 오류 - error: {}", e.getMessage());
            ack.acknowledge();
        }
    }
}