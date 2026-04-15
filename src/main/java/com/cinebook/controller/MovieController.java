package com.cinebook.controller;

import com.cinebook.dto.*;
import com.cinebook.service.impl.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MovieDTO>>> getAllMovies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre) {

        List<MovieDTO> movies;
        if (search != null && !search.isBlank()) {
            movies = movieService.searchMovies(search);
        } else if (genre != null && !genre.isBlank()) {
            movies = movieService.getMoviesByGenre(genre);
        } else {
            movies = movieService.getAllMovies();
        }
        return ResponseEntity.ok(ApiResponse.ok("Movies fetched", movies));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieDTO>> getMovie(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Movie fetched", movieService.getMovieById(id)));
    }

    // Admin only
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MovieDTO>> createMovie(@RequestBody MovieDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Movie created", movieService.createMovie(dto)));
    }
}
