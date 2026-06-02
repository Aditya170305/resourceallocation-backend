package com.services.resourceallocation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.services.resourceallocation.model.User;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    // Find HOD for a given department (used to validate HOD review)
    Optional<User> findByDepartmentAndRole(String department, String role);

    // All faculty in a department
    // List<User> findByDepartmentAndRole(String department, String roleFilter);

    Long countByDepartment(String department);

    List<User> findByRole(String role);

    Long countByRole(String role);

    Long countByDepartmentAndRole(
            String department ,
            String role
    );
}