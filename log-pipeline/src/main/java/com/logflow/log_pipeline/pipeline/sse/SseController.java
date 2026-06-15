package com.logflow.log_pipeline.pipeline.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class SseController {

    private final SseEmitterService sseEmitterService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam(required = false) Long userId,
                              @RequestParam(required = false) String clientUuid) {
        if (userId != null) {
            return sseEmitterService.connect(userId);
        }
        return sseEmitterService.connectByClientUuid(clientUuid);
    }
}