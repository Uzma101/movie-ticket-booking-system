package com.cinebook.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.cinebook.dto.ShowtimeDTO;
import com.cinebook.model.Hall;
import com.cinebook.model.Movie;
import com.cinebook.model.User;
import com.cinebook.repository.HallRepository;
import com.cinebook.repository.MovieRepository;
import com.cinebook.repository.UserRepository;
import com.cinebook.service.impl.ShowtimeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

        private final MovieRepository movieRepository;
        private final HallRepository hallRepository;
        private final UserRepository userRepository;
        private final ShowtimeService showtimeService;
        private final PasswordEncoder passwordEncoder;

        private final com.cinebook.repository.ShowtimeRepository showtimeRepository;

        @Override
        public void run(String... args) {
                if (movieRepository.count() > 0) {
                        log.info("Database already seeded — skipping.");
                        java.util.List<com.cinebook.model.Showtime> pastShows = showtimeRepository.findAll().stream()
                                        .filter(s -> s.getShowDateTime().isBefore(LocalDateTime.now()))
                                        .toList();
                        if (!pastShows.isEmpty()) {
                                LocalDateTime base = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
                                int hoursToAdd = 2;
                                for (com.cinebook.model.Showtime s : pastShows) {
                                        s.setShowDateTime(base.plusHours(hoursToAdd));
                                        hoursToAdd += 4;
                                        showtimeRepository.save(s);
                                }
                                log.info("Updated {} past showtimes to future dates.", pastShows.size());
                        }
                        return;
                }

                log.info("Seeding database with sample data...");

                // ─── Admin User ───────────────────────────────────────────────────────
                User admin = User.builder()
                                .name("Admin")
                                .email("admin@cinebook.com")
                                .password(passwordEncoder.encode("admin123"))
                                .role(User.Role.ADMIN)
                                .build();
                userRepository.save(admin);

                // ─── Demo User ────────────────────────────────────────────────────────
                User demo = User.builder()
                                .name("Uzma Abshar")
                                .email("user@cinebook.com")
                                .password(passwordEncoder.encode("user123"))
                                .role(User.Role.USER)
                                .build();
                userRepository.save(demo);

                // ─── Halls ────────────────────────────────────────────────────────────
                Hall imax = hallRepository.save(Hall.builder()
                                .name("Hall 1 — IMAX").type("IMAX")
                                .totalRows(10).seatsPerRow(12).totalCapacity(120).active(true).build());

                Hall premium = hallRepository.save(Hall.builder()
                                .name("Hall 2 — Premium").type("PREMIUM")
                                .totalRows(8).seatsPerRow(10).totalCapacity(80).active(true).build());

                Hall standard = hallRepository.save(Hall.builder()
                                .name("Hall 3 — Standard").type("STANDARD")
                                .totalRows(10).seatsPerRow(12).totalCapacity(120).active(true).build());

                Hall dolby = hallRepository.save(Hall.builder()
                                .name("Hall 4 — Dolby Atmos").type("DOLBY")
                                .totalRows(8).seatsPerRow(10).totalCapacity(80).active(true).build());

                // ─── Movies ───────────────────────────────────────────────────────────
                Movie m1 = movieRepository.save(Movie.builder()
                                .title("Galactic Odyssey II").genre("Sci-Fi, Action")
                                .description("The epic continuation of humanity's journey across the stars.")
                                .durationMinutes(162).language("Hindi/English").format("IMAX")
                                .rating("UA").imdbRating(9.1).posterEmoji("🚀").active(true).build());

                Movie m2 = movieRepository.save(Movie.builder()
                                .title("Shadow Protocol").genre("Thriller, Action")
                                .description("A rogue intelligence agent races to prevent a global cyberattack.")
                                .durationMinutes(130).language("Hindi").format("Standard")
                                .rating("A").imdbRating(8.4).posterEmoji("🕵️").active(true).build());

                Movie m3 = movieRepository.save(Movie.builder()
                                .title("Ocean Echoes").genre("Drama, Romance")
                                .description("A heartfelt drama about love, loss, and finding meaning.")
                                .durationMinutes(118).language("English").format("Standard")
                                .rating("U").imdbRating(8.8).posterEmoji("🌊").active(true).build());

                Movie m4 = movieRepository.save(Movie.builder()
                                .title("Wild Kingdom").genre("Action, Adventure")
                                .description("Safari action meets political thriller across three continents.")
                                .durationMinutes(125).language("Hindi").format("IMAX")
                                .rating("UA").imdbRating(7.9).posterEmoji("🦁").active(true).build());

                Movie m5 = movieRepository.save(Movie.builder()
                                .title("The Big Mistake").genre("Comedy")
                                .description("A wedding planner accidentally books two weddings on the same day.")
                                .durationMinutes(110).language("Hindi").format("Standard")
                                .rating("U").imdbRating(7.5).posterEmoji("😂").active(true).build());

                Movie m6 = movieRepository.save(Movie.builder()
                                .title("Phantom Signal").genre("Sci-Fi, Thriller")
                                .description("Scientists intercept a message from deep space that changes everything.")
                                .durationMinutes(140).language("English").format("Dolby")
                                .rating("UA").imdbRating(8.6).posterEmoji("🔮").active(true).build());

                // ─── Showtimes ────────────────────────────────────────────────────────
                LocalDateTime base = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);

                // Galactic Odyssey — IMAX, multiple dates
                createShow(m1, imax, base.plusHours(2), new BigDecimal("350"), new BigDecimal("500"));
                createShow(m1, imax, base.plusHours(6), new BigDecimal("350"), new BigDecimal("500"));
                createShow(m1, imax, base.plusDays(1).withHour(10), new BigDecimal("350"), new BigDecimal("500"));
                createShow(m1, imax, base.plusDays(1).withHour(15), new BigDecimal("350"), new BigDecimal("500"));
                createShow(m1, imax, base.plusDays(2).withHour(12), new BigDecimal("350"), new BigDecimal("500"));

                // Shadow Protocol — Standard + Premium
                createShow(m2, standard, base.plusHours(1), new BigDecimal("200"), new BigDecimal("300"));
                createShow(m2, premium, base.plusHours(5), new BigDecimal("250"), new BigDecimal("350"));
                createShow(m2, standard, base.plusDays(1).withHour(14), new BigDecimal("200"), new BigDecimal("300"));

                // Ocean Echoes
                createShow(m3, premium, base.plusHours(3), new BigDecimal("200"), new BigDecimal("300"));
                createShow(m3, standard, base.plusDays(1).withHour(11), new BigDecimal("180"), new BigDecimal("280"));

                // Wild Kingdom — IMAX
                createShow(m4, imax, base.plusHours(4), new BigDecimal("300"), new BigDecimal("450"));
                createShow(m4, imax, base.plusDays(1).withHour(16), new BigDecimal("300"), new BigDecimal("450"));

                // Comedy
                createShow(m5, standard, base.plusHours(1), new BigDecimal("180"), new BigDecimal("260"));
                createShow(m5, standard, base.plusDays(1).withHour(13), new BigDecimal("180"), new BigDecimal("260"));

                // Phantom Signal — Dolby
                createShow(m6, dolby, base.plusHours(3), new BigDecimal("280"), new BigDecimal("380"));
                createShow(m6, dolby, base.plusDays(1).withHour(19), new BigDecimal("280"), new BigDecimal("380"));

                log.info("Database seeded successfully!");
                log.info("Admin login: admin@cinebook.com / admin123");
                log.info("User login:  user@cinebook.com  / user123");
        }

        private void createShow(Movie movie, Hall hall,
                        LocalDateTime time,
                        BigDecimal stdPrice, BigDecimal premPrice) {
                try {
                        showtimeService.createShowtime(ShowtimeDTO.builder()
                                        .movieId(movie.getId())
                                        .hallId(hall.getId())
                                        .showDateTime(time)
                                        .standardPrice(stdPrice)
                                        .premiumPrice(premPrice)
                                        .build());
                } catch (Exception e) {
                        log.warn("Failed to seed showtime: {}", e.getMessage());
                }
        }
}
