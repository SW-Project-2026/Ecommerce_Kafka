package com.logflow.log_pipeline.be;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "campaign_filter")
@Getter
public class CampaignBeFilterEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "operator")
    private String operator;

    @Column(name = "value")
    private String value;

    @Column(name = "period_days")
    private Integer periodDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_field_id")
    private EventFieldBeEntity eventField;

    // 편의 메서드
    public String getEventName() {
        return eventField != null && eventField.getEvent() != null
            ? eventField.getEvent().getEventName() : null;
    }

    public String getFieldName() {
        return eventField != null ? eventField.getFieldName() : null;
    }

    public String getFieldType() {
        return eventField != null ? eventField.getFieldType() : null;
    }
}