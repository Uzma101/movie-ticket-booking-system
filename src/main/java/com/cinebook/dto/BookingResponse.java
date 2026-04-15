package com.cinebook.dto;
import com.cinebook.model.Booking;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingResponse {
    private Long id;
    private String bookingCode;
    private String movieTitle;
    private String hallName;
    private LocalDateTime showDateTime;
    private List<String> seatCodes;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal convenienceFee;
    private BigDecimal finalAmount;
    private String promoCode;
    private Booking.BookingStatus status;
    private Booking.PaymentMethod paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
}
