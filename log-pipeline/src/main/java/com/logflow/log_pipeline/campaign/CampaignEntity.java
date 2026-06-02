package com.logflow.log_pipeline.campaign;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Campaign")
public class CampaignEntity {

    @Id
    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "collection_type")
    private String collectionType;

    @Column(name = "filter_logical_operator")
    private String filterLogicalOperator;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "batch_cycle")
    private String batchCycle;

    @Column(name = "batch_time")
    private String batchTime;

    @Column(name = "batch_day_of_week")
    private String batchDayOfWeek;

    @Column(name = "batch_day_of_month")
    private Integer batchDayOfMonth;

    // 신규 추가
    @Column(name = "status")
    private String status;

    public Long getCampaignId()              { return campaignId; }
    public String getCollectionType()        { return collectionType; }
    public String getFilterLogicalOperator() { return filterLogicalOperator; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public String getBatchCycle()            { return batchCycle; }
    public String getBatchTime()             { return batchTime; }
    public String getBatchDayOfWeek()        { return batchDayOfWeek; }
    public Integer getBatchDayOfMonth()      { return batchDayOfMonth; }
    public String getStatus()                { return status; }

    public void setCampaignId(Long campaignId)             { this.campaignId = campaignId; }
    public void setCollectionType(String collectionType)   { this.collectionType = collectionType; }
    public void setFilterLogicalOperator(String op)        { this.filterLogicalOperator = op; }
    public void setCreatedAt(LocalDateTime createdAt)      { this.createdAt = createdAt; }
    public void setBatchCycle(String batchCycle)           { this.batchCycle = batchCycle; }
    public void setBatchTime(String batchTime)             { this.batchTime = batchTime; }
    public void setBatchDayOfWeek(String batchDayOfWeek)   { this.batchDayOfWeek = batchDayOfWeek; }
    public void setBatchDayOfMonth(Integer batchDayOfMonth){ this.batchDayOfMonth = batchDayOfMonth; }
    public void setStatus(String status)                   { this.status = status; }
}