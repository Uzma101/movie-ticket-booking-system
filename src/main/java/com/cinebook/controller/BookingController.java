package com.cinebook.controller;

import com.cinebook.dto.*;
import com.cinebook.repository.UserRepository;
import com.cinebook.service.impl.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    // POST /api/bookings — create booking after seats are locked
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        BookingResponse booking = bookingService.createBooking(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Booking confirmed!", booking));
    }

    // GET /api/bookings — current user's booking history
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(
                ApiResponse.ok("Bookings fetched", bookingService.getUserBookings(userId)));
    }

    // GET /api/bookings/{code} — get booking by code
    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
            @PathVariable String code,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(
                ApiResponse.ok("Booking fetched", bookingService.getBookingByCode(code, userId)));
    }

    // DELETE /api/bookings/{id}/cancel
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(
                ApiResponse.ok("Booking cancelled", bookingService.cancelBooking(id, userId)));
    }

    // GET /api/bookings/analytics — admin only
    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalytics() {
        return ResponseEntity.ok(ApiResponse.ok("Analytics", bookingService.getAnalytics()));
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow().getId();
    }
}
