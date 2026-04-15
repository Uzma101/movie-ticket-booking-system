package com.cinebook.service.impl;

import com.cinebook.dto.ShowtimeDTO;
import com.cinebook.exception.ResourceNotFoundException;
import com.cinebook.model.*;
import com.cinebook.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;

    public List<ShowtimeDTO> getShowtimesByMovie(Long movieId) {
        return showtimeRepository
                .findUpcomingByMovie(movieId, LocalDateTime.now())
                .stream().map(this::toDTO).toList();
    }

    public ShowtimeDTO getShowtimeById(Long id) {
        return showtimeRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found: " + id));
    }

    @Transactional
    public ShowtimeDTO createShowtime(ShowtimeDTO dto) {
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));
        Hall hall = hallRepository.findById(dto.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found"));

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .hall(hall)
                .showDateTime(dto.getShowDateTime())
                .standardPrice(dto.getStandardPrice())
                .premiumPrice(dto.getPremiumPrice())
                .active(true)
                .build();

        showtime = showtimeRepository.save(showtime);
        generateSeats(showtime, hall);
        return toDTO(showtime);
    }

    private void generateSeats(Showtime showtime, Hall hall) {
        String[] rowLabels = "ABCDEFGHJK".split("");
        int premiumRows = 2;

        for (int r = 0; r < hall.getTotalRows(); r++) {
            String rowLabel = rowLabels[Math.min(r, rowLabels.length - 1)];
            Seat.SeatType type = r < premiumRows ? Seat.SeatType.PREMIUM : Seat.SeatType.STANDARD;

            for (int n = 1; n <= hall.getSeatsPerRow(); n++) {
                Seat seat = Seat.builder()
                        .showtime(showtime)
                        .rowLabel(rowLabel)
                        .seatNumber(n)
                        .seatCode(rowLabel + n)
                        .seatType(type)
                        .status(Seat.SeatStatus.AVAILABLE)
                        .build();
                seatRepository.save(seat);
            }
        }
    }

    private ShowtimeDTO toDTO(Showtime s) {
        long available = seatRepository.countAvailableSeats(s.getId());
        long total = (long) s.getHall().getTotalRows() * s.getHall().getSeatsPerRow();

        return ShowtimeDTO.builder()
                .id(s.getId())
                .movieId(s.getMovie().getId())
                .movieTitle(s.getMovie().getTitle())
                .hallId(s.getHall().getId())
                .hallName(s.getHall().getName())
                .hallType(s.getHall().getType())
                .showDateTime(s.getShowDateTime())
                .standardPrice(s.getStandardPrice())
                .premiumPrice(s.getPremiumPrice())
                .availableSeats(available)
                .totalSeats(total)
                .build();
    }
}
