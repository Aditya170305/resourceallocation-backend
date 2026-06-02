package com.services.resourceallocation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.services.resourceallocation.model.User;
import com.services.resourceallocation.repository.UserRepository;
import com.services.resourceallocation.service.LoginService;
import com.services.resourceallocation.service.RegisterService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {
    
    @Autowired
    private RegisterService registerService;


    @Autowired
    private LoginService loginService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home() {
        return "Resource Allocation Backend Running Successfully";
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
       try {
            // Call the service
            User savedUser = registerService.saveUser(user);
            
            // Return success with the saved user details
            return ResponseEntity.ok("User registered successfully! ID: " + savedUser.getId());
            
        } catch (Exception e) {
            // If service throws "Email already in use", it caught here
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginData) {
        try {
            // We only need email and password from loginData
            User authenticatedUser = loginService.authenticate(loginData.getUsername(), loginData.getPassword());

            // Return the full user object (including role) to React
            return ResponseEntity.ok(authenticatedUser);

        } catch (Exception e) {
            // Returns "User not found" or "Invalid password"
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping("/users/count")
    public ResponseEntity<Long> getUsersCount(
            @RequestParam(required = false) String department
    ) {

        if (
            department == null ||
            department.equalsIgnoreCase("All Departments")
        ) {

            return ResponseEntity.ok(
                userRepository.count()
            );
        }

        return ResponseEntity.ok(
            userRepository.countByDepartment(
                department
            )
        );
    }

    @GetMapping("/users/all")
    public ResponseEntity<List<User>> getAllUsers() {

        List<User> users = userRepository.findAll();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/stats")
    public ResponseEntity<?> getUserStats(
            @RequestParam(required = false) String department
    ) {

        long total;
        long faculty;
        long hod;

        if (
            department == null ||
            department.equalsIgnoreCase("All Departments")
        ) {

            total = userRepository.count();

            faculty = userRepository.countByRole("Faculty");

            hod = userRepository.countByRole("HOD");

        } else {

            long facultyCount =
                    userRepository.countByDepartmentAndRole(
                            department,
                            "Faculty"
                    );

            long hodCount =
                    userRepository.countByDepartmentAndRole(
                            department,
                            "HOD"
                    );

            faculty = facultyCount;
            hod = hodCount;

            total = faculty + hod;
        }

        Map<String, Long> stats = new HashMap<>();

        stats.put("total", total);
        stats.put("faculty", faculty);
        stats.put("hod", hod);

        return ResponseEntity.ok(stats);
    }
}
