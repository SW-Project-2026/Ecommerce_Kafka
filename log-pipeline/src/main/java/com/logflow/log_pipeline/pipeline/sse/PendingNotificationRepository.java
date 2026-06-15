package com.logflow.log_pipeline.pipeline.sse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PendingNotificationRepository extends JpaRepository<PendingNotificationEntity, Long> {

    List<PendingNotificationEntity> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    List<PendingNotificationEntity> findByClientUuid(String clientUuid);

    void deleteByClientUuid(String clientUuid);
}