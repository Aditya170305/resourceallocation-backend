package com.services.resourceallocation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.services.resourceallocation.model.Resource;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Integer> {

    List<Resource> findByType(Resource.ResourceType type);

    // ── ADD THIS ── fixes the red underline error
    List<Resource> findByTypeAndDepartment(Resource.ResourceType type, String department);

    List<Resource> findByDepartment(String department);

    Optional<Resource> findByName(String name);

    Long countByDepartment(String department);
}