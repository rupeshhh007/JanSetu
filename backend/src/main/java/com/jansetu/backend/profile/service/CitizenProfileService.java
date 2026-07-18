package com.jansetu.backend.profile.service;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.profile.dto.ProfileRequest;
import com.jansetu.backend.profile.dto.ProfileResponse;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.exception.ProfileNotFoundException;
import com.jansetu.backend.profile.repository.CitizenProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class CitizenProfileService {

    private final CitizenProfileRepository profileRepository;

    public CitizenProfileService(CitizenProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    // --- 1. CREATE PROFILE ---
    public ProfileResponse createProfile(User user, ProfileRequest request) {
        // Business Rule: A user can only have ONE profile.
        if (profileRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("A profile already exists for this user.");
        }

        CitizenProfile profile = new CitizenProfile();
        profile.setUser(user); // Establish the OneToOne link

        updateProfileFields(profile, request);

        CitizenProfile savedProfile = profileRepository.save(profile);
        return mapToResponse(savedProfile);
    }

    // --- 2. GET PROFILE ---
    public ProfileResponse getProfile(User user) {
        return mapToResponse(getProfileEntity(user));
    }

    // --- 3. UPDATE PROFILE ---
    public ProfileResponse updateProfile(User user, ProfileRequest request) {
        CitizenProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found"));

        updateProfileFields(profile, request);

        CitizenProfile updatedProfile = profileRepository.save(profile);
        return mapToResponse(updatedProfile);
    }

    // --- HELPER: Map Data from DTO to Entity ---
    private void updateProfileFields(CitizenProfile profile, ProfileRequest request) {
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());
        profile.setState(request.getState());
        profile.setDistrict(request.getDistrict());
        profile.setPincode(request.getPincode());
        profile.setCategory(request.getCategory());
        profile.setAnnualFamilyIncome(request.getAnnualFamilyIncome());
        profile.setMaritalStatus(request.getMaritalStatus());
        profile.setPrimaryOccupation(request.getPrimaryOccupation());

        // Note: Boolean objects in DTO use getter, boolean primitives in Entity use setter
        profile.setStudent(request.getIsStudent());
        profile.setFarmer(request.getIsFarmer());
        profile.setHasDisability(request.getHasDisability());
    }
    public CitizenProfile getProfileByEmail(String email) {
        return profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found"));
    }
    public CitizenProfile getProfileEntity(User user) {
        // Use the ID instead of the User object reference
        return profileRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found"));
    }

    // --- HELPER: Map Data from Entity to DTO ---
    private ProfileResponse mapToResponse(CitizenProfile profile) {
        return ProfileResponse.builder()
                .profileId(profile.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .state(profile.getState())
                .district(profile.getDistrict())
                .pincode(profile.getPincode())
                .category(profile.getCategory())
                .annualFamilyIncome(profile.getAnnualFamilyIncome())
                .maritalStatus(profile.getMaritalStatus())
                .primaryOccupation(profile.getPrimaryOccupation())
                .isStudent(profile.isStudent())
                .isFarmer(profile.isFarmer())
                .hasDisability(profile.isHasDisability())
                .lastUpdated(profile.getUpdatedAt()) // Returns the auto-generated timestamp
                .build();
    }
}