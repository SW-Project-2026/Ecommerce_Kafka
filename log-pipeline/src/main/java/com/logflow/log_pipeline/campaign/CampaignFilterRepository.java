package com.logflow.log_pipeline.campaign;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// Campaign_Filter 테이블 CRUD
public interface CampaignFilterRepository extends JpaRepository<CampaignFilterEntity, Long> {

    // 특정 캠페인의 필터 조건 전체 조회
    List<CampaignFilterEntity> findByCampaignId(Long campaignId);

    // 캠페인 수정 시 기존 필터 전체 삭제
    void deleteByCampaignId(Long campaignId);
}