package com.cinebook.controller;

import com.cinebook.dto.*;
import com.cinebook.repository.UserRepository;
import com.cinebook.service.impl.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;
    private final UserRepository userRepository;

    // GET seat map for a showtime (public-ish — auth required for privacy)
    @GetMapping("/showtime/{showtimeId}")
    public ResponseEntity<ApiResponse<List<SeatDTO>>> getSeatMap(
            @PathVariable Long showtimeId) {
        return ResponseEntity.ok(
                ApiResponse.ok("Seat map fetched", seatService.getSeatMap(showtimeId)));
    }

    // POST /api/seats/lock — lock selected seats for 10 minutes
    @PostMapping("/lock")
    public ResponseEntity<ApiResponse<List<SeatDTO>>> lockSeats(
            @RequestBody LockSeatsRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        List<SeatDTO> locked = seatService.lockSeats(
                request.getShowtimeId(), request.getSeatCodes(), userId);
        return ResponseEntity.ok(ApiResponse.ok(
                "Seats locked for 10 minutes", locked));
    }

    // POST /api/seats/unlock — release locked seats (e.g. user goes back)
    @PostMapping("/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockSeats(
            @RequestBody LockSeatsRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        seatService.unlockSeats(request.getShowtimeId(), request.getSeatCodes(), userId);
        return ResponseEntity.ok(ApiResponse.ok("Seats unlocked", null));
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow().getId();
    }
}
