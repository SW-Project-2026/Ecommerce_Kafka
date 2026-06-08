package com.logflow.log_pipeline.be;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalTime;

@Entity
@Table(name = "campaign")
@Getter
public class CampaignBeEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "campaign_name")
    private String campaignName;

    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "ad_id")
    private Long adId;

    @Column(name = "status")
    private String status;

    @Column(name = "campaign_type")
    private String campaignType;

    @Column(name = "filter_logical_operator")
    private String filterLogicalOperator;

    @Column(name = "batch_cycle")
    private String batchCycle;

    @Column(name = "batch_time")
    private LocalTime batchTime;

    @Column(name = "batch_day_of_week")
    private String batchDayOfWeek;

    @Column(name = "batch_day_of_month")
    private Integer batchDayOfMonth;
}