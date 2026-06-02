package com.services.resourceallocation.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.services.resourceallocation.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    // ── Faculty: own bookings ──────────────────────────────────
    List<Booking> findByFacultyIdOrderByRequestedAtDesc(Integer facultyId);

    List<Booking> findByFacultyIdAndStatusOrderByRequestedAtDesc(
            Integer facultyId, String status);

    // ── HOD: department bookings ───────────────────────────────
    List<Booking> findByDepartmentOrderByRequestedAtDesc(String department);

    List<Booking> findByDepartmentAndStatusOrderByRequestedAtDesc(
            String department, String status);

    // ── HOD: pending requests that need review ─────────────────
    List<Booking> findByDepartmentAndStatusOrderByRequestedAtAsc(
            String department, String status);

    // ── CONFLICT CHECK ─────────────────────────────────────────
    // Checks if any APPROVED or PENDING booking exists for the same
    // resource on the same date and lecture slot.
    // A slot can only have ONE approved booking at a time.
    @Query("""
        SELECT b FROM Booking b
        WHERE b.resourceId   = :resourceId
          AND b.bookingDate  = :bookingDate
          AND b.lectureSlot  = :lectureSlot
          AND b.status IN ('APPROVED', 'PENDING')
          AND (:excludeId IS NULL OR b.bookingId <> :excludeId)
    """)
    List<Booking> findConflictingBookings(
            @Param("resourceId")   Integer   resourceId,
            @Param("bookingDate")  LocalDate bookingDate,
            @Param("lectureSlot")  String    lectureSlot,
            @Param("excludeId")    Integer   excludeId
    );

    // ── Strict conflict: only APPROVED (for final gate) ────────
    @Query("""
        SELECT b FROM Booking b
        WHERE b.resourceId   = :resourceId
          AND b.bookingDate  = :bookingDate
          AND b.lectureSlot  = :lectureSlot
          AND b.status = 'APPROVED'
    """)
    List<Booking> findApprovedConflicts(
            @Param("resourceId")  Integer   resourceId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("lectureSlot") String    lectureSlot
    );

    // ── Timetable view: all approved for a resource ────────────
    @Query("""
        SELECT b FROM Booking b
        WHERE b.resourceId = :resourceId
          AND b.status = 'APPROVED'
          AND b.bookingDate >= CURRENT_DATE
        ORDER BY b.bookingDate, b.startTime
    """)
    List<Booking> findApprovedByResourceId(@Param("resourceId") Integer resourceId);

    // ── Count stats for HOD dashboard ─────────────────────────
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.department = :dept AND b.status = :status")
    Long countByDepartmentAndStatus(
            @Param("dept")   String dept,
            @Param("status") String status);

    // ── Count stats for Faculty dashboard ─────────────────────
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.facultyId = :fid AND b.status = :status")
    Long countByFacultyIdAndStatus(
            @Param("fid")    Integer facultyId,
            @Param("status") String  status);
            
}