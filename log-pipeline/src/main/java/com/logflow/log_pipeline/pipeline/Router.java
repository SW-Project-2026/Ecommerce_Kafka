package com.logflow.log_pipeline.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.logflow.log_pipeline.pipeline.event.EventLogConsumer;
import com.logflow.log_pipeline.pipeline.history.HistoryLogEntity;
import com.logflow.log_pipeline.pipeline.history.HistoryLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Router {

    private static final Logger log = LoggerFactory.getLogger(Router.class);

    private final HistoryLogRepository historyLogRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final EventLogConsumer eventLogConsumer;

    // START_TOPIC 구독 → First_Save_History 저장 → historyId 포함해서 FILTER_TOPIC 전달
    @KafkaListener(topics = "START_TOPIC", groupId = "history-save-group")
    public void saveHistory(String message, Acknowledgment ack) {
        try {
            // First_Save_History 저장
            HistoryLogEntity history = new HistoryLogEntity();
            history.setJsonLog(message);
            historyLogRepository.save(history);

            // event_log 저장
            eventLogConsumer.consume(message);

            // 메시지에 history_id 추가 후 FILTER_TOPIC으로 전달
            JsonNode logNode   = objectMapper.readTree(message);
            ObjectNode newNode = ((ObjectNode) logNode).put("history_id", history.getHistoryId());
            kafkaTemplate.send("FILTER_TOPIC", newNode.toString());

            log.info("history 저장 완료 - historyId: {}", history.getHistoryId());
            ack.acknowledge();

        } catch (Exception e) {
            log.error("history 저장 오류 - message: {} error: {}", message, e.getMessage());
            ack.acknowledge();
        }
    }
}