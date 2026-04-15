package com.cinebook.dto;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class LockSeatsRequest {
    @NotNull private Long showtimeId;
    @NotEmpty @Size(max = 8, message = "Maximum 8 seats per booking")
    private List<String> seatCodes;
}
