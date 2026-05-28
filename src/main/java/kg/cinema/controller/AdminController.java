package kg.cinema.controller;

import kg.cinema.dto.response.UserResponse;
import kg.cinema.entity.Order;
import kg.cinema.entity.Promocode;
import kg.cinema.entity.User;
import kg.cinema.repository.OrderRepository;
import kg.cinema.repository.TicketRepository;
import kg.cinema.service.PromocodeService;
import kg.cinema.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final PromocodeService promocodeService;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;

    /**
     * Get dashboard statistics
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> stats = new HashMap<>();

        // Get today's stats
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<Order> todayOrders = orderRepository.findPaidOrdersByDateRange(startOfDay, endOfDay);

        stats.put("todayOrders", todayOrders.size());
        stats.put("todayRevenue", todayOrders.stream()
            .map(Order::getFinalAmount)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));

        // Total stats
        stats.put("totalUsers", userService.getAllUsers().size());
        stats.put("totalOrders", orderRepository.findByPaymentStatus(Order.PaymentStatus.PAID).size());

        return ResponseEntity.ok(stats);
    }

    /**
     * Get revenue report
     */
    @GetMapping("/reports/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Order> orders = orderRepository.findPaidOrdersByDateRange(start, end);

        java.math.BigDecimal totalRevenue = orders.stream()
            .map(Order::getFinalAmount)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalOrders", orders.size());
        report.put("totalRevenue", totalRevenue);
        report.put("orders", orders);

        return ResponseEntity.ok(report);
    }

    /**
     * Get all users
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponse> response = users.stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Update user role
     */
    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        String role = request.get("role");
        userService.updateRole(userId, User.UserRole.valueOf(role));
        return ResponseEntity.ok().build();
    }

    /**
     * Deactivate user
     */
    @PatchMapping("/users/{userId}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Activate user
     */
    @PatchMapping("/users/{userId}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long userId) {
        userService.activateUser(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get all promocodes
     */
    @GetMapping("/promocodes")
    public ResponseEntity<List<Promocode>> getAllPromocodes() {
        return ResponseEntity.ok(promocodeService.getAllPromocodes());
    }

    /**
     * Create promocode
     */
    @PostMapping("/promocodes")
    public ResponseEntity<Promocode> createPromocode(@RequestBody Promocode promocode) {
        return ResponseEntity.ok(promocodeService.createPromocode(promocode));
    }

    /**
     * Update promocode
     */
    @PutMapping("/promocodes/{id}")
    public ResponseEntity<Promocode> updatePromocode(
            @PathVariable Long id,
            @RequestBody Promocode promocode) {
        return ResponseEntity.ok(promocodeService.updatePromocode(id, promocode));
    }

    /**
     * Deactivate promocode
     */
    @PatchMapping("/promocodes/{id}/deactivate")
    public ResponseEntity<Void> deactivatePromocode(@PathVariable Long id) {
        promocodeService.deactivatePromocode(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Get all orders
     */
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    /**
     * Get orders by status
     */
    @GetMapping("/orders/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status) {
        Order.PaymentStatus paymentStatus = Order.PaymentStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(orderRepository.findByPaymentStatus(paymentStatus));
    }
}
