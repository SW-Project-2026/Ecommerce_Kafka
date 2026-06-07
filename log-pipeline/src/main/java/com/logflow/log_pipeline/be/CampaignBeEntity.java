package com.logflow.log_pipeline.be;

import jakarta.persistence.*;
import lombok.Getter;

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
}