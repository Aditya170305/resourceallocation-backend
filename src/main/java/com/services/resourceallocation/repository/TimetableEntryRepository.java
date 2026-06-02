package com.services.resourceallocation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.services.resourceallocation.model.TimetableEntry;

@Repository
public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Integer> {

    // All entries for a specific resource
    List<TimetableEntry> findByResource_ResourceId(Integer resourceId);

    // Entries for a resource filtered by lecture slot
    List<TimetableEntry> findByResource_ResourceIdAndLectureSlot(
            Integer resourceId,
            TimetableEntry.LectureSlot lectureSlot
    );

    // Eager fetch entries for a resource (no N+1)
    @Query("SELECT t FROM TimetableEntry t JOIN FETCH t.resource r WHERE r.resourceId = :resourceId")
    List<TimetableEntry> findByResourceIdEager(@Param("resourceId") Integer resourceId);

    // ── NEW: All entries where a specific faculty has a class ──────────────
    // Used by faculty dashboard to show "Today's Schedule"
    @Query("SELECT t FROM TimetableEntry t JOIN FETCH t.resource r WHERE t.facultyName = :facultyName")
    List<TimetableEntry> findByFacultyName(@Param("facultyName") String facultyName);

    // Delete all entries for a resource (used by timetable upload)
    @Modifying
    @Transactional
    @Query("DELETE FROM TimetableEntry t WHERE t.resource.resourceId = :resourceId")
    void deleteByResourceId(@Param("resourceId") Integer resourceId);

    List<TimetableEntry>
    findByResource_ResourceIdAndDayName(
        Integer resourceId,
        String dayName
    );

    void deleteByResource_ResourceIdAndDayName(
        Integer resourceId,
        String dayName
    );
}