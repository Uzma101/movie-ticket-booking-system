package com.cinebook.dto;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ShowtimeDTO {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private Long hallId;
    private String hallName;
    private String hallType;
    private LocalDateTime showDateTime;
    private BigDecimal standardPrice;
    private BigDecimal premiumPrice;
    private long availableSeats;
    private long totalSeats;
}
