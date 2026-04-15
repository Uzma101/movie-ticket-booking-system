package com.cinebook.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MovieDTO {
    private Long id;
    private String title;
    private String description;
    private String genre;
    private Integer durationMinutes;
    private String language;
    private String format;
    private String rating;
    private Double imdbRating;
    private String posterEmoji;
}
