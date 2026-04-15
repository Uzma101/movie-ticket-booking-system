package com.cinebook.repository;

import com.cinebook.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Booking> findByBookingCode(String bookingCode);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CONFIRMED' " +
           "AND b.createdAt >= :start AND b.createdAt < :end")
    long countTodayConfirmedBookings(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(b.finalAmount) FROM Booking b WHERE b.status = 'CONFIRMED' " +
           "AND b.createdAt >= :start AND b.createdAt < :end")
    Double todayRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
