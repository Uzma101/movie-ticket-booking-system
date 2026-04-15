package com.cinebook.repository;

import com.cinebook.model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    @Query("SELECT s FROM Showtime s WHERE s.movie.id = :movieId " +
           "AND s.showDateTime > :after AND s.active = true ORDER BY s.showDateTime ASC")
    List<Showtime> findUpcomingByMovie(@Param("movieId") Long movieId,
                                       @Param("after") LocalDateTime after);

    @Query("SELECT s FROM Showtime s WHERE s.movie.id = :movieId " +
           "AND s.showDateTime BETWEEN :start AND :end AND s.active = true")
    List<Showtime> findByMovieAndDateRange(@Param("movieId") Long movieId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);
}
