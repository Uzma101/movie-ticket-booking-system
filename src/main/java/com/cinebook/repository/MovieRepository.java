package com.cinebook.repository;

import com.cinebook.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByActiveTrue();

    List<Movie> findByGenreContainingIgnoreCaseAndActiveTrue(String genre);

    @Query("SELECT m FROM Movie m WHERE m.active = true AND " +
           "(LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.genre) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Movie> searchMovies(@Param("query") String query);

    List<Movie> findByFormatAndActiveTrue(String format);
}
