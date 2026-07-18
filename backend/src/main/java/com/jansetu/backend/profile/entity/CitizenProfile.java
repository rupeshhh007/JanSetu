package com.jansetu.backend.profile.entity;

import com.jansetu.backend.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "citizen_profiles")
@Getter
@Setter
@NoArgsConstructor
public class CitizenProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // --- THE CRITICAL RELATIONSHIP ---
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    // --- DEMOGRAPHICS ---
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    // --- LOCATION ---
    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false, length = 6)
    private String pincode;

    // --- SOCIO-ECONOMIC TRIGGERS ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal annualFamilyIncome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaritalStatus maritalStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Occupation primaryOccupation;

    // --- SPECIAL ELIGIBILITY FLAGS ---
    @Column(nullable = false)
    private boolean isStudent;

    @Column(nullable = false)
    private boolean isFarmer;

    @Column(nullable = false)
    private boolean hasDisability;

    // --- AUDIT FIELDS ---
    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}