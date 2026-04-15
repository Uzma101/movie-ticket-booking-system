package com.cinebook.repository;

import com.cinebook.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByShowtimeIdOrderByRowLabelAscSeatNumberAsc(Long showtimeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.showtime.id = :showtimeId AND s.seatCode IN :codes")
    List<Seat> findBySeatCodesWithLock(@Param("showtimeId") Long showtimeId,
                                       @Param("codes") List<String> codes);

    @Query("SELECT s FROM Seat s WHERE s.showtime.id = :showtimeId AND s.seatCode IN :codes")
    List<Seat> findBySeatCodes(@Param("showtimeId") Long showtimeId,
                               @Param("codes") List<String> codes);

    @Modifying
    @Query("UPDATE Seat s SET s.status = 'AVAILABLE', s.lockedByUserId = null, " +
           "s.lockExpiresAt = null WHERE s.status = 'LOCKED' AND s.lockExpiresAt < :now")
    int releaseExpiredLocks(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.showtime.id = :showtimeId AND s.status = 'AVAILABLE'")
    long countAvailableSeats(@Param("showtimeId") Long showtimeId);

    @Query("SELECT s FROM Seat s WHERE s.booking.id = :bookingId")
    List<Seat> findByBookingId(@Param("bookingId") Long bookingId);
}
