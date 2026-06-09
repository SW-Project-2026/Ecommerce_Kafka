package com.logflow.log_pipeline.be;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdBeRepository extends JpaRepository<AdBeEntity, Long> {
}