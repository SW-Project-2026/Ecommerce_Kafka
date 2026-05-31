package com.logflow.log_pipeline.pipeline.history;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface HistoryLogRepository extends JpaRepository<HistoryLogEntity, Long> {

    // 배치 스케줄러에서 최근 N일 이내 로그 조회용
    List<HistoryLogEntity> findByHistoryTimestampAfter(LocalDateTime from);
}