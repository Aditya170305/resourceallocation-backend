package com.services.resourceallocation.controller;

import com.services.resourceallocation.dto.PasswordChangeDTO;
import com.services.resourceallocation.dto.ProfileUpdateDTO;
import com.services.resourceallocation.dto.UserProfileDTO;
import com.services.resourceallocation.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    // GET /api/profile?userId=5
    @GetMapping
    public ResponseEntity<?> getProfile(@RequestParam("userId") Integer userId) {
        try {
            return ResponseEntity.ok(profileService.getUserProfile(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT /api/profile/update?userId=5
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(
            @RequestParam("userId") Integer userId,
            @RequestBody ProfileUpdateDTO updateRequest) {
        try {
            UserProfileDTO updatedProfile = profileService.updateProfile(userId, updateRequest);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT /api/profile/change-password?userId=5
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestParam("userId") Integer userId,
            @RequestBody PasswordChangeDTO passwordRequest) {
        try {
            String successMessage = profileService.changePassword(userId, passwordRequest);
            return ResponseEntity.ok(successMessage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred while changing the password.");
        }
    }
}