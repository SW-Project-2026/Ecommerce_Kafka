package com.logflow.log_pipeline.campaign;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CampaignConsumer {

    private static final Logger log = LoggerFactory.getLogger(CampaignConsumer.class);

    private final CampaignRepository campaignRepository;
    private final CampaignFilterRepository campaignFilterRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = "CAMPAIGN_TOPIC", groupId = "log-pipeline-group")
    public void consume(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String action = node.path("action").asText("UPSERT");

            if ("STATUS_UPDATE".equals(action)) {
                handleStatusUpdate(node);
            } else if ("DELETE".equals(action)) {
                handleDelete(node);
            } else {
                handleUpsert(node);
            }
        } catch (Exception e) {
            log.error("CAMPAIGN_TOPIC 처리 오류: {}", e.getMessage());
        }
    }

    private void handleUpsert(JsonNode node) {
        Long campaignId = node.path("campaignId").asLong();

        CampaignEntity campaign = campaignRepository.findById(campaignId)
            .orElse(new CampaignEntity());

        campaign.setCampaignId(campaignId);
        campaign.setCollectionType(node.path("collectionType").asText());
        campaign.setFilterLogicalOperator(node.path("filterLogicalOperator").asText());
        campaign.setCreatedAt(LocalDateTime.now());

        String batchCycle = node.path("batchCycle").asText("");
        campaign.setBatchCycle(batchCycle.isEmpty() ? null : batchCycle);

        String batchTime = node.path("batchTime").asText("");
        campaign.setBatchTime(batchTime.isEmpty() ? null : batchTime);

        String batchDayOfWeek = node.path("batchDayOfWeek").asText("");
        campaign.setBatchDayOfWeek(batchDayOfWeek.isEmpty() ? null : batchDayOfWeek);

        int batchDayOfMonth = node.path("batchDayOfMonth").asInt(0);
        campaign.setBatchDayOfMonth(batchDayOfMonth == 0 ? null : batchDayOfMonth);

        campaign.setStatus(node.path("status").asText("IN_PROGRESS"));

        campaignRepository.save(campaign);

        campaignFilterRepository.deleteByCampaignId(campaignId);
        JsonNode filtersNode = node.path("filters");
        if (filtersNode.isArray()) {
            for (JsonNode f : filtersNode) {
                CampaignFilterEntity filter = new CampaignFilterEntity();
                filter.setAll(
                    campaignId,
                    f.path("eventName").asText(),
                    f.path("eventFieldName").asText(),
                    f.path("fieldType").asText(),
                    f.path("operator").asText(),
                    f.path("value").asText(),
                    f.path("periodDays").asInt(7)
                );
                campaignFilterRepository.save(filter);
            }
        }

        log.info("캠페인 UPSERT 완료 - campaignId: {} status: {}", campaignId, campaign.getStatus());
    }

    private void handleStatusUpdate(JsonNode node) {
        Long campaignId = node.path("campaignId").asLong();
        String status   = node.path("status").asText();

        campaignRepository.findById(campaignId).ifPresentOrElse(
            campaign -> {
                campaign.setStatus(status);
                campaignRepository.save(campaign);
                log.info("캠페인 상태 변경 완료 - campaignId: {} status: {}", campaignId, status);
            },
            () -> log.warn("상태 변경 대상 캠페인 없음 - campaignId: {}", campaignId)
        );
    }

    private void handleDelete(JsonNode node) {
        Long campaignId = node.path("campaignId").asLong();

        campaignFilterRepository.deleteByCampaignId(campaignId);
        campaignRepository.deleteById(campaignId);

        log.info("캠페인 DELETE 완료 - campaignId: {}", campaignId);
    }
}