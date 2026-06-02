package com.services.resourceallocation.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.services.resourceallocation.model.User;
import com.services.resourceallocation.repository.UserRepository;

@Service
public class LoginService {

    @Autowired
    private UserRepository userRepository;

    public User authenticate(String username, String password) throws Exception {
        // 1. Search for the user by email
        Optional<User> userOpt = userRepository.findByUsername(username);

        // 2. Check if user exists
        if (userOpt.isEmpty()) {
            throw new Exception("User not found with this email!");
        }

        User user = userOpt.get();

        // 3. Verify password (Direct comparison for now)
        if (!user.getPassword().equals(password)) {
            throw new Exception("Invalid password!");
        }

        // 4. Return user object if everything is correct
        return user;
    }
}