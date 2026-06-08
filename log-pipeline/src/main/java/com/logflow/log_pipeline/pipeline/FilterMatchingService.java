package com.logflow.log_pipeline.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logflow.log_pipeline.be.CampaignBeEntity;
import com.logflow.log_pipeline.be.CampaignBeFilterEntity;
import com.logflow.log_pipeline.be.CampaignBeFilterRepository;
import com.logflow.log_pipeline.be.CampaignBeRepository;
import com.logflow.log_pipeline.pipeline.history.FilterFailEntity;
import com.logflow.log_pipeline.pipeline.history.FilterFailRepository;
import com.logflow.log_pipeline.pipeline.history.FilterSuccessEntity;
import com.logflow.log_pipeline.pipeline.history.FilterSuccessRepository;
import com.logflow.log_pipeline.pipeline.history.HistoryLogEntity;
import com.logflow.log_pipeline.pipeline.history.HistoryLogRepository;
import com.logflow.log_pipeline.pipeline.sse.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilterMatchingService {

    private static final Logger log = LoggerFactory.getLogger(FilterMatchingService.class);

    private final FilterSuccessRepository filterSuccessRepository;
    private final FilterFailRepository filterFailRepository;
    private final HistoryLogRepository historyLogRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final SseEmitterService sseEmitterService;
    private final CampaignBeRepository campaignBeRepository;
    private final CampaignBeFilterRepository campaignBeFilterRepository;

    // ── 실시간: TRIGGERED 캠페인 중 IN_PROGRESS인 것만 매칭 ──
    public void match(Long historyId, JsonNode logNode, String rawMessage) {
        String eventName = logNode.path("event_name").asText();

        List<CampaignBeEntity> campaigns = campaignBeRepository.findAll().stream()
            .filter(c -> "TRIGGERED".equals(c.getCampaignType()))
            .filter(c -> "IN_PROGRESS".equals(c.getStatus()))
            .toList();

        for (CampaignBeEntity campaign : campaigns) {
            List<CampaignBeFilterEntity> filters =
                campaignBeFilterRepository.findByCampaignIdWithEventField(campaign.getId());

            boolean hasMatchingEvent = filters.stream()
                .anyMatch(f -> eventName.equals(f.getEventName()));
            if (!hasMatchingEvent) continue;

            boolean matched = matchFilters(logNode, filters, campaign.getFilterLogicalOperator());

            if (matched) {
                handleSuccess(historyId, campaign, true, rawMessage, logNode);
            } else {
                handleFail(historyId, campaign);
            }
        }
    }

    // ── 배치: BATCH 캠페인 중 IN_PROGRESS인 것만 실행 ──
    @Scheduled(cron = "0 * * * * *")
    public void batchMatch() {
        LocalTime nowTime      = LocalTime.now();
        int nowHour            = nowTime.getHour();
        int nowMinute          = nowTime.getMinute();
        LocalDateTime now      = LocalDateTime.now();
        DayOfWeek nowDayOfWeek = now.getDayOfWeek();
        int nowDayOfMonth      = now.getDayOfMonth();

        List<CampaignBeEntity> batchCampaigns = campaignBeRepository.findAll().stream()
            .filter(c -> "BATCH".equals(c.getCampaignType()))
            .filter(c -> "IN_PROGRESS".equals(c.getStatus()))
            .filter(c -> c.getBatchCycle() != null)
            .filter(c -> c.getBatchTime() != null)
            .toList();

        for (CampaignBeEntity campaign : batchCampaigns) {
            String batchTimeStr = campaign.getBatchTime().toString();
            String[] timeParts  = batchTimeStr.split(":");
            if (timeParts.length < 2) continue;
            int runHour   = Integer.parseInt(timeParts[0].trim());
            int runMinute = Integer.parseInt(timeParts[1].trim());

            if (nowHour != runHour || nowMinute != runMinute) continue;

            String batchCycle = campaign.getBatchCycle();

            if ("WEEKLY".equals(batchCycle)) {
                if (campaign.getBatchDayOfWeek() == null) continue;
                DayOfWeek runDayOfWeek = DayOfWeek.valueOf(campaign.getBatchDayOfWeek());
                if (!nowDayOfWeek.equals(runDayOfWeek)) continue;
            }

            if ("MONTHLY".equals(batchCycle)) {
                if (campaign.getBatchDayOfMonth() == null) continue;
                if (nowDayOfMonth != campaign.getBatchDayOfMonth()) continue;
            }

            log.info("배치 캠페인 실행 - campaignId: {} batchCycle: {} 실행시각: {}:{}",
                campaign.getId(), batchCycle, nowHour, nowMinute);

            List<CampaignBeFilterEntity> filters =
                campaignBeFilterRepository.findByCampaignIdWithEventField(campaign.getId());

            if (filters.isEmpty()) continue;

            int maxPeriodDays = filters.stream()
                .mapToInt(CampaignBeFilterEntity::getPeriodDays)
                .max()
                .orElse(7);

            LocalDateTime from = LocalDateTime.now().minusDays(maxPeriodDays);
            List<HistoryLogEntity> logs = historyLogRepository.findByHistoryTimestampAfter(from);

            for (HistoryLogEntity historyLog : logs) {
                try {
                    JsonNode logNode  = objectMapper.readTree(historyLog.getJsonLog());
                    String eventName  = logNode.path("event_name").asText();

                    boolean hasMatchingEvent = filters.stream()
                        .anyMatch(f -> eventName.equals(f.getEventName()));
                    if (!hasMatchingEvent) continue;

                    boolean matched = matchFilters(logNode, filters, campaign.getFilterLogicalOperator());

                    if (matched) {
                        handleSuccess(historyLog.getHistoryId(), campaign, false, historyLog.getJsonLog(), logNode);
                    } else {
                        handleFail(historyLog.getHistoryId(), campaign);
                    }

                } catch (Exception e) {
                    log.error("배치 로그 파싱 오류 - historyId: {} error: {}", historyLog.getHistoryId(), e.getMessage());
                }
            }

            log.info("배치 캠페인 완료 - campaignId: {}", campaign.getId());
        }
    }

    private boolean matchFilters(JsonNode logNode, List<CampaignBeFilterEntity> filters, String logicalOperator) {
        if (filters == null || filters.isEmpty()) return false;

        List<Boolean> results = filters.stream()
            .map(f -> matchSingleFilter(logNode, f))
            .toList();

        if ("AND".equals(logicalOperator)) {
            return results.stream().allMatch(Boolean::booleanValue);
        } else {
            return results.stream().anyMatch(Boolean::booleanValue);
        }
    }

    private boolean matchSingleFilter(JsonNode logNode, CampaignBeFilterEntity filter) {
        try {
            JsonNode fieldNode = logNode.path(filter.getFieldName());
            if (fieldNode.isMissingNode()) return false;

            String fieldType = filter.getFieldType();
            String operator  = filter.getOperator();
            String value     = filter.getValue();

            if ("NUMBER".equals(fieldType) || "TIME".equals(fieldType)) {
                double actual   = fieldNode.asDouble();
                double expected = Double.parseDouble(value);
                return switch (operator) {
                    case "GTE"    -> actual >= expected;
                    case "LTE"    -> actual <= expected;
                    case "GT"     -> actual > expected;
                    case "LT"     -> actual < expected;
                    case "EQUALS" -> actual == expected;
                    default       -> false;
                };
            }

            if ("STRING".equals(fieldType)) {
                String actual = fieldNode.asText();
                return switch (operator) {
                    case "EQUALS"       -> actual.equals(value);
                    case "NOT_EQUALS"   -> !actual.equals(value);
                    case "CONTAINS"     -> actual.contains(value);
                    case "NOT_CONTAINS" -> !actual.contains(value);
                    default             -> false;
                };
            }

            return false;

        } catch (Exception e) {
            log.error("단일 필터 매칭 오류 - field: {} error: {}", filter.getFieldName(), e.getMessage());
            return false;
        }
    }

    private void handleSuccess(Long historyId, CampaignBeEntity campaign, boolean isRealtime, String rawMessage, JsonNode logNode) {
        Long campaignId = campaign.getId();
        LocalDate today = LocalDate.now();

        boolean isDuplicate = isRealtime
            ? filterSuccessRepository.existsByHistoryIdAndCampaignId(historyId, campaignId)
            : filterSuccessRepository.existsByHistoryIdAndCampaignIdAndCheckedDate(historyId, campaignId, today);

        if (isDuplicate) {
            log.info("중복 데이터 스킵 - historyId: {} campaignId: {}", historyId, campaignId);
            return;
        }

        FilterSuccessEntity success = new FilterSuccessEntity();
        success.setHistoryId(historyId);
        success.setCampaignId(campaignId);
        filterSuccessRepository.save(success);

        kafkaTemplate.send("FILTER_SUCCESS_TOPIC", String.valueOf(campaignId), rawMessage);
        log.info("필터링 성공 - historyId: {} campaignId: {}", historyId, campaignId);

        // user_id 추출
        JsonNode userIdNode = logNode.path("user_id");
        if (userIdNode.isMissingNode() || userIdNode.isNull()) {
            log.info("user_id 없음 - SSE 푸시 스킵");
            return;
        }
        Long userId = userIdNode.asLong();

        // SSE 푸시
        try {
            Long couponId = campaign.getCouponId();
            Long adId     = campaign.getAdId();
            sseEmitterService.sendEvent(userId, campaignId, couponId, adId);
        } catch (Exception e) {
            log.error("SSE 푸시 오류 - campaignId: {} error: {}", campaignId, e.getMessage());
        }
    }

    private void handleFail(Long historyId, CampaignBeEntity campaign) {
        Long campaignId = campaign.getId();
        LocalDate today = LocalDate.now();

        boolean isDuplicate = filterFailRepository
            .existsByHistoryIdAndCampaignIdAndCheckedDate(historyId, campaignId, today);

        if (isDuplicate) {
            log.info("중복 실패 데이터 스킵 - historyId: {} campaignId: {}", historyId, campaignId);
            return;
        }

        FilterFailEntity fail = new FilterFailEntity();
        fail.setHistoryId(historyId);
        fail.setCampaignId(campaignId);
        filterFailRepository.save(fail);

        log.info("필터링 실패 저장 완료 - historyId: {} campaignId: {}", historyId, campaignId);
    }
}