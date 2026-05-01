package com.logflow.log_pipeline.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.logflow.log_pipeline.pipeline.history.HistoryLogEntity;
import com.logflow.log_pipeline.pipeline.history.HistoryLogRepository;

@Service
public class Router {

    private static final Logger log = LoggerFactory.getLogger(Router.class);

    private final HistoryLogRepository historyLogRepository;

    public Router(HistoryLogRepository historyLogRepository) {
        this.historyLogRepository = historyLogRepository;
    }

    @KafkaListener(topics = "START_TOPIC", groupId = "history-save-group")
    public void saveHistory(String message, Acknowledgment ack) {
        try {
            HistoryLogEntity history = new HistoryLogEntity();
            history.setJsonLog(message);
            historyLogRepository.save(history);

            log.info("history 저장 완료 - historyId: {}", history.getHistoryId());
            ack.acknowledge();

        } catch (Exception e) {
            log.error("history 저장 오류 - message: {} error: {}", message, e.getMessage());
            ack.acknowledge();
        }
    }
}