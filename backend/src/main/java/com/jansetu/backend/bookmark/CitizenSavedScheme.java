package com.jansetu.backend.bookmark;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.scheme.entity.Scheme;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/** Bookmarks are private account state; never expose another citizen's shortlist. */
@Entity
@Table(name = "citizen_saved_schemes",
       uniqueConstraints = @UniqueConstraint(name = "uq_citizen_saved_scheme", columnNames = {"citizen_id", "scheme_id"}))
@Getter
@Setter
public class CitizenSavedScheme {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citizen_id", nullable = false)
    private User citizen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scheme_id", nullable = false)
    private Scheme scheme;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
