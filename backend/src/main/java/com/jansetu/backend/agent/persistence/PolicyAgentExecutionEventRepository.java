package com.jansetu.backend.agent.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PolicyAgentExecutionEventRepository extends JpaRepository<PolicyAgentExecutionEvent, UUID> {
    List<PolicyAgentExecutionEvent> findByRunIdOrderBySequenceNumber(UUID runId);
    long countByRunId(UUID runId);
}
