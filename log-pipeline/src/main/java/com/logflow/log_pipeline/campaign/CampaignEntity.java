package com.logflow.log_pipeline.campaign;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Campaigns")
public class CampaignEntity {

    @Id
    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "collection_type")
    private String collectionType; // TRIGGERED / BATCH

    @Column(name = "filter_logical_operator")
    private String filterLogicalOperator; // AND / OR

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // DAILY / WEEKLY / MONTHLY (VARCHAR)
    @Column(name = "batch_cycle")
    private String batchCycle;

    // HH:mm 형식 (예: "09:00")
    @Column(name = "batch_time")
    private String batchTime;

    // MONDAY ~ SUNDAY (WEEKLY일 때만 사용)
    @Column(name = "batch_day_of_week")
    private String batchDayOfWeek;

    // 1~31 (MONTHLY일 때만 사용)
    @Column(name = "batch_day_of_month")
    private Integer batchDayOfMonth;

    public Long getCampaignId()              { return campaignId; }
    public String getCollectionType()        { return collectionType; }
    public String getFilterLogicalOperator() { return filterLogicalOperator; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public String getBatchCycle()            { return batchCycle; }
    public String getBatchTime()             { return batchTime; }
    public String getBatchDayOfWeek()        { return batchDayOfWeek; }
    public Integer getBatchDayOfMonth()      { return batchDayOfMonth; }

    public void setCampaignId(Long campaignId)                    { this.campaignId = campaignId; }
    public void setCollectionType(String collectionType)          { this.collectionType = collectionType; }
    public void setFilterLogicalOperator(String op)               { this.filterLogicalOperator = op; }
    public void setCreatedAt(LocalDateTime createdAt)             { this.createdAt = createdAt; }
    public void setBatchCycle(String batchCycle)                  { this.batchCycle = batchCycle; }
    public void setBatchTime(String batchTime)                    { this.batchTime = batchTime; }
    public void setBatchDayOfWeek(String batchDayOfWeek)          { this.batchDayOfWeek = batchDayOfWeek; }
    public void setBatchDayOfMonth(Integer batchDayOfMonth)       { this.batchDayOfMonth = batchDayOfMonth; }
}