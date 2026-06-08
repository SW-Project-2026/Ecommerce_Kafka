package com.logflow.log_pipeline.be;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "event_field")
@Getter
public class EventFieldBeEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "field_name")
    private String fieldName;

    @Column(name = "field_type")
    private String fieldType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private EventBeEntity event;
}