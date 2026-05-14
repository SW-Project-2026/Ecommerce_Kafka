package com.logflow.log_pipeline.campaign;

import jakarta.persistence.*;

@Entity
@Table(name = "Campaign_Filters")
public class CampaignFilterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "key")
    private Long key;

    @Column(nullable = false)
    private Long campaignId;

    @Column(nullable = false)
    private String eventName;

    @Column(nullable = false)
    private String eventFieldName;

    @Column(nullable = false)
    private String fieldType;

    @Column(nullable = false)
    private String operator;

    @Column(nullable = false)
    private String value;

    @Column(nullable = false)
    private Integer periodDays;

    public void setAll(Long campaignId, String eventName, String eventFieldName,
                       String fieldType, String operator, String value, Integer periodDays) {
        this.campaignId     = campaignId;
        this.eventName      = eventName;
        this.eventFieldName = eventFieldName;
        this.fieldType      = fieldType;
        this.operator       = operator;
        this.value          = value;
        this.periodDays     = periodDays;
    }

    public Long getCampaignId()       { return campaignId; }
    public String getEventName()      { return eventName; }
    public String getEventFieldName() { return eventFieldName; }
    public String getFieldType()      { return fieldType; }
    public String getOperator()       { return operator; }
    public String getValue()          { return value; }
    public Integer getPeriodDays()    { return periodDays; }
}