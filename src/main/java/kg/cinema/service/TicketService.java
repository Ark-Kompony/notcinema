package kg.cinema.service;

import kg.cinema.entity.Ticket;
import kg.cinema.entity.User;
import kg.cinema.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;

    /**
     * Get ticket by QR code
     */
    public Ticket getTicketByQRCode(String qrCode) {
        return ticketRepository.findByQrCode(qrCode)
            .orElseThrow(() -> new RuntimeException("Ticket not found with QR code: " + qrCode));
    }

    /**
     * Validate and scan ticket at entrance
     */
    @Transactional
    public Ticket scanTicket(String qrCode, User scannedBy) {
        Ticket ticket = getTicketByQRCode(qrCode);

        // Validate ticket status
        if (ticket.getStatus() != Ticket.TicketStatus.SOLD) {
            throw new RuntimeException("Ticket is not valid. Status: " + ticket.getStatus());
        }

        // Check if already scanned
        if (ticket.getScannedAt() != null) {
            throw new RuntimeException("Ticket has already been scanned at: " + ticket.getScannedAt());
        }

        // Check if showtime has started (allow entry 30 minutes before)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime allowedEntryTime = ticket.getShowtime().getStartTime().minusMinutes(30);

        if (now.isBefore(allowedEntryTime)) {
            throw new RuntimeException("Too early to enter. Showtime starts at: " +
                ticket.getShowtime().getStartTime());
        }

        // Check if showtime has ended
        if (now.isAfter(ticket.getShowtime().getEndTime())) {
            throw new RuntimeException("Showtime has already ended");
        }

        // Mark as scanned
        ticket.setScannedAt(now);
        ticket.setScannedBy(scannedBy);
        ticket.setStatus(Ticket.TicketStatus.USED);

        return ticketRepository.save(ticket);
    }

    /**
     * Get ticket details
     */
    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
    }
}
