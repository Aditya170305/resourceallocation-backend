package com.services.resourceallocation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.services.resourceallocation.model.User;
import com.services.resourceallocation.repository.UserRepository;

@Service
public class RegisterService {
    
    @Autowired
    UserRepository userRepository;

    public User saveUser(User user) throws Exception {
        // 1. Business Logic: Check if email exists
        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            throw new Exception("Email is already in use!");
        }

        // 2. Perform Save
        return userRepository.save(user);
    }
}
