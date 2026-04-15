package com.cinebook.controller;

import com.cinebook.dto.ApiResponse;
import com.cinebook.model.Hall;
import com.cinebook.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/halls")
@RequiredArgsConstructor
public class HallController {

    private final HallRepository hallRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Hall>>> getAllHalls() {
        return ResponseEntity.ok(ApiResponse.ok("Halls fetched", hallRepository.findByActiveTrue()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Hall>> createHall(@RequestBody Hall hall) {
        return ResponseEntity.ok(ApiResponse.ok("Hall created", hallRepository.save(hall)));
    }
}
