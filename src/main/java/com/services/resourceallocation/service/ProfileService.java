package com.services.resourceallocation.service;

import com.services.resourceallocation.dto.PasswordChangeDTO;
import com.services.resourceallocation.dto.ProfileUpdateDTO;
import com.services.resourceallocation.dto.UserProfileDTO;
import com.services.resourceallocation.model.User;
import com.services.resourceallocation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    public UserProfileDTO getUserProfile(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return mapToDTO(user);
    }

    public UserProfileDTO updateProfile(Integer userId, ProfileUpdateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update allowed fields
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        
        // Corrected to use the proper User entity setter
        user.setPhone(dto.getPhone()); 
        
        user.setDepartment(dto.getDepartment());
        user.setDob(dto.getDob());

        User updatedUser = userRepository.save(user);
        return mapToDTO(updatedUser);
    }

    public String changePassword(Integer userId, PasswordChangeDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 1. Verify current password matches DB
        if (!user.getPassword().equals(dto.getCurrentPassword())) {
            throw new IllegalArgumentException("Incorrect current password.");
        }

        // 2. Verify new password matches confirmation
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("New passwords do not match.");
        }

        // 3. Save new password
        user.setPassword(dto.getNewPassword());
        userRepository.save(user);

        return "Password successfully updated!";
    }

    // Mapper Helper
    private UserProfileDTO mapToDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        
        // Corrected to use the proper User entity getter
        dto.setPhone(user.getPhone()); 
        
        dto.setRole(user.getRole());
        dto.setDepartment(user.getDepartment());
        dto.setDob(user.getDob());
        
        // Safely map timestamp to string year. 
        // Because getCreatedAt() is already a LocalDateTime, we just call .getYear()
        // if (user.getCreatedAt() != null) {
        //     dto.setCreatedAt(String.valueOf(user.getCreatedAt()));
        // } else {
        //     dto.setCreatedAt("2025"); 
        // }
        return dto;
    }
}