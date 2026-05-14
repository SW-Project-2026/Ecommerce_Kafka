package com.logflow.log_pipeline.campaign;

import org.springframework.data.jpa.repository.JpaRepository;

// Campaign 테이블 CRUD
public interface CampaignRepository extends JpaRepository<CampaignEntity, Long> {
}