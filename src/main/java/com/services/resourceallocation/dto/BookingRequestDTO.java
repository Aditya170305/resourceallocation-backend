package com.services.resourceallocation.dto;

import java.time.LocalDate;

/**
 * Payload the faculty sends when submitting a new booking request.
 */
public class BookingRequestDTO {

    private Integer   resourceId;
    private LocalDate bookingDate;
    private String    lectureSlot;   // "1st Lecture" … "6th Lecture"
    private String    startTime;     // "HH:mm"
    private String    endTime;       // "HH:mm"
    private String    purpose;

    // ── Getters & Setters ──────────────────────────────────────

    public Integer   getResourceId()                    { return resourceId; }
    public void      setResourceId(Integer v)           { this.resourceId = v; }

    public LocalDate getBookingDate()                   { return bookingDate; }
    public void      setBookingDate(LocalDate v)        { this.bookingDate = v; }

    public String    getLectureSlot()                   { return lectureSlot; }
    public void      setLectureSlot(String v)           { this.lectureSlot = v; }

    public String    getStartTime()                     { return startTime; }
    public void      setStartTime(String v)             { this.startTime = v; }

    public String    getEndTime()                       { return endTime; }
    public void      setEndTime(String v)               { this.endTime = v; }

    public String    getPurpose()                       { return purpose; }
    public void      setPurpose(String v)               { this.purpose = v; }
}