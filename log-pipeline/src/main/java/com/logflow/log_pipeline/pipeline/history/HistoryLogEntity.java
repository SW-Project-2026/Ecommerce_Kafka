package com.logflow.log_pipeline.pipeline.history;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "First_Save_History") // DB 테이블 이름
public class HistoryLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PK 자동 증가
    private Long historyId;

    private LocalDateTime historyTimestamp; // DB 저장 시간

    @Column(columnDefinition = "TEXT") // 긴 JSON 저장을 위해 TEXT 타입
    private String jsonLog; // 원본 JSON 데이터

    @PrePersist
    public void prePersist() {
        this.historyTimestamp = LocalDateTime.now(); // 저장 직전 현재 시간 세팅
    }

    public void setJsonLog(String jsonLog) { this.jsonLog = jsonLog; }

    public Long getHistoryId() { return historyId; }
    public LocalDateTime getHistoryTimestamp() { return historyTimestamp; }
    public String getJsonLog() { return jsonLog; }
}