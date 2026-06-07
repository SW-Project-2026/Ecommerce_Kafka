package com.logflow.log_pipeline.be;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "campaign")
@Getter
public class CampaignBeEntity {

    @Id
    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "campaign_name")
    private String campaignName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category1")
    private String category1;

    @Column(name = "category2")
    private String category2;

    @Column(name = "status")
    private String status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "collection_type")
    private String collectionType;

    @Column(name = "batch_cycle")
    private String batchCycle;

    @Column(name = "batch_time")
    private LocalDateTime batchTime;

    @Column(name = "batch_day_of_week")
    private String batchDayOfWeek;

    @Column(name = "batch_day_of_month")
    private Integer batchDayOfMonth;

    @Column(name = "filter_logical_operator")
    private String filterLogicalOperator;

    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "ad_id")
    private Long adId;

    @Column(name = "coupon_restriction_days")
    private Integer couponRestrictionDays;
}