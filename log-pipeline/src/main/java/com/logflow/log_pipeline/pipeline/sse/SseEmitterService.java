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

    // userId → SseEmitter
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    // FE에서 SSE 연결 시 호출
    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.info("SSE 연결 종료 - userId: {}", userId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            log.info("SSE 타임아웃 - userId: {}", userId);
        });
        emitter.onError(e -> {
            emitters.remove(userId);
            log.error("SSE 오류 - userId: {} error: {}", userId, e.getMessage());
        });

        emitters.put(userId, emitter);
        log.info("SSE 연결 - userId: {}", userId);

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitters.remove(userId);
        }

        return emitter;
    }

    // 필터 성공 시 해당 userId에 이벤트 푸시
    // SSE 연결 없으면 pending_notification에 저장
    public void sendEvent(Long userId, Long campaignId, Long couponId, Long adId,
                          String couponName, String discountType, Integer discountAmount,
                          Integer minOrderAmount, Integer maxDiscountAmount) {
        SseEmitter emitter = emitters.get(userId);

        String payload = buildPayload(userId, campaignId, couponId, adId,
            couponName, discountType, discountAmount, minOrderAmount, maxDiscountAmount);

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("campaign").data(payload));
                log.info("SSE 이벤트 전송 - userId: {} campaignId: {}", userId, campaignId);
            } catch (IOException e) {
                emitters.remove(userId);
                log.error("SSE 전송 실패 - userId: {} error: {}", userId, e.getMessage());
                savePending(userId, campaignId, couponId, adId,
                    couponName, discountType, discountAmount, minOrderAmount, maxDiscountAmount);
            }
        } else {
            log.info("SSE 연결 없음 - pending 저장 userId: {} campaignId: {}", userId, campaignId);
            savePending(userId, campaignId, couponId, adId,
                couponName, discountType, discountAmount, minOrderAmount, maxDiscountAmount);
        }
    }

    private String buildPayload(Long userId, Long campaignId, Long couponId, Long adId,
                                String couponName, String discountType, Integer discountAmount,
                                Integer minOrderAmount, Integer maxDiscountAmount) {
        return String.format(
            "{\"userId\":%d,\"campaignId\":%d,\"couponId\":%s,\"adId\":%s," +
            "\"couponName\":%s,\"discountType\":%s,\"discountAmount\":%s," +
            "\"minOrderAmount\":%s,\"maxDiscountAmount\":%s}",
            userId,
            campaignId,
            couponId          != null ? couponId.toString()          : "null",
            adId              != null ? adId.toString()              : "null",
            couponName        != null ? "\"" + couponName + "\""     : "null",
            discountType      != null ? "\"" + discountType + "\""   : "null",
            discountAmount    != null ? discountAmount.toString()    : "null",
            minOrderAmount    != null ? minOrderAmount.toString()    : "null",
            maxDiscountAmount != null ? maxDiscountAmount.toString() : "null"
        );
    }

    private void savePending(Long userId, Long campaignId, Long couponId, Long adId,
                             String couponName, String discountType, Integer discountAmount,
                             Integer minOrderAmount, Integer maxDiscountAmount) {
        PendingNotificationEntity pending = new PendingNotificationEntity();
        pending.setUserId(userId);
        pending.setCampaignId(campaignId);
        pending.setCouponId(couponId);
        pending.setAdId(adId);
        pending.setCouponName(couponName);
        pending.setDiscountType(discountType);
        pending.setDiscountAmount(discountAmount);
        pending.setMinOrderAmount(minOrderAmount);
        pending.setMaxDiscountAmount(maxDiscountAmount);
        pendingNotificationRepository.save(pending);
        log.info("pending 저장 완료 - userId: {} campaignId: {}", userId, campaignId);
    }
}