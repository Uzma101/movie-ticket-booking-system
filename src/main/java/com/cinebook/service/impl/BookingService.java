package com.cinebook.service.impl;

import com.cinebook.dto.*;
import com.cinebook.exception.*;
import com.cinebook.model.*;
import com.cinebook.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    private static final BigDecimal CONVENIENCE_FEE = new BigDecimal("30.00");

    private static final Map<String, BigDecimal> PROMO_CODES = Map.of(
            "SAVE10",    new BigDecimal("0.10"),
            "FIRST50",   new BigDecimal("0.50"),
            "WEEKEND20", new BigDecimal("0.20")
    );

    /**
     * Creates a confirmed booking.
     * Seats must already be LOCKED by this user (via POST /api/seats/lock).
     * Transitions seats: LOCKED → BOOKED atomically.
     */
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, Long userId) {
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Seat> seats = seatRepository.findBySeatCodesWithLock(
                request.getShowtimeId(), request.getSeatCodes());

        if (seats.size() != request.getSeatCodes().size()) {
            throw new BookingException("One or more seat codes are invalid");
        }

        // Validate all seats are locked by this user and not expired
        for (Seat seat : seats) {
            if (seat.getStatus() != Seat.SeatStatus.LOCKED) {
                throw new SeatNotAvailableException(
                        "Seat " + seat.getSeatCode() + " is not locked. Lock seats first via /api/seats/lock");
            }
            if (!userId.equals(seat.getLockedByUserId())) {
                throw new SeatNotAvailableException(
                        "Seat " + seat.getSeatCode() + " is locked by another user.");
            }
            if (seat.isLockExpired()) {
                throw new BookingException(
                        "Seat lock has expired. Please restart booking.");
            }
        }

        // ── Calculate pricing ─────────────────────────────────────────────
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Seat seat : seats) {
            BigDecimal price = seat.getSeatType() == Seat.SeatType.PREMIUM
                    ? showtime.getPremiumPrice()
                    : showtime.getStandardPrice();
            subtotal = subtotal.add(price);
        }

        BigDecimal convenienceFee = CONVENIENCE_FEE.multiply(new BigDecimal(seats.size()));
        BigDecimal totalBeforeDiscount = subtotal.add(convenienceFee);

        // ── Apply promo code ──────────────────────────────────────────────
        BigDecimal discount = BigDecimal.ZERO;
        String appliedPromo = null;
        if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
            BigDecimal rate = PROMO_CODES.get(request.getPromoCode().toUpperCase());
            if (rate != null) {
                discount = subtotal.multiply(rate);
                appliedPromo = request.getPromoCode().toUpperCase();
                log.info("Promo {} applied — discount ₹{}", appliedPromo, discount);
            }
        }

        BigDecimal finalAmount = totalBeforeDiscount.subtract(discount);

        // ── Create booking ────────────────────────────────────────────────
        Booking booking = Booking.builder()
                .bookingCode(generateBookingCode())
                .user(user)
                .showtime(showtime)
                .totalAmount(subtotal)
                .discountAmount(discount)
                .convenienceFee(convenienceFee)
                .finalAmount(finalAmount)
                .promoCode(appliedPromo)
                .status(Booking.BookingStatus.CONFIRMED)
                .paymentMethod(request.getPaymentMethod())
                .paymentTransactionId(request.getPaymentTransactionId())
                .confirmedAt(LocalDateTime.now())
                .build();

        booking = bookingRepository.save(booking);

        // ── Confirm seats ─────────────────────────────────────────────────
        for (Seat seat : seats) {
            seat.setStatus(Seat.SeatStatus.BOOKED);
            seat.setLockExpiresAt(null);
            seat.setLockedByUserId(null);
            seat.setBooking(booking);
        }
        seatRepository.saveAll(seats);

        log.info("Booking {} confirmed — user {}, {} seats, ₹{}",
                booking.getBookingCode(), userId, seats.size(), finalAmount);

        return toResponse(booking, seats);
    }

    public List<BookingResponse> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(b -> {
                    List<Seat> seats = seatRepository.findByBookingId(b.getId());
                    return toResponse(b, seats);
                }).toList();
    }

    public BookingResponse getBookingByCode(String code, Long userId) {
        Booking booking = bookingRepository.findByBookingCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + code));

        if (!booking.getUser().getId().equals(userId)) {
            throw new BookingException("Booking does not belong to this user");
        }
        List<Seat> seats = seatRepository.findByBookingId(booking.getId());
        return toResponse(booking, seats);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found or does not belong to you"));

        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new BookingException("Only CONFIRMED bookings can be cancelled");
        }

        if (booking.getShowtime().getShowDateTime()
                .isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BookingException(
                    "Cancellation not allowed within 2 hours of showtime");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Release seats back to AVAILABLE
        List<Seat> seats = seatRepository.findByBookingId(bookingId);
        for (Seat seat : seats) {
            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            seat.setBooking(null);
        }
        seatRepository.saveAll(seats);

        log.info("Booking {} cancelled by user {}", booking.getBookingCode(), userId);
        return toResponse(booking, seats);
    }

    public Map<String, Object> getAnalytics() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("todayBookings", bookingRepository.countTodayConfirmedBookings(startOfDay, endOfDay));
        Double rev = bookingRepository.todayRevenue(startOfDay, endOfDay);
        stats.put("todayRevenue", rev != null ? rev : 0.0);
        stats.put("totalBookings", bookingRepository.count());
        return stats;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String generateBookingCode() {
        return "CB" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 6).toUpperCase();
    }

    private BookingResponse toResponse(Booking b, List<Seat> seats) {
        return BookingResponse.builder()
                .id(b.getId())
                .bookingCode(b.getBookingCode())
                .movieTitle(b.getShowtime().getMovie().getTitle())
                .hallName(b.getShowtime().getHall().getName())
                .showDateTime(b.getShowtime().getShowDateTime())
                .seatCodes(seats.stream().map(Seat::getSeatCode).toList())
                .totalAmount(b.getTotalAmount())
                .discountAmount(b.getDiscountAmount())
                .convenienceFee(b.getConvenienceFee())
                .finalAmount(b.getFinalAmount())
                .promoCode(b.getPromoCode())
                .status(b.getStatus())
                .paymentMethod(b.getPaymentMethod())
                .createdAt(b.getCreatedAt())
                .confirmedAt(b.getConfirmedAt())
                .build();
    }
}
