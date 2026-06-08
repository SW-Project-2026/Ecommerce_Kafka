package com.logflow.log_pipeline.be;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignBeFilterRepository extends JpaRepository<CampaignBeFilterEntity, Long> {

    @Query("SELECT f FROM CampaignBeFilterEntity f JOIN FETCH f.eventField ef JOIN FETCH ef.event WHERE f.campaignId = :campaignId")
    List<CampaignBeFilterEntity> findByCampaignIdWithEventField(@Param("campaignId") Long campaignId);
}