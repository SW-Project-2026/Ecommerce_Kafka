package com.logflow.log_pipeline.campaign;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CampaignConsumer {

    private static final Logger log = LoggerFactory.getLogger(CampaignConsumer.class);

    private final CampaignRepository campaignRepository;
    private final CampaignFilterRepository campaignFilterRepository;
    private final ObjectMapper objectMapper;

    // CAMPAIGN_TOPIC 구독 → Campaigns, Campaign_Filters 테이블 저장
    @KafkaListener(topics = "CAMPAIGN_TOPIC", groupId = "campaign-save-group")
    public void saveCampaign(String message, Acknowledgment ack) {
        try {
            JsonNode root = objectMapper.readTree(message);

            Long campaignId = root.get("campaignId").asLong();

            // 캠페인 저장 (upsert: id 존재 시 덮어쓰기)
            CampaignEntity campaign = campaignRepository.findById(campaignId)
                .orElse(new CampaignEntity());

            campaign.setCampaignId(campaignId);
            campaign.setCollectionType(root.path("collectionType").asText());
            campaign.setFilterLogicalOperator(root.path("filterLogicalOperator").asText("AND"));

            // createdAt 파싱
            String createdAtStr = root.path("createdAt").asText();
            if (!createdAtStr.isEmpty()) {
                try {
                    campaign.setCreatedAt(LocalDateTime.parse(createdAtStr));
                } catch (Exception e) {
                    campaign.setCreatedAt(LocalDateTime.now());
                }
            }

            // batchCycle: DAILY / WEEKLY / MONTHLY
            String batchCycle = root.path("batchCycle").asText("");
            campaign.setBatchCycle(batchCycle.isEmpty() ? null : batchCycle);

            // batchTime: HH:mm
            String batchTime = root.path("batchTime").asText("");
            campaign.setBatchTime(batchTime.isEmpty() ? null : batchTime);

            // batchDayOfWeek: MONDAY ~ SUNDAY
            String batchDayOfWeek = root.path("batchDayOfWeek").asText("");
            campaign.setBatchDayOfWeek(batchDayOfWeek.isEmpty() ? null : batchDayOfWeek);

            // batchDayOfMonth: 1~31
            JsonNode dayOfMonthNode = root.path("batchDayOfMonth");
            campaign.setBatchDayOfMonth(dayOfMonthNode.isNull() || dayOfMonthNode.asInt() == 0
                ? null : dayOfMonthNode.asInt());

            campaignRepository.save(campaign);

            // 기존 필터 삭제 후 새로 저장
            campaignFilterRepository.deleteByCampaignId(campaignId);

            JsonNode filtersNode = root.path("filters");
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

            log.info("CAMPAIGN_TOPIC 저장 완료 - campaignId: {}", campaignId);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("CAMPAIGN_TOPIC 처리 오류 - error: {}", e.getMessage());
            ack.acknowledge();
        }
    }
}