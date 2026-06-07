package com.logflow.log_pipeline.pipeline.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class PendingNotificationController {

    private final PendingNotificationRepository pendingNotificationRepository;

    // 미수신 알림 조회
    @GetMapping("/pending")
    public ResponseEntity<List<Map<String, Object>>> getPending(@RequestParam Long userId) {
        List<PendingNotificationEntity> list = pendingNotificationRepository.findByUserId(userId);
        List<Map<String, Object>> result = list.stream()
            .map(p -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id",         p.getId());
                map.put("userId",     p.getUserId());
                map.put("campaignId", p.getCampaignId());
                map.put("couponId",   p.getCouponId());
                map.put("adId",       p.getAdId());
                return map;
            })
            .toList();
        return ResponseEntity.ok(result);
    }

    // 미수신 알림 삭제 (확인 후)
    @Transactional
    @DeleteMapping("/pending")
    public ResponseEntity<Void> deletePending(@RequestParam Long userId) {
        pendingNotificationRepository.deleteByUserId(userId);
        return ResponseEntity.ok().build();
    }
}