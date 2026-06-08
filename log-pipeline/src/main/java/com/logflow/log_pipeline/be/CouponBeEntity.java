package com.logflow.log_pipeline.be;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "coupon")
@Getter
public class CouponBeEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "discount_type")
    private String discountType;

    @Column(name = "discount_amount")
    private Integer discountAmount;

    @Column(name = "min_order_amount")
    private Integer minOrderAmount;

    @Column(name = "max_discount_amount")
    private Integer maxDiscountAmount;
}