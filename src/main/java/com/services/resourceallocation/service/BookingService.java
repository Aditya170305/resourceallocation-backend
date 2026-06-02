package com.services.resourceallocation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.services.resourceallocation.dto.BookingRequestDTO;
import com.services.resourceallocation.dto.BookingResponseDTO;
import com.services.resourceallocation.dto.BookingStatsDTO;
import com.services.resourceallocation.dto.HodReviewDTO;
import com.services.resourceallocation.model.Booking;
import com.services.resourceallocation.model.Resource;
import com.services.resourceallocation.model.User;
import com.services.resourceallocation.repository.BookingRepository;
import com.services.resourceallocation.repository.ResourceRepository;
import com.services.resourceallocation.repository.UserRepository;

@Service
public class BookingService {

    // Resource types that require the 48-hour advance booking rule
    private static final Set<String> HALL_TYPES = Set.of("Hall");

    @Autowired private BookingRepository   bookingRepository;
    @Autowired private ResourceRepository  resourceRepository;
    @Autowired private UserRepository      userRepository;

    // ═══════════════════════════════════════════════════════════
    //  FACULTY: CREATE BOOKING REQUEST
    // ═══════════════════════════════════════════════════════════

    /**
     * Faculty submits a new booking request.
     *
     * Edge cases handled:
     *  1. Resource must exist.
     *  2. Faculty must be a valid user.
     *  3. Booking date must be in the future.
     *  4. Hall/event bookings must be requested at least 48 hours in advance.
     *  5. Duplicate check: if the faculty already has a PENDING or APPROVED
     *     booking for the same resource+date+slot, reject.
     *  6. Conflict check: if another APPROVED booking exists for the same
     *     resource+date+slot, reject immediately (no point sending to HOD).
     *  7. If a PENDING conflict exists, still allow — HOD will resolve it
     *     (first-approved-wins enforced at approval time).
     */
    @Transactional
    public BookingResponseDTO createBooking(Integer facultyId,
                                            BookingRequestDTO req) {
        // 1. Validate resource
        Resource resource = resourceRepository.findById(req.getResourceId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Resource not found: " + req.getResourceId()));

        // 2. Validate faculty
        User faculty = userRepository.findById(facultyId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Faculty not found: " + facultyId));

        // 3. Booking date must be in the future
        if (req.getBookingDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Booking date must be a future date.");
        }


        // 4. Hall/event: 48-hour advance rule
        boolean isHall = HALL_TYPES.contains(resource.getType().name());
        if (isHall) {
            LocalDateTime earliest = LocalDateTime.now().plusHours(48);
            LocalDateTime requested = req.getBookingDate().atTime(
                LocalTime.parse(req.getStartTime()));
            if (requested.isBefore(earliest)) {
                throw new IllegalArgumentException(
                    "Hall and event bookings must be requested at least " +
                    "48 hours in advance. Please choose a later date/time.");
            }
        }

        // 5. Duplicate: faculty already requested this exact slot
        List<Booking> myConflicts = bookingRepository.findConflictingBookings(
            req.getResourceId(), req.getBookingDate(), req.getLectureSlot(), null)
            .stream()
            .filter(b -> b.getFacultyId().equals(facultyId))
            .collect(Collectors.toList());

        if (!myConflicts.isEmpty()) {
            throw new IllegalArgumentException(
                "You already have a booking request for this resource, " +
                "date, and lecture slot.");
        }

        // 6. Hard conflict: slot already APPROVED by someone else
        List<Booking> approvedConflicts = bookingRepository.findApprovedConflicts(
            req.getResourceId(), req.getBookingDate(), req.getLectureSlot());

        if (!approvedConflicts.isEmpty()) {
            throw new IllegalArgumentException(
                "This slot is already booked and approved for '" +
                resource.getName() + "' on " + req.getBookingDate() +
                " (" + req.getLectureSlot() + "). Please choose another slot.");
        }

        // 7. Soft conflict: PENDING exists from someone else — allow, HOD resolves
        //    (we just proceed; first-approved-wins is enforced in approveBooking)

        // Build and save booking
        Booking booking = new Booking();
        booking.setResourceId(resource.getResourceId());
        booking.setResourceName(resource.getName());
        booking.setResourceType(resource.getType().name());
        booking.setFacultyId(facultyId);
        booking.setFacultyName(faculty.getFullName());
        
        // 🚨 CRITICAL FIX 1: Assign the booking to the RESOURCE'S department!
        booking.setDepartment(faculty.getDepartment());
        
        booking.setBookingDate(req.getBookingDate());
        booking.setLectureSlot(req.getLectureSlot());
        booking.setStartTime(LocalTime.parse(req.getStartTime()));
        booking.setEndTime(LocalTime.parse(req.getEndTime()));
        booking.setPurpose(req.getPurpose());
        booking.setStatus("PENDING");
        booking.setHallBooking(isHall);

        // 🚨 CRITICAL FIX 2: Set the requested timestamp so JPA can sort it!
        booking.setRequestedAt(LocalDateTime.now());

        return toDTO(bookingRepository.save(booking));
    }

    // ═══════════════════════════════════════════════════════════
    //  HOD: APPROVE BOOKING
    // ═══════════════════════════════════════════════════════════

    /**
     * HOD approves a pending booking.
     *
     * Edge cases handled:
     *  1. Booking must exist and be PENDING.
     *  2. HOD must belong to the same department as the booking.
     *  3. Re-check for hard conflicts at approval time (race condition:
     *     two pending requests for same slot — first approved wins,
     *     second gets auto-rejected with a clear message).
     *  4. After approval, all other PENDING requests for the same
     *     resource+date+slot are auto-rejected (with a system remark).
     */
    @Transactional
    public BookingResponseDTO approveBooking(Integer bookingId,
                                             HodReviewDTO review) {
        // 1. Booking must exist and be PENDING
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Booking not found: " + bookingId));

        if (!"PENDING".equals(booking.getStatus())) {
            throw new IllegalStateException(
                "Only PENDING bookings can be approved. " +
                "Current status: " + booking.getStatus());
        }

        // 2. HOD must be in same department
        User hod = userRepository.findById(review.getHodId())
            .orElseThrow(() -> new IllegalArgumentException(
                "HOD not found: " + review.getHodId()));

        if (!hod.getDepartment().equalsIgnoreCase(booking.getDepartment())) {
            throw new SecurityException(
                "You can only approve bookings from your own department.");
        }

        // 3. Race condition: re-check for approved conflicts at approval time
        List<Booking> approvedConflicts = bookingRepository.findApprovedConflicts(
            booking.getResourceId(),
            booking.getBookingDate(),
            booking.getLectureSlot());

        if (!approvedConflicts.isEmpty()) {
            // Another booking was approved first — auto-reject this one
            booking.setStatus("REJECTED");
            booking.setHodId(review.getHodId());
            booking.setHodRemarks(
                "Auto-rejected: The slot was approved for another faculty member " +
                "(" + approvedConflicts.get(0).getFacultyName() + ") " +
                "before this request could be reviewed.");
            booking.setReviewedAt(LocalDateTime.now());
            return toDTO(bookingRepository.save(booking));
        }

        // 4. Approve this booking
        booking.setStatus("APPROVED");
        booking.setHodId(review.getHodId());
        booking.setHodRemarks(review.getRemarks());
        booking.setReviewedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // 4a. Auto-reject all other PENDING requests for the same slot
        List<Booking> competitors = bookingRepository.findConflictingBookings(
            booking.getResourceId(),
            booking.getBookingDate(),
            booking.getLectureSlot(),
            bookingId   // exclude the one we just approved
        );

        for (Booking rival : competitors) {
            if ("PENDING".equals(rival.getStatus())) {
                rival.setStatus("REJECTED");
                rival.setHodId(review.getHodId());
                rival.setHodRemarks(
                    "Auto-rejected: The slot was allocated to " +
                    booking.getFacultyName() +
                    ". Please choose a different slot.");
                rival.setReviewedAt(LocalDateTime.now());
                bookingRepository.save(rival);
            }
        }

        return toDTO(booking);
    }

    // ═══════════════════════════════════════════════════════════
    //  HOD: REJECT BOOKING
    // ═══════════════════════════════════════════════════════════

    @Transactional
    public BookingResponseDTO rejectBooking(Integer bookingId,
                                            HodReviewDTO review) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Booking not found: " + bookingId));

        if (!"PENDING".equals(booking.getStatus())) {
            throw new IllegalStateException(
                "Only PENDING bookings can be rejected.");
        }

        User hod = userRepository.findById(review.getHodId())
            .orElseThrow(() -> new IllegalArgumentException(
                "HOD not found: " + review.getHodId()));

        if (!hod.getDepartment().equalsIgnoreCase(booking.getDepartment())) {
            throw new SecurityException(
                "You can only reject bookings from your own department.");
        }

        booking.setStatus("REJECTED");
        booking.setHodId(review.getHodId());
        booking.setHodRemarks(
            review.getRemarks() != null ? review.getRemarks() : "Rejected by HOD.");
        booking.setReviewedAt(LocalDateTime.now());

        return toDTO(bookingRepository.save(booking));
    }

    // ═══════════════════════════════════════════════════════════
    //  FACULTY / HOD: CANCEL BOOKING
    // ═══════════════════════════════════════════════════════════

    /**
     * Faculty can cancel their own PENDING bookings.
     * HOD can cancel any PENDING or APPROVED booking in their dept.
     */
    @Transactional
    public BookingResponseDTO cancelBooking(Integer bookingId,
                                            Integer requesterId,
                                            String  requesterRole) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Booking not found: " + bookingId));

        if ("CANCELLED".equals(booking.getStatus()) ||
            "REJECTED".equals(booking.getStatus())) {
            throw new IllegalStateException(
                "Booking is already " + booking.getStatus() + ".");
        }

        if ("Faculty".equalsIgnoreCase(requesterRole)) {
            // Faculty can only cancel their own pending bookings
            if (!booking.getFacultyId().equals(requesterId)) {
                throw new SecurityException(
                    "You can only cancel your own bookings.");
            }
            if (!"PENDING".equals(booking.getStatus())) {
                throw new IllegalStateException(
                    "Faculty can only cancel PENDING bookings. " +
                    "Contact your HOD to cancel an approved booking.");
            }
        } else if ("HOD".equalsIgnoreCase(requesterRole)) {
            // HOD can cancel any booking in their department
            User hod = userRepository.findById(requesterId).orElseThrow();
            if (!hod.getDepartment().equalsIgnoreCase(booking.getDepartment())) {
                throw new SecurityException(
                    "You can only cancel bookings from your own department.");
            }
        }

        booking.setStatus("CANCELLED");
        booking.setReviewedAt(LocalDateTime.now());

        return toDTO(bookingRepository.save(booking));
    }

    // ═══════════════════════════════════════════════════════════
    //  FACULTY: GET MY BOOKINGS
    // ═══════════════════════════════════════════════════════════

    public List<BookingResponseDTO> getFacultyBookings(Integer facultyId,
                                                        String statusFilter) {
        List<Booking> list;
        if (statusFilter == null || statusFilter.isBlank() ||
            "ALL".equalsIgnoreCase(statusFilter)) {
            list = bookingRepository
                .findByFacultyIdOrderByRequestedAtDesc(facultyId);
        } else {
            list = bookingRepository
                .findByFacultyIdAndStatusOrderByRequestedAtDesc(
                    facultyId, statusFilter.toUpperCase());
        }
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  HOD: GET DEPARTMENT BOOKINGS
    // ═══════════════════════════════════════════════════════════

    public List<BookingResponseDTO> getDepartmentBookings(String department,
                                                           String statusFilter) {
        List<Booking> list;
        if (statusFilter == null || statusFilter.isBlank() ||
    "ALL".equalsIgnoreCase(statusFilter)) {

    list = bookingRepository
        .findByDepartmentOrderByRequestedAtDesc(
            department
        );

} else {

    if ("PENDING".equalsIgnoreCase(statusFilter)) {

        list = bookingRepository
            .findByDepartmentAndStatusOrderByRequestedAtAsc(
                department,
                "PENDING"
            );

    } else {

        list = bookingRepository
            .findByDepartmentAndStatusOrderByRequestedAtDesc(
                department,
                statusFilter.toUpperCase()
            );
    }
}
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  CALENDAR: APPROVED BOOKINGS FOR A RESOURCE
    //  (used by ShowResources to highlight booked slots)
    // ═══════════════════════════════════════════════════════════

    public List<BookingResponseDTO> getApprovedBookingsForResource(
            Integer resourceId) {
        return bookingRepository.findApprovedByResourceId(resourceId)
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  STATS
    // ═══════════════════════════════════════════════════════════

    public BookingStatsDTO getFacultyStats(Integer facultyId) {
        long approved  = bookingRepository.countByFacultyIdAndStatus(facultyId, "APPROVED");
        long pending   = bookingRepository.countByFacultyIdAndStatus(facultyId, "PENDING");
        long rejected  = bookingRepository.countByFacultyIdAndStatus(facultyId, "REJECTED");
        long cancelled = bookingRepository.countByFacultyIdAndStatus(facultyId, "CANCELLED");
        return new BookingStatsDTO(
            approved + pending + rejected + cancelled,
            approved, pending, rejected, cancelled);
    }

    public BookingStatsDTO getHodStats(String department) {
        long approved  = bookingRepository.countByDepartmentAndStatus(department, "APPROVED");
        long pending   = bookingRepository.countByDepartmentAndStatus(department, "PENDING");
        long rejected  = bookingRepository.countByDepartmentAndStatus(department, "REJECTED");
        long cancelled = bookingRepository.countByDepartmentAndStatus(department, "CANCELLED");
        return new BookingStatsDTO(
            approved + pending + rejected + cancelled,
            approved, pending, rejected, cancelled);
    }

    // ═══════════════════════════════════════════════════════════
    //  PRIVATE: entity → DTO
    // ═══════════════════════════════════════════════════════════

    private BookingResponseDTO toDTO(Booking b) {
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setBookingId(b.getBookingId());
        dto.setResourceId(b.getResourceId());
        dto.setResourceName(b.getResourceName());
        dto.setResourceType(b.getResourceType());
        dto.setFacultyId(b.getFacultyId());
        dto.setFacultyName(b.getFacultyName());
        dto.setDepartment(b.getDepartment());
        dto.setBookingDate(b.getBookingDate());
        dto.setLectureSlot(b.getLectureSlot());
        dto.setStartTime(b.getStartTime() != null
            ? b.getStartTime().toString() : null);
        dto.setEndTime(b.getEndTime() != null
            ? b.getEndTime().toString() : null);
        dto.setPurpose(b.getPurpose());
        dto.setStatus(b.getStatus());
        dto.setHodRemarks(b.getHodRemarks());
        dto.setRequestedAt(b.getRequestedAt());
        dto.setReviewedAt(b.getReviewedAt());
        dto.setHallBooking(b.isHallBooking());
        return dto;
    }

    public List<Booking> getBookingsForAdmin(
        String department,
        String status
    ) {

        List<Booking> bookings =
                bookingRepository.findAll();

        if (
            department != null &&
            !department.equalsIgnoreCase("All Departments")
        ) {

            bookings = bookings.stream()
                    .filter(b ->
                            b.getDepartment() != null &&
                            b.getDepartment()
                                    .equalsIgnoreCase(department)
                    )
                    .toList();
        }

        if (
            status != null &&
            !status.equalsIgnoreCase("All Status")
        ) {

            bookings = bookings.stream()
                    .filter(b ->
                            b.getStatus() != null &&
                            b.getStatus()
                                    .equalsIgnoreCase(status)
                    )
                    .toList();
        }

        return bookings;
    }
}