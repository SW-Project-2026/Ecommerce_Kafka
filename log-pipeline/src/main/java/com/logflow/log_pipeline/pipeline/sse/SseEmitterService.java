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

        // 연결 확인용 초기 이벤트
        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitters.remove(userId);
        }

        return emitter;
    }

    // 필터 성공 시 해당 userId에 이벤트 푸시
    // SSE 연결 없으면 pending_notification에 저장
    public void sendEvent(Long userId, Long campaignId, Long couponId, Long adId) {
        SseEmitter emitter = emitters.get(userId);

        String payload = String.format(
            "{\"userId\":%d,\"campaignId\":%d,\"couponId\":%s,\"adId\":%s}",
            userId,
            campaignId,
            couponId != null ? couponId.toString() : "null",
            adId     != null ? adId.toString()     : "null"
        );

        if (emitter != null) {
            // SSE 연결 있으면 즉시 푸시
            try {
                emitter.send(SseEmitter.event().name("campaign").data(payload));
                log.info("SSE 이벤트 전송 - userId: {} campaignId: {}", userId, campaignId);
            } catch (IOException e) {
                emitters.remove(userId);
                log.error("SSE 전송 실패 - userId: {} error: {}", userId, e.getMessage());
                // 전송 실패 시 pending 저장
                savePending(userId, campaignId, couponId, adId);
            }
        } else {
            // SSE 연결 없으면 pending 저장
            log.info("SSE 연결 없음 - pending 저장 userId: {} campaignId: {}", userId, campaignId);
            savePending(userId, campaignId, couponId, adId);
        }
    }

    private void savePending(Long userId, Long campaignId, Long couponId, Long adId) {
        PendingNotificationEntity pending = new PendingNotificationEntity();
        pending.setUserId(userId);
        pending.setCampaignId(campaignId);
        pending.setCouponId(couponId);
        pending.setAdId(adId);
        pendingNotificationRepository.save(pending);
        log.info("pending 저장 완료 - userId: {} campaignId: {}", userId, campaignId);
    }
}