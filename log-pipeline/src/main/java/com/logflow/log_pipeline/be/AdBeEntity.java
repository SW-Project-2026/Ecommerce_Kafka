package com.logflow.log_pipeline.be;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "ad")
@Getter
public class AdBeEntity {

    @Id
    @Column(name = "ad_id")
    private Long adId;

    @Column(name = "ad_name")
    private String adName;

    @Column(name = "target_type")
    private String targetType;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "category")
    private String category;

    @Column(name = "keyword")
    private String keyword;
}