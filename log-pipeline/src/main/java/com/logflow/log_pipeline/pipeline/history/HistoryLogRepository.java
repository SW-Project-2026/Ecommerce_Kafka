package com.logflow.log_pipeline.pipeline.history;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryLogRepository extends JpaRepository<HistoryLogEntity, Long> {
}