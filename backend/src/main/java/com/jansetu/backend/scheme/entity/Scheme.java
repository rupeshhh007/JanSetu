package com.jansetu.backend.scheme.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "schemes")
@Getter
@Setter
public class Scheme {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 160)
    private String department;

    @Column(length = 2048)
    private String officialUrl;

    @Column(length = 255)
    private String sourceName;

    private Instant lastVerifiedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> benefits = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> requiredDocuments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SchemeStatus status = SchemeStatus.PUBLISHED;

    private Instant publishedAt;

    private Instant archivedAt;

    @Version
    private long version;

    @org.hibernate.annotations.CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @org.hibernate.annotations.UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    // This is the Hybrid JSONB implementation
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private EligibilityCriteria eligibilityCriteria;
}
