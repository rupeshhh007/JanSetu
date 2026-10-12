package com.jansetu.backend.profile.service;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.profile.dto.ProfileRequest;
import com.jansetu.backend.profile.entity.Category;
import com.jansetu.backend.profile.entity.Gender;
import com.jansetu.backend.profile.entity.MaritalStatus;
import com.jansetu.backend.profile.entity.Occupation;
import com.jansetu.backend.profile.fact.CitizenProfileFactService;
import com.jansetu.backend.profile.repository.CitizenProfileRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CitizenProfileServiceTest {
    @Test
    void savesTheInitialProfileAsPrivateSelfDeclaredPolicyFacts() {
        CitizenProfileRepository profiles = mock(CitizenProfileRepository.class);
        CitizenProfileFactService facts = mock(CitizenProfileFactService.class);
        CitizenProfileService service = new CitizenProfileService(profiles, facts);
        User user = new User();
        when(profiles.findByUser(user)).thenReturn(Optional.empty());
        when(profiles.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.createProfile(user, request());

        verify(facts, times(8)).synchronizeSelfDeclaredProfileFact(any(), any(), any());
        verify(facts).synchronizeSelfDeclaredProfileFact(any(), eq(FactKey.ANNUAL_FAMILY_INCOME), eq("250000"));
        verify(facts).synchronizeSelfDeclaredProfileFact(any(), eq(FactKey.IS_STUDENT), eq("true"));
    }

    private ProfileRequest request() {
        ProfileRequest request = new ProfileRequest();
        request.setFirstName("Riya");
        request.setLastName("Test");
        request.setDateOfBirth(LocalDate.of(2001, 1, 1));
        request.setGender(Gender.FEMALE);
        request.setState("Maharashtra");
        request.setDistrict("Pune");
        request.setPincode("411001");
        request.setCategory(Category.OBC);
        request.setAnnualFamilyIncome(new BigDecimal("250000"));
        request.setMaritalStatus(MaritalStatus.SINGLE);
        request.setPrimaryOccupation(Occupation.STUDENT);
        request.setIsStudent(true);
        request.setIsFarmer(false);
        request.setHasDisability(false);
        return request;
    }
}
