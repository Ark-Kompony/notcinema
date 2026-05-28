package kg.cinema.controller;

import kg.cinema.dto.request.BookingRequest;
import kg.cinema.entity.CartSession;
import kg.cinema.entity.Order;
import kg.cinema.entity.Ticket;
import kg.cinema.entity.User;
import kg.cinema.service.AuthService;
import kg.cinema.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final AuthService authService;

    /**
     * Reserve seats (add to cart)
     */
    @PostMapping("/reserve")
    public ResponseEntity<Map<String, Object>> reserveSeats(@Valid @RequestBody BookingRequest request) {
        User user = authService.getCurrentUser();

        List<CartSession> reservations = bookingService.reserveSeats(
            user,
            request.getShowtimeId(),
            request.getSeatIds()
        );

        // Calculate total amount
        java.math.BigDecimal totalAmount = reservations.stream()
            .map(r -> bookingService.calculateTicketPrice(r.getShowtime().getId(), r.getSeat().getId()))
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        Map<String, Object> response = new HashMap<>();
        response.put("sessionToken", reservations.get(0).getSessionToken());
        response.put("expiresAt", reservations.get(0).getExpiresAt());
        response.put("seatCount", reservations.size());
        response.put("totalAmount", totalAmount);
        response.put("message", "Seats reserved successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Confirm booking (create order)
     */
    @PostMapping("/confirm")
    public ResponseEntity<Order> confirmBooking(@RequestBody Map<String, Object> request) {
        User user = authService.getCurrentUser();

        String sessionToken = (String) request.get("sessionToken");
        String promocode = (String) request.get("promocode");

        // Parse bonusToUse if present
        java.math.BigDecimal bonusToUse = null;
        if (request.containsKey("bonusToUse")) {
            bonusToUse = new java.math.BigDecimal(request.get("bonusToUse").toString());
        }

        Order order = bookingService.confirmBooking(user, sessionToken, promocode, bonusToUse);
        return ResponseEntity.ok(order);
    }

    /**
     * Get user's orders
     */
    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders() {
        Long userId = authService.getCurrentUserId();
        return ResponseEntity.ok(bookingService.getUserOrders(userId));
    }

    /**
     * Get order details
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        Order order = bookingService.getOrderById(orderId);

        // Verify order belongs to current user
        Long currentUserId = authService.getCurrentUserId();
        if (!order.getUser().getId().equals(currentUserId) && !authService.isManagerOrAdmin()) {
            throw new RuntimeException("Access denied");
        }

        return ResponseEntity.ok(order);
    }

    /**
     * Get tickets for an order
     */
    @GetMapping("/orders/{orderId}/tickets")
    public ResponseEntity<List<Ticket>> getOrderTickets(@PathVariable Long orderId) {
        Order order = bookingService.getOrderById(orderId);

        // Verify order belongs to current user
        Long currentUserId = authService.getCurrentUserId();
        if (!order.getUser().getId().equals(currentUserId) && !authService.isManagerOrAdmin()) {
            throw new RuntimeException("Access denied");
        }

        return ResponseEntity.ok(bookingService.getOrderTickets(orderId));
    }

    /**
     * Cancel order
     */
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        Long userId = authService.getCurrentUserId();
        bookingService.cancelOrder(orderId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check seat availability
     */
    @GetMapping("/check-availability")
    public ResponseEntity<Map<String, Boolean>> checkAvailability(
            @RequestParam Long showtimeId,
            @RequestParam Long seatId) {
        boolean available = bookingService.isSeatAvailable(showtimeId, seatId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);

        return ResponseEntity.ok(response);
    }
}
