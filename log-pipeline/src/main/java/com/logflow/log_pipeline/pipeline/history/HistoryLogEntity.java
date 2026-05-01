package com.logflow.log_pipeline.pipeline.history;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "First_Save_History")
public class HistoryLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    private LocalDateTime historyTimestamp;

    @Column(columnDefinition = "TEXT")
    private String jsonLog;

    @PrePersist
    public void prePersist() {
        this.historyTimestamp = LocalDateTime.now();
    }

    public void setJsonLog(String jsonLog) { this.jsonLog = jsonLog; }

    public Long getHistoryId() { return historyId; }
    public LocalDateTime getHistoryTimestamp() { return historyTimestamp; }
    public String getJsonLog() { return jsonLog; }
}