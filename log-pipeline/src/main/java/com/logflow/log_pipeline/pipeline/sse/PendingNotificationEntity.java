package com.logflow.log_pipeline.pipeline.sse;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "pending_notification")
@Getter
public class PendingNotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "client_uuid")
    private String clientUuid;

    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "coupon_name")
    private String couponName;

    @Column(name = "discount_type")
    private String discountType;

    @Column(name = "discount_amount")
    private Integer discountAmount;

    @Column(name = "min_order_amount")
    private Integer minOrderAmount;

    @Column(name = "max_discount_amount")
    private Integer maxDiscountAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void setUserId(Long userId)                   { this.userId = userId; }
    public void setClientUuid(String clientUuid)         { this.clientUuid = clientUuid; }
    public void setCampaignId(Long campaignId)           { this.campaignId = campaignId; }
    public void setCouponId(Long couponId)               { this.couponId = couponId; }
    public void setCouponName(String couponName)         { this.couponName = couponName; }
    public void setDiscountType(String discountType)     { this.discountType = discountType; }
    public void setDiscountAmount(Integer discountAmount) { this.discountAmount = discountAmount; }
    public void setMinOrderAmount(Integer minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    public void setMaxDiscountAmount(Integer maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }
}