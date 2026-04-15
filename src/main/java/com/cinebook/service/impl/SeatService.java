package com.cinebook.service.impl;

import com.cinebook.dto.SeatDTO;
import com.cinebook.exception.*;
import com.cinebook.model.Seat;
import com.cinebook.repository.SeatRepository;
import com.cinebook.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatService {

    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;

    @Value("${app.seat-lock.ttl-minutes}")
    private int lockTtlMinutes;

    public List<SeatDTO> getSeatMap(Long showtimeId) {
        showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found: " + showtimeId));
        return seatRepository.findByShowtimeIdOrderByRowLabelAscSeatNumberAsc(showtimeId)
                .stream().map(this::toDTO).toList();
    }

    /**
     * Concurrency-safe seat locking.
     * Uses SELECT FOR UPDATE (PESSIMISTIC_WRITE) to block concurrent
     * transactions from acquiring the same seats simultaneously.
     */
    @Transactional
    public List<SeatDTO> lockSeats(Long showtimeId, List<String> seatCodes, Long userId) {
        if (seatCodes.size() > 8) {
            throw new BookingException("Cannot lock more than 8 seats at once");
        }

        // PESSIMISTIC_WRITE: blocks any other transaction trying to update same rows
        List<Seat> seats = seatRepository.findBySeatCodesWithLock(showtimeId, seatCodes);

        if (seats.size() != seatCodes.size()) {
            throw new ResourceNotFoundException("One or more seat codes are invalid");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(lockTtlMinutes);

        for (Seat seat : seats) {
            // Auto-release expired lock before checking
            if (seat.getStatus() == Seat.SeatStatus.LOCKED && seat.isLockExpired()) {
                log.info("Auto-releasing expired lock on seat {}", seat.getSeatCode());
                seat.setStatus(Seat.SeatStatus.AVAILABLE);
                seat.setLockedByUserId(null);
                seat.setLockExpiresAt(null);
            }

            if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
                throw new SeatNotAvailableException(
                        "Seat " + seat.getSeatCode() + " is not available (status: " + seat.getStatus() + ")");
            }

            seat.setStatus(Seat.SeatStatus.LOCKED);
            seat.setLockedByUserId(userId);
            seat.setLockExpiresAt(expiresAt);
        }

        List<Seat> saved = seatRepository.saveAll(seats);
        log.info("User {} locked {} seats for showtime {} until {}",
                userId, seatCodes.size(), showtimeId, expiresAt);
        return saved.stream().map(this::toDTO).toList();
    }

    @Transactional
    public void unlockSeats(Long showtimeId, List<String> seatCodes, Long userId) {
        List<Seat> seats = seatRepository.findBySeatCodes(showtimeId, seatCodes);
        for (Seat seat : seats) {
            if (seat.getStatus() == Seat.SeatStatus.LOCKED
                    && userId.equals(seat.getLockedByUserId())) {
                seat.setStatus(Seat.SeatStatus.AVAILABLE);
                seat.setLockedByUserId(null);
                seat.setLockExpiresAt(null);
            }
        }
        seatRepository.saveAll(seats);
        log.info("User {} unlocked {} seats for showtime {}", userId, seatCodes.size(), showtimeId);
    }

    // Release expired locks every 2 minutes automatically
    @Scheduled(fixedDelay = 120_000)
    @Transactional
    public void releaseExpiredLocks() {
        int released = seatRepository.releaseExpiredLocks(LocalDateTime.now());
        if (released > 0) {
            log.info("Scheduler released {} expired seat locks", released);
        }
    }

    private SeatDTO toDTO(Seat s) {
        return SeatDTO.builder()
                .id(s.getId())
                .seatCode(s.getSeatCode())
                .rowLabel(s.getRowLabel())
                .seatNumber(s.getSeatNumber())
                .seatType(s.getSeatType())
                .status(s.getStatus())
                .build();
    }
}
