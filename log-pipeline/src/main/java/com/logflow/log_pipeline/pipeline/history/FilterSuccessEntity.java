package com.logflow.log_pipeline.pipeline.history;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Filter_Success")
public class FilterSuccessEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long successId;

    @Column(nullable = false)
    private Long historyId; // First_Save_History 참조

    @Column(nullable = false)
    private Long campaignId; // Campaigns 참조

    @Column(nullable = false)
    private LocalDate checkedDate; // 검사 일자 (연월일만, 배치 중복 체크용)

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt   = LocalDateTime.now();
        this.checkedDate = LocalDate.now();
    }

    public void setHistoryId(Long historyId)   { this.historyId = historyId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }

    public Long getSuccessId()          { return successId; }
    public Long getHistoryId()          { return historyId; }
    public Long getCampaignId()         { return campaignId; }
    public LocalDate getCheckedDate()   { return checkedDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}