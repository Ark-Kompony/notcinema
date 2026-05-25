package kg.cinema.service;

import kg.cinema.entity.*;
import kg.cinema.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final CartSessionRepository cartSessionRepository;
    private final OrderRepository orderRepository;
    private final PriceRepository priceRepository;
    private final PromocodeRepository promocodeRepository;
    private final LoyaltyService loyaltyService;

    @Value("${app.cart-session-timeout:10}")
    private int cartSessionTimeoutMinutes;

    /**
     * Check if a seat is available for booking
     */
    public boolean isSeatAvailable(Long showtimeId, Long seatId) {
        // Check if seat is already booked
        var bookedTicket = ticketRepository.findBookedTicketByShowtimeAndSeat(showtimeId, seatId);
        if (bookedTicket.isPresent()) {
            return false;
        }

        // Check if seat is reserved in an active cart session
        var activeReservation = cartSessionRepository.findActiveReservation(
            showtimeId, seatId, LocalDateTime.now()
        );
        return activeReservation.isEmpty();
    }

    /**
     * Get all available seats for a showtime
     */
    public List<Seat> getAvailableSeats(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
            .orElseThrow(() -> new RuntimeException("Showtime not found"));

        List<Seat> allSeats = seatRepository.findByHallIdAndIsActiveTrue(showtime.getHall().getId());
        List<Seat> availableSeats = new ArrayList<>();

        for (Seat seat : allSeats) {
            if (isSeatAvailable(showtimeId, seat.getId())) {
                availableSeats.add(seat);
            }
        }

        return availableSeats;
    }

    /**
     * Calculate ticket price based on showtime and seat type
     */
    public BigDecimal calculateTicketPrice(Long showtimeId, Long seatId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
            .orElseThrow(() -> new RuntimeException("Showtime not found"));

        Seat seat = seatRepository.findById(seatId)
            .orElseThrow(() -> new RuntimeException("Seat not found"));

        BigDecimal basePrice = showtime.getBasePrice();

        // Get price modifier for seat type
        var priceModifier = priceRepository.findByShowtimeIdAndSeatType(showtimeId, seat.getSeatType())
            .map(Price::getPriceModifier)
            .orElse(BigDecimal.ZERO);

        return basePrice.add(priceModifier);
    }

    /**
     * Reserve seats temporarily (add to cart)
     */
    @Transactional
    public List<CartSession> reserveSeats(User user, Long showtimeId, List<Long> seatIds) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
            .orElseThrow(() -> new RuntimeException("Showtime not found"));

        // Validate showtime is not in the past
        if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot book tickets for past showtimes");
        }

        List<CartSession> reservations = new ArrayList<>();
        String sessionToken = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(cartSessionTimeoutMinutes);

        for (Long seatId : seatIds) {
            // Check if seat is available
            if (!isSeatAvailable(showtimeId, seatId)) {
                throw new RuntimeException("Seat " + seatId + " is not available");
            }

            Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found: " + seatId));

            CartSession cartSession = new CartSession();
            cartSession.setUser(user);
            cartSession.setShowtime(showtime);
            cartSession.setSeat(seat);
            cartSession.setSessionToken(sessionToken);
            cartSession.setExpiresAt(expiresAt);
            cartSession.setStatus(CartSession.CartStatus.ACTIVE);

            reservations.add(cartSessionRepository.save(cartSession));
        }

        return reservations;
    }

    /**
     * Confirm booking and create order with tickets
     */
    @Transactional
    public Order confirmBooking(User user, String sessionToken, String promocodeStr, BigDecimal bonusToUse) {
        // Get cart sessions
        List<CartSession> cartSessions = cartSessionRepository.findBySessionToken(sessionToken);
        if (cartSessions.isEmpty()) {
            throw new RuntimeException("No cart sessions found");
        }

        // Validate all sessions are active and not expired
        for (CartSession session : cartSessions) {
            if (session.getStatus() != CartSession.CartStatus.ACTIVE) {
                throw new RuntimeException("Cart session is not active");
            }
            if (session.isExpired()) {
                throw new RuntimeException("Cart session has expired");
            }
            if (!session.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Cart session does not belong to user");
            }
        }

        // Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartSession session : cartSessions) {
            BigDecimal ticketPrice = calculateTicketPrice(
                session.getShowtime().getId(),
                session.getSeat().getId()
            );
            totalAmount = totalAmount.add(ticketPrice);
        }

        // Apply promocode if provided
        BigDecimal discountAmount = BigDecimal.ZERO;
        Promocode promocode = null;
        if (promocodeStr != null && !promocodeStr.isEmpty()) {
            promocode = promocodeRepository.findByCodeAndIsActiveTrue(promocodeStr)
                .orElseThrow(() -> new RuntimeException("Invalid promocode"));

            if (!promocode.isValid()) {
                throw new RuntimeException("Promocode is not valid or has expired");
            }

            if (promocode.getDiscountType() == Promocode.DiscountType.PERCENT) {
                discountAmount = totalAmount.multiply(promocode.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));
            } else {
                discountAmount = promocode.getDiscountValue();
            }
        }

        // Apply loyalty bonus
        BigDecimal bonusUsed = BigDecimal.ZERO;
        if (bonusToUse != null && bonusToUse.compareTo(BigDecimal.ZERO) > 0) {
            LoyaltyAccount loyaltyAccount = loyaltyService.getLoyaltyAccount(user.getId());
            if (loyaltyAccount.getBonusBalance().compareTo(bonusToUse) < 0) {
                throw new RuntimeException("Insufficient bonus balance");
            }
            bonusUsed = bonusToUse;
        }

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setPromocode(promocode);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setBonusUsed(bonusUsed);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);

        order = orderRepository.save(order);

        // Create tickets
        for (CartSession session : cartSessions) {
            BigDecimal ticketPrice = calculateTicketPrice(
                session.getShowtime().getId(),
                session.getSeat().getId()
            );

            Ticket ticket = new Ticket();
            ticket.setOrder(order);
            ticket.setShowtime(session.getShowtime());
            ticket.setSeat(session.getSeat());
            ticket.setPrice(ticketPrice);
            ticket.setStatus(Ticket.TicketStatus.RESERVED);
            ticket.setQrCode(generateQRCode(order.getId(), session.getSeat().getId()));

            ticketRepository.save(ticket);

            // Mark cart session as purchased
            session.setStatus(CartSession.CartStatus.PURCHASED);
            cartSessionRepository.save(session);
        }

        return order;
    }

    /**
     * Get user's orders
     */
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get order details
     */
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    /**
     * Get tickets for an order
     */
    public List<Ticket> getOrderTickets(Long orderId) {
        return ticketRepository.findByOrderId(orderId);
    }

    /**
     * Cancel order (only if not paid)
     */
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = getOrderById(orderId);

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Order does not belong to user");
        }

        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            throw new RuntimeException("Cannot cancel paid order");
        }

        order.setPaymentStatus(Order.PaymentStatus.CANCELLED);
        orderRepository.save(order);

        // Update tickets
        List<Ticket> tickets = ticketRepository.findByOrderId(orderId);
        for (Ticket ticket : tickets) {
            ticket.setStatus(Ticket.TicketStatus.CANCELLED);
            ticketRepository.save(ticket);
        }
    }

    /**
     * Clean up expired cart sessions
     */
    @Transactional
    public void cleanupExpiredSessions() {
        List<CartSession> expiredSessions = cartSessionRepository.findExpiredSessions(LocalDateTime.now());
        for (CartSession session : expiredSessions) {
            session.setStatus(CartSession.CartStatus.EXPIRED);
            cartSessionRepository.save(session);
        }
    }

    /**
     * Generate QR code string for ticket
     */
    private String generateQRCode(Long orderId, Long seatId) {
        return "TICKET-" + orderId + "-" + seatId + "-" + UUID.randomUUID().toString();
    }
}
