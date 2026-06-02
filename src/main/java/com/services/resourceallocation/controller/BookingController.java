package com.services.resourceallocation.controller;

import com.services.resourceallocation.dto.*;
import com.services.resourceallocation.model.Booking;
import com.services.resourceallocation.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // ───────────────────────────────────────────────────────────
    //  FACULTY ENDPOINTS
    // ───────────────────────────────────────────────────────────

    /**
     * POST /api/bookings/request?facultyId=5
     * Faculty submits a new booking request.
     * Body: { resourceId, bookingDate, lectureSlot, startTime, endTime, purpose }
     */
    @PostMapping("/request")
    public ResponseEntity<?> createBooking(
            @RequestParam("facultyId") Integer facultyId,
            @RequestBody BookingRequestDTO req) {
        try {
            BookingResponseDTO result = bookingService.createBooking(facultyId, req);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Something went wrong: " + e.getMessage());
        }
    }

    /**
     * GET /api/bookings/my?facultyId=5&status=ALL
     * Faculty views their own bookings.
     * status: ALL | PENDING | APPROVED | REJECTED | CANCELLED
     */
    @GetMapping("/my")
    public ResponseEntity<List<BookingResponseDTO>> getMyBookings(
            @RequestParam("facultyId")                     Integer facultyId,
            @RequestParam(value = "status", defaultValue = "ALL") String status) {
        return ResponseEntity.ok(
            bookingService.getFacultyBookings(facultyId, status));
    }

    /**
     * GET /api/bookings/my/stats?facultyId=5
     * Dashboard stats for faculty.
     */
    @GetMapping("/my/stats")
    public ResponseEntity<BookingStatsDTO> getFacultyStats(
            @RequestParam("facultyId") Integer facultyId) {
        return ResponseEntity.ok(bookingService.getFacultyStats(facultyId));
    }

    /**
     * PUT /api/bookings/{id}/cancel?requesterId=5&role=Faculty
     * Faculty cancels their own PENDING booking.
     * HOD cancels any booking in their department.
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(
            @PathVariable("id")            Integer bookingId,
            @RequestParam("requesterId")   Integer requesterId,
            @RequestParam("role")          String  role) {
        try {
            BookingResponseDTO result =
                bookingService.cancelBooking(bookingId, requesterId, role);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    // ───────────────────────────────────────────────────────────
    //  HOD ENDPOINTS
    // ───────────────────────────────────────────────────────────

    /**
     * GET /api/bookings/department?dept=Computer+Science+and+Engineering&status=ALL
     * HOD views all bookings in their department.
     */
    @GetMapping("/department")
    public ResponseEntity<List<BookingResponseDTO>> getDepartmentBookings(
            @RequestParam("dept")                          String dept,
            @RequestParam(value = "status", defaultValue = "ALL") String status) {
        return ResponseEntity.ok(
            bookingService.getDepartmentBookings(dept, status));
    }

    /**
     * GET /api/bookings/department/stats?dept=Computer+Science+and+Engineering
     * Dashboard stats for HOD.
     */
    @GetMapping("/department/stats")
    public ResponseEntity<BookingStatsDTO> getHodStats(
            @RequestParam("dept") String dept) {
        return ResponseEntity.ok(bookingService.getHodStats(dept));
    }

    /**
     * PUT /api/bookings/{id}/approve
     * HOD approves a booking.
     * Body: { hodId, action: "APPROVE", remarks: "Approved." }
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveBooking(
            @PathVariable("id") Integer bookingId,
            @RequestBody HodReviewDTO review) {
        try {
            review.setAction("APPROVE");
            BookingResponseDTO result =
                bookingService.approveBooking(bookingId, review);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    /**
     * PUT /api/bookings/{id}/reject
     * HOD rejects a booking.
     * Body: { hodId, action: "REJECT", remarks: "Reason..." }
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectBooking(
            @PathVariable("id") Integer bookingId,
            @RequestBody HodReviewDTO review) {
        try {
            review.setAction("REJECT");
            BookingResponseDTO result =
                bookingService.rejectBooking(bookingId, review);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    // ───────────────────────────────────────────────────────────
    //  CALENDAR ENDPOINT
    // ───────────────────────────────────────────────────────────

    /**
     * GET /api/bookings/resource/{id}/approved
     * Returns all approved bookings for a resource — used by
     * ShowResources calendar to highlight booked slots in blue/red.
     */
    @GetMapping("/resource/{id}/approved")
    public ResponseEntity<List<BookingResponseDTO>> getApprovedForResource(
            @PathVariable("id") Integer resourceId) {
        return ResponseEntity.ok(
            bookingService.getApprovedBookingsForResource(resourceId));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Booking>> getAllBookingsForAdmin(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status
    ) {

        List<Booking> bookings =
                bookingService.getBookingsForAdmin(
                        department,
                        status
                );

        return ResponseEntity.ok(bookings);
    }
}