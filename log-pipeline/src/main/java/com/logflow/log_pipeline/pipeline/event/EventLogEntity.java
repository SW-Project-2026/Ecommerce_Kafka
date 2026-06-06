package com.logflow.log_pipeline.pipeline.event;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "event_name", nullable = false, length = 100)
    private String eventName;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    // 상품 관련
    @Column(name = "product_id", length = 100)
    private String productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "product_category", length = 100)
    private String productCategory;

    // 구매
    @Column(name = "approved_amount")
    private Integer approvedAmount;

    // 찜/장바구니
    @Column(name = "action_type", length = 20)
    private String actionType;

    // 쿠폰
    @Column(name = "coupon_code", length = 100)
    private String couponCode;

    @Column(name = "discount_amount")
    private Integer discountAmount;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    // 검색
    @Column(name = "search_keyword", length = 200)
    private String searchKeyword;

    // 페이지/상품 체류
    @Column(name = "page_name", length = 200)
    private String pageName;

    @Column(name = "dwell_time")
    private Integer dwellTime;

    // 리뷰
    @Column(name = "review_rating")
    private Integer reviewRating;

    // 포인트
    @Column(name = "earned_points")
    private Integer earnedPoints;

    @Column(name = "earn_reason", length = 100)
    private String earnReason;

    // 로그인/로그아웃
    @Column(name = "login_id", length = 100)
    private String loginId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}