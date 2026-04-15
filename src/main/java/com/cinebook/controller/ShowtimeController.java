package com.cinebook.controller;

import com.cinebook.dto.*;
import com.cinebook.service.impl.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<ApiResponse<List<ShowtimeDTO>>> getByMovie(@PathVariable Long movieId) {
        return ResponseEntity.ok(
                ApiResponse.ok("Showtimes fetched", showtimeService.getShowtimesByMovie(movieId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShowtimeDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Showtime fetched", showtimeService.getShowtimeById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShowtimeDTO>> create(@RequestBody ShowtimeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Showtime created", showtimeService.createShowtime(dto)));
    }
}
