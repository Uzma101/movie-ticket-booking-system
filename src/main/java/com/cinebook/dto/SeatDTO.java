package com.cinebook.dto;
import com.cinebook.model.Seat;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatDTO {
    private Long id;
    private String seatCode;
    private String rowLabel;
    private Integer seatNumber;
    private Seat.SeatType seatType;
    private Seat.SeatStatus status;
}
