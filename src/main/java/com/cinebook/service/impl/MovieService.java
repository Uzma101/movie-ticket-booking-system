package com.cinebook.service.impl;

import com.cinebook.dto.MovieDTO;
import com.cinebook.exception.ResourceNotFoundException;
import com.cinebook.model.Movie;
import com.cinebook.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    public List<MovieDTO> getAllMovies() {
        return movieRepository.findByActiveTrue().stream()
                .map(this::toDTO).toList();
    }

    public MovieDTO getMovieById(Long id) {
        return movieRepository.findById(id)
                .filter(Movie::getActive)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + id));
    }

    public List<MovieDTO> searchMovies(String query) {
        return movieRepository.searchMovies(query).stream()
                .map(this::toDTO).toList();
    }

    public List<MovieDTO> getMoviesByGenre(String genre) {
        return movieRepository.findByGenreContainingIgnoreCaseAndActiveTrue(genre).stream()
                .map(this::toDTO).toList();
    }

    @Transactional
    public MovieDTO createMovie(MovieDTO dto) {
        Movie movie = Movie.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .genre(dto.getGenre())
                .durationMinutes(dto.getDurationMinutes())
                .language(dto.getLanguage())
                .format(dto.getFormat())
                .rating(dto.getRating())
                .imdbRating(dto.getImdbRating())
                .posterEmoji(dto.getPosterEmoji())
                .active(true)
                .build();
        return toDTO(movieRepository.save(movie));
    }

    private MovieDTO toDTO(Movie m) {
        return MovieDTO.builder()
                .id(m.getId())
                .title(m.getTitle())
                .description(m.getDescription())
                .genre(m.getGenre())
                .durationMinutes(m.getDurationMinutes())
                .language(m.getLanguage())
                .format(m.getFormat())
                .rating(m.getRating())
                .imdbRating(m.getImdbRating())
                .posterEmoji(m.getPosterEmoji())
                .build();
    }
}
