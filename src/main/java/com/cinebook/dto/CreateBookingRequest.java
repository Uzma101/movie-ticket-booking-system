package com.cinebook.dto;
import com.cinebook.model.Booking;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class CreateBookingRequest {
    @NotNull private Long showtimeId;
    @NotEmpty @Size(max = 8) private List<String> seatCodes;
    private String promoCode;
    @NotNull private Booking.PaymentMethod paymentMethod;
    private String paymentTransactionId;
}
