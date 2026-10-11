package com.jansetu.backend.application.entity;

import com.jansetu.backend.application.enums.ApplicationStatus;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.scheme.entity.Scheme;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private CitizenProfile citizenProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id", nullable = false)
    private Scheme scheme;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    private LocalDateTime submittedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private long version;
}
