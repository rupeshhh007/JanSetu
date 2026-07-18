package com.jansetu.backend.profile.dto;

import com.jansetu.backend.profile.entity.Category;
import com.jansetu.backend.profile.entity.Gender;
import com.jansetu.backend.profile.entity.MaritalStatus;
import com.jansetu.backend.profile.entity.Occupation;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ProfileResponse {

    private UUID profileId;

    // Core Identity
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Gender gender;

    // Location
    private String state;
    private String district;
    private String pincode;

    // Socio-Economic Triggers
    private Category category;
    private BigDecimal annualFamilyIncome;
    private MaritalStatus maritalStatus;
    private Occupation primaryOccupation;

    // Special Flags
    private boolean isStudent;
    private boolean isFarmer;
    private boolean hasDisability;

    // Metadata
    private Instant lastUpdated;
}