package com.logflow.log_pipeline.pipeline.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventLogConsumer {

    private final EventLogRepository eventLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void consume(String jsonLog) {
        try {
            JsonNode node = objectMapper.readTree(jsonLog);

            String eventName = getText(node, "event_name");
            if (eventName == null) return;

            LocalDateTime eventTimestamp = parseTimestamp(getText(node, "event_timestamp"));
            Long userId = getLong(node, "user_id");

            EventLogEntity entity = EventLogEntity.builder()
                    .userId(userId)
                    .eventName(eventName)
                    .eventTimestamp(eventTimestamp)
                    // 상품 관련
                    .productId(getText(node, "productId"))
                    .productName(getText(node, "productName"))
                    .productCategory(getText(node, "productCategory"))
                    // 구매
                    .approvedAmount(getInt(node, "approvedAmount"))
                    // 찜/장바구니
                    .actionType(getText(node, "actionType"))
                    // 쿠폰
                    .couponCode(getText(node, "couponCode"))
                    .discountAmount(getInt(node, "discountAmount"))
                    // 검색
                    .searchKeyword(getText(node, "searchKeyword"))
                    // 페이지/상품 체류
                    .pageName(getText(node, "pageName"))
                    .dwellTime(getInt(node, "dwellTime"))
                    // 리뷰
                    .reviewRating(getInt(node, "reviewRating"))
                    // 포인트
                    .earnedPoints(getInt(node, "earnedPoints"))
                    .earnReason(getText(node, "earnReason"))
                    // 로그인/로그아웃
                    .loginId(getText(node, "user_login_id"))
                    .build();

            eventLogRepository.save(entity);

        } catch (Exception e) {
            log.error("[EventLogConsumer] 저장 실패: {}", e.getMessage());
        }
    }

    private String getText(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n == null || n.isNull()) ? null : n.asText();
    }

    private Integer getInt(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n == null || n.isNull()) ? null : n.asInt();
    }

    private Long getLong(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n == null || n.isNull()) ? null : n.asLong();
    }

    private LocalDateTime parseTimestamp(String raw) {
        if (raw == null) return LocalDateTime.now();
        try {
            return OffsetDateTime.parse(raw).toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}