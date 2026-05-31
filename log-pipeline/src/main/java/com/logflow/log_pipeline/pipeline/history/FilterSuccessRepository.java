package com.logflow.log_pipeline.pipeline.history;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

public interface FilterSuccessRepository extends JpaRepository<FilterSuccessEntity, Long> {

    // 실시간 중복 체크: history_id + campaign_id 조합
    boolean existsByHistoryIdAndCampaignId(Long historyId, Long campaignId);

    // 배치 중복 체크: history_id + campaign_id + 검사일자 조합
    boolean existsByHistoryIdAndCampaignIdAndCheckedDate(Long historyId, Long campaignId, LocalDate checkedDate);
}