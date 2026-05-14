package com.logflow.log_pipeline.pipeline.history;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

public interface FilterFailRepository extends JpaRepository<FilterFailEntity, Long> {

    // 배치/실시간 모두 당일 기준 중복 체크
    boolean existsByHistoryIdAndCampaignIdAndCheckedDate(Long historyId, Long campaignId, LocalDate checkedDate);
}