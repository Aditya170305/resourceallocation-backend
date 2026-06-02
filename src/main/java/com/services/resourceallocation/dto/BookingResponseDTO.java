package com.services.resourceallocation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Returned by the API for both faculty and HOD views.
 */
public class BookingResponseDTO {

    private Integer       bookingId;
    private Integer       resourceId;
    private String        resourceName;
    private String        resourceType;
    private Integer       facultyId;
    private String        facultyName;
    private String        department;
    private LocalDate     bookingDate;
    private String        lectureSlot;
    private String        startTime;      // "HH:mm"
    private String        endTime;        // "HH:mm"
    private String        purpose;
    private String        status;         // PENDING | APPROVED | REJECTED | CANCELLED
    private String        hodRemarks;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;
    private boolean       isHallBooking;

    // ── Getters & Setters ──────────────────────────────────────

    public Integer       getBookingId()                     { return bookingId; }
    public void          setBookingId(Integer v)            { this.bookingId = v; }

    public Integer       getResourceId()                    { return resourceId; }
    public void          setResourceId(Integer v)           { this.resourceId = v; }

    public String        getResourceName()                  { return resourceName; }
    public void          setResourceName(String v)          { this.resourceName = v; }

    public String        getResourceType()                  { return resourceType; }
    public void          setResourceType(String v)          { this.resourceType = v; }

    public Integer       getFacultyId()                     { return facultyId; }
    public void          setFacultyId(Integer v)            { this.facultyId = v; }

    public String        getFacultyName()                   { return facultyName; }
    public void          setFacultyName(String v)           { this.facultyName = v; }

    public String        getDepartment()                    { return department; }
    public void          setDepartment(String v)            { this.department = v; }

    public LocalDate     getBookingDate()                   { return bookingDate; }
    public void          setBookingDate(LocalDate v)        { this.bookingDate = v; }

    public String        getLectureSlot()                   { return lectureSlot; }
    public void          setLectureSlot(String v)           { this.lectureSlot = v; }

    public String        getStartTime()                     { return startTime; }
    public void          setStartTime(String v)             { this.startTime = v; }

    public String        getEndTime()                       { return endTime; }
    public void          setEndTime(String v)               { this.endTime = v; }

    public String        getPurpose()                       { return purpose; }
    public void          setPurpose(String v)               { this.purpose = v; }

    public String        getStatus()                        { return status; }
    public void          setStatus(String v)                { this.status = v; }

    public String        getHodRemarks()                    { return hodRemarks; }
    public void          setHodRemarks(String v)            { this.hodRemarks = v; }

    public LocalDateTime getRequestedAt()                   { return requestedAt; }
    public void          setRequestedAt(LocalDateTime v)    { this.requestedAt = v; }

    public LocalDateTime getReviewedAt()                    { return reviewedAt; }
    public void          setReviewedAt(LocalDateTime v)     { this.reviewedAt = v; }

    public boolean       isHallBooking()                    { return isHallBooking; }
    public void          setHallBooking(boolean v)          { this.isHallBooking = v; }
}