package com.services.resourceallocation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.services.resourceallocation.model.TimetableEntry;

public interface TimetableRepository
        extends JpaRepository<TimetableEntry, Long> {
}