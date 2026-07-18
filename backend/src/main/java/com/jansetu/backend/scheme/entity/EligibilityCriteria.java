package com.jansetu.backend.scheme.entity;

import com.jansetu.backend.profile.entity.Category;
import com.jansetu.backend.profile.entity.Occupation;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class EligibilityCriteria {
    // We use nullable Integer/BigDecimal/Boolean so that if a scheme
    // doesn't care about a specific rule, it can simply be null in the JSON.

    private Integer minAge;
    private Integer maxAge;
    private BigDecimal maxFamilyIncome;
    private List<Category> requiredCategories;
    private List<Occupation> allowedOccupations;
    private Boolean requiresStudent;
    private Boolean requiresFarmer;
    private Boolean requiresDisability;
}