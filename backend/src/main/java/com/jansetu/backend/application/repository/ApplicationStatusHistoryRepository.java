package com.jansetu.backend.application.repository;

import com.jansetu.backend.application.entity.Application;
import com.jansetu.backend.application.entity.ApplicationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, UUID> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "changedBy")
    List<ApplicationStatusHistory> findByApplicationOrderByChangedAtDesc(Application application);
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "changedBy")
    List<ApplicationStatusHistory> findByApplicationInOrderByChangedAtDesc(List<Application> applications);
}

