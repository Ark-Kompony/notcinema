package kg.cinema.controller;

import kg.cinema.entity.Ticket;
import kg.cinema.entity.User;
import kg.cinema.service.AuthService;
import kg.cinema.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final AuthService authService;

    /**
     * Get ticket by QR code
     */
    @GetMapping("/qr/{qrCode}")
    public ResponseEntity<Ticket> getTicketByQRCode(@PathVariable String qrCode) {
        return ResponseEntity.ok(ticketService.getTicketByQRCode(qrCode));
    }

    /**
     * Scan ticket at entrance (MANAGER/ADMIN only)
     */
    @PostMapping("/scan")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> scanTicket(@RequestBody Map<String, String> request) {
        String qrCode = request.get("qrCode");
        User scannedBy = authService.getCurrentUser();

        Ticket ticket = ticketService.scanTicket(qrCode, scannedBy);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Ticket scanned successfully");
        response.put("ticket", ticket);

        return ResponseEntity.ok(response);
    }

    /**
     * Get ticket details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Long id) {
        Ticket ticket = ticketService.getTicketById(id);

        // Verify ticket belongs to current user or user is manager/admin
        Long currentUserId = authService.getCurrentUserId();
        if (!ticket.getOrder().getUser().getId().equals(currentUserId) && !authService.isManagerOrAdmin()) {
            throw new RuntimeException("Access denied");
        }

        return ResponseEntity.ok(ticket);
    }
}
