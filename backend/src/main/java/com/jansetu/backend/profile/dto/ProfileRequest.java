package com.jansetu.backend.profile.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jansetu.backend.profile.entity.Category;
import com.jansetu.backend.profile.entity.Gender;
import com.jansetu.backend.profile.entity.MaritalStatus;
import com.jansetu.backend.profile.entity.Occupation;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProfileRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "District is required")
    private String district;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Must be a valid 6-digit Indian pincode")
    private String pincode;

    @NotNull(message = "Category is required")
    private Category category;

    @NotNull(message = "Annual family income is required")
    @Min(value = 0, message = "Income cannot be negative")
    private BigDecimal annualFamilyIncome;

    @NotNull(message = "Marital status is required")
    private MaritalStatus maritalStatus;

    @NotNull(message = "Primary occupation is required")
    private Occupation primaryOccupation;

    @NotNull(message = "Student status is required")
    @JsonProperty("isStudent")
    private Boolean isStudent;

    @NotNull(message = "Farmer status is required")
    @JsonProperty("isFarmer")
    private Boolean isFarmer;

    @NotNull(message = "Disability status is required")
    @JsonProperty("hasDisability")
    private Boolean hasDisability;
}