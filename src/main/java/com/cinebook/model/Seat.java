package com.cinebook.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seats",
       uniqueConstraints = @UniqueConstraint(columnNames = {"showtime_id", "row_label", "seat_number"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @Column(nullable = false, length = 5)
    private String rowLabel;

    @Column(nullable = false)
    private Integer seatNumber;

    @Column(nullable = false, length = 10)
    private String seatCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SeatType seatType = SeatType.STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Version
    private Long version;

    @Column(name = "locked_by_user_id")
    private Long lockedByUserId;

    @Column(name = "lock_expires_at")
    private LocalDateTime lockExpiresAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    public boolean isLockExpired() {
        return lockExpiresAt != null && LocalDateTime.now().isAfter(lockExpiresAt);
    }

    public enum SeatType  { STANDARD, PREMIUM }
    public enum SeatStatus { AVAILABLE, LOCKED, BOOKED }
}
