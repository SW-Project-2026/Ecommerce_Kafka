package com.logflow.log_pipeline.pipeline.sse;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SseEmitterService {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterService.class);
    private static final Long TIMEOUT = 30 * 60 * 1000L; // 30분

    private final PendingNotificationRepository pendingNotificationRepository;

    // key → SseEmitter ("user:{userId}" 또는 "uuid:{clientUuid}")
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    private String userKey(Long userId)        { return "user:" + userId; }
    private String uuidKey(String clientUuid)  { return "uuid:" + clientUuid; }

    // ── 로그인 사용자 연결 ──
    public SseEmitter connect(Long userId) {
        return connectByKey(userKey(userId), "userId: " + userId);
    }

    // ── 비로그인 사용자 연결 (client_uuid 기준) ──
    public SseEmitter connectByClientUuid(String clientUuid) {
        return connectByKey(uuidKey(clientUuid), "clientUuid: " + clientUuid);
    }

    private SseEmitter connectByKey(String key, String logLabel) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        emitter.onCompletion(() -> {
            emitters.remove(key);
            log.info("SSE 연결 종료 - {}", logLabel);
        });
        emitter.onTimeout(() -> {
            emitters.remove(key);
            log.info("SSE 타임아웃 - {}", logLabel);
        });
        emitter.onError(e -> {
            emitters.remove(key);
            log.error("SSE 오류 - {} error: {}", logLabel, e.getMessage());
        });

        emitters.put(key, emitter);
        log.info("SSE 연결 - {}", logLabel);

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitters.remove(key);
        }

        return emitter;
    }

    // ── 로그인 사용자에게 이벤트 전송 ──
    public void sendEvent(Long userId, Long campaignId, Long couponId,
                          String couponName, String discountType, Integer discountAmount,
                          Integer minOrderAmount, Integer maxDiscountAmount) {
        String payload = buildPayload(userId, null, campaignId, couponId,
            couponName, discountType, discountAmount, minOrderAmount, maxDiscountAmount);

        boolean sent = trySend(userKey(userId), payload, "userId: " + userId);
        if (!sent) {
            savePending(userId, null, campaignId, couponId,
                couponName, discountType, discountAmount, minOrderAmount, maxDiscountAmount);
        }
    }

    // ── 비로그인 사용자에게 이벤트 전송 (client_uuid 기준) ──
    public void sendEventByClientUuid(String clientUuid, Long campaignId, Long couponId,
                          String couponName, String discountType, Integer discountAmount,
                          Integer minOrderAmount, Integer maxDiscountAmount) {
        String payload = buildPayload(null, clientUuid, campaignId, couponId,
            couponName, discountType, discountAmount, minOrderAmount, maxDiscountAmount);

        boolean sent = trySend(uuidKey(clientUuid), payload, "clientUuid: " + clientUuid);
        if (!sent) {
            savePending(null, clientUuid, campaignId, couponId,
                couponName, discountType, discountAmount, minOrderAmount, maxDiscountAmount);
        }
    }

    // ── 공통 전송 처리, 성공 여부 반환 ──
    private boolean trySend(String key, String payload, String logLabel) {
        SseEmitter emitter = emitters.get(key);

        if (emitter == null) {
            log.info("SSE 연결 없음 - pending 저장 {}", logLabel);
            return false;
        }

        try {
            emitter.send(SseEmitter.event().name("campaign").data(payload));
            log.info("SSE 이벤트 전송 - {}", logLabel);
            return true;
        } catch (IOException e) {
            emitters.remove(key);
            log.error("SSE 전송 실패 - {} error: {}", logLabel, e.getMessage());
            return false;
        }
    }

    private String buildPayload(Long userId, String clientUuid, Long campaignId, Long couponId,
                                String couponName, String discountType, Integer discountAmount,
                                Integer minOrderAmount, Integer maxDiscountAmount) {
        return String.format(
            "{\"userId\":%s,\"clientUuid\":%s,\"campaignId\":%d,\"couponId\":%s," +
            "\"couponName\":%s,\"discountType\":%s,\"discountAmount\":%s," +
            "\"minOrderAmount\":%s,\"maxDiscountAmount\":%s}",
            userId            != null ? userId.toString()             : "null",
            clientUuid        != null ? "\"" + clientUuid + "\""       : "null",
            campaignId,
            couponId          != null ? couponId.toString()              : "null",
            couponName        != null ? "\"" + couponName + "\""         : "null",
            discountType      != null ? "\"" + discountType + "\""       : "null",
            discountAmount    != null ? discountAmount.toString()        : "null",
            minOrderAmount    != null ? minOrderAmount.toString()        : "null",
            maxDiscountAmount != null ? maxDiscountAmount.toString()     : "null"
        );
    }

    private void savePending(Long userId, String clientUuid, Long campaignId, Long couponId,
                             String couponName, String discountType, Integer discountAmount,
                             Integer minOrderAmount, Integer maxDiscountAmount) {
        PendingNotificationEntity pending = new PendingNotificationEntity();
        pending.setUserId(userId);
        pending.setClientUuid(clientUuid);
        pending.setCampaignId(campaignId);
        pending.setCouponId(couponId);
        pending.setCouponName(couponName);
        pending.setDiscountType(discountType);
        pending.setDiscountAmount(discountAmount);
        pending.setMinOrderAmount(minOrderAmount);
        pending.setMaxDiscountAmount(maxDiscountAmount);
        pendingNotificationRepository.save(pending);
        log.info("pending 저장 완료 - userId: {} clientUuid: {} campaignId: {}", userId, clientUuid, campaignId);
    }
}