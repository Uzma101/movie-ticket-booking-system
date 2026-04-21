package com.cinebook.job;

import com.cinebook.dto.ShowtimeDTO;
import com.cinebook.model.Hall;
import com.cinebook.model.Movie;
import com.cinebook.repository.HallRepository;
import com.cinebook.repository.MovieRepository;
import com.cinebook.repository.ShowtimeRepository;
import com.cinebook.service.impl.ShowtimeService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShowtimeGeneratorJob {

    private final ShowtimeService showtimeService;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final ShowtimeRepository showtimeRepository;

    /**
     * Run once when application starts
     */
    @PostConstruct
    public void runOnStartup() {
        log.info("Running showtime generation on startup...");
        generateShowtimes();
    }

    /**
     * Runs every midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void generateShowtimes() {
        log.info("Running automated showtime generation job...");

        // safer null handling
        List<Movie> activeMovies = movieRepository.findAll()
                .stream()
                .filter(movie -> Boolean.TRUE.equals(movie.getActive()))
                .toList();

        List<Hall> activeHalls = hallRepository.findAll()
                .stream()
                .filter(hall -> Boolean.TRUE.equals(hall.getActive()))
                .toList();

        if (activeMovies.isEmpty() || activeHalls.isEmpty()) {
            log.warn("No active movies or halls found. Skipping showtime generation.");
            return;
        }

        LocalDate today = LocalDate.now();

        for (int i = 0; i <= 5; i++) {
            LocalDate targetDate = today.plusDays(i);

            for (int m = 0; m < activeMovies.size(); m++) {
                Movie movie = activeMovies.get(m);
                Hall hall = activeHalls.get(m % activeHalls.size());

                LocalDateTime time1 = targetDate.atTime(14, 0); // 2 PM
                LocalDateTime time2 = targetDate.atTime(19, 0); // 7 PM

                createShowIfMissing(movie, hall, time1,
                        new BigDecimal("200"), new BigDecimal("300"));

                createShowIfMissing(movie, hall, time2,
                        new BigDecimal("250"), new BigDecimal("350"));
            }
        }

        log.info("Automated showtime generation complete.");
    }

    /**
     * Prevent duplicate showtimes
     */
    private void createShowIfMissing(Movie movie, Hall hall,
            LocalDateTime time,
            BigDecimal std, BigDecimal prem) {

        List<com.cinebook.model.Showtime> existing = showtimeRepository.findByMovieAndDateRange(
                movie.getId(),
                time.minusMinutes(1),
                time.plusMinutes(1));

        if (existing.isEmpty()) {
            try {
                showtimeService.createShowtime(
                        ShowtimeDTO.builder()
                                .movieId(movie.getId())
                                .hallId(hall.getId())
                                .showDateTime(time)
                                .standardPrice(std)
                                .premiumPrice(prem)
                                .build());

                log.info("Created showtime for {} at {}", movie.getTitle(), time);

            } catch (Exception e) {
                log.warn("Failed to generate showtime for movie {}: {}",
                        movie.getTitle(), e.getMessage());
            }
        }
    }
}