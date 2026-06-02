package com.services.resourceallocation.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "booking",
       indexes = {
           @Index(name = "idx_booking_slot",
                  columnList = "resource_id,booking_date,lecture_slot,status"),
           @Index(name = "idx_booking_faculty",
                  columnList = "faculty_id,status"),
           @Index(name = "idx_booking_department",
                  columnList = "department,status")
       })
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Integer bookingId;

    // ── Resource info ──────────────────────────────────────────
    @Column(name = "resource_id", nullable = false)
    private Integer resourceId;

    @Column(name = "resource_name", nullable = false, length = 100)
    private String resourceName;

    @Column(name = "resource_type", nullable = false, length = 100)
    private String resourceType;

    // ── Faculty info ───────────────────────────────────────────
    @Column(name = "faculty_id", nullable = false)
    private Integer facultyId;

    @Column(name = "faculty_name", nullable = false, length = 255)
    private String facultyName;

    @Column(name = "department", nullable = false, length = 255)
    private String department;

    // ── Slot info ──────────────────────────────────────────────
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "lecture_slot", nullable = false)
    private String lectureSlot;   // "1st Lecture" … "6th Lecture"

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "purpose", nullable = false, columnDefinition = "TEXT")
    private String purpose;

    // ── Status ────────────────────────────────────────────────
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";   // PENDING | APPROVED | REJECTED | CANCELLED

    // ── HOD review ────────────────────────────────────────────
    @Column(name = "hod_id")
    private Integer hodId;

    @Column(name = "hod_remarks", columnDefinition = "TEXT")
    private String hodRemarks;

    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    // ── Hall/event flag (48-hr advance rule) ──────────────────
    @Column(name = "is_hall_booking", nullable = false)
    private boolean isHallBooking = false;

    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) requestedAt = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────

    public Integer getBookingId()                    { return bookingId; }
    public void    setBookingId(Integer bookingId)   { this.bookingId = bookingId; }

    public Integer getResourceId()                   { return resourceId; }
    public void    setResourceId(Integer resourceId) { this.resourceId = resourceId; }

    public String  getResourceName()                    { return resourceName; }
    public void    setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String  getResourceType()                    { return resourceType; }
    public void    setResourceType(String resourceType) { this.resourceType = resourceType; }

    public Integer getFacultyId()                    { return facultyId; }
    public void    setFacultyId(Integer facultyId)   { this.facultyId = facultyId; }

    public String  getFacultyName()                     { return facultyName; }
    public void    setFacultyName(String facultyName)   { this.facultyName = facultyName; }

    public String  getDepartment()                    { return department; }
    public void    setDepartment(String department)   { this.department = department; }

    public LocalDate getBookingDate()                   { return bookingDate; }
    public void      setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public String  getLectureSlot()                     { return lectureSlot; }
    public void    setLectureSlot(String lectureSlot)   { this.lectureSlot = lectureSlot; }

    public LocalTime getStartTime()                   { return startTime; }
    public void      setStartTime(LocalTime startTime){ this.startTime = startTime; }

    public LocalTime getEndTime()                   { return endTime; }
    public void      setEndTime(LocalTime endTime)  { this.endTime = endTime; }

    public String  getPurpose()                   { return purpose; }
    public void    setPurpose(String purpose)     { this.purpose = purpose; }

    public String  getStatus()                   { return status; }
    public void    setStatus(String status)      { this.status = status; }

    public Integer getHodId()                  { return hodId; }
    public void    setHodId(Integer hodId)     { this.hodId = hodId; }

    public String  getHodRemarks()                    { return hodRemarks; }
    public void    setHodRemarks(String hodRemarks)   { this.hodRemarks = hodRemarks; }

    public LocalDateTime getRequestedAt()                       { return requestedAt; }
    public void          setRequestedAt(LocalDateTime v)        { this.requestedAt = v; }

    public LocalDateTime getReviewedAt()                      { return reviewedAt; }
    public void          setReviewedAt(LocalDateTime v)       { this.reviewedAt = v; }

    public boolean isHallBooking()                   { return isHallBooking; }
    public void    setHallBooking(boolean v)         { this.isHallBooking = v; }
}