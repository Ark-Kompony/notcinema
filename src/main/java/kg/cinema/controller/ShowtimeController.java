package kg.cinema.controller;

import kg.cinema.entity.Seat;
import kg.cinema.entity.Showtime;
import kg.cinema.service.BookingService;
import kg.cinema.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;
    private final BookingService bookingService;

    /**
     * Get showtime by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Showtime> getShowtimeById(@PathVariable Long id) {
        return ResponseEntity.ok(showtimeService.getShowtimeById(id));
    }

    /**
     * Get showtimes by movie
     */
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Showtime>> getShowtimesByMovie(@PathVariable Long movieId) {
        return ResponseEntity.ok(showtimeService.getShowtimesByMovie(movieId));
    }

    /**
     * Get showtimes by movie and date
     */
    @GetMapping("/movie/{movieId}/date/{date}")
    public ResponseEntity<List<Showtime>> getShowtimesByMovieAndDate(
            @PathVariable Long movieId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showtimeService.getShowtimesByMovieAndDate(movieId, date));
    }

    /**
     * Get showtimes by cinema and date
     */
    @GetMapping("/cinema/{cinemaId}/date/{date}")
    public ResponseEntity<List<Showtime>> getShowtimesByCinemaAndDate(
            @PathVariable Long cinemaId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showtimeService.getShowtimesByCinemaAndDate(cinemaId, date));
    }

    /**
     * Get available seats for showtime
     */
    @GetMapping("/{id}/seats")
    public ResponseEntity<List<Seat>> getAvailableSeats(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getAvailableSeats(id));
    }

    /**
     * Create showtime (MANAGER/ADMIN only)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Showtime> createShowtime(@RequestBody Showtime showtime) {
        return ResponseEntity.ok(showtimeService.createShowtime(showtime));
    }

    /**
     * Update showtime (MANAGER/ADMIN only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Showtime> updateShowtime(@PathVariable Long id, @RequestBody Showtime showtime) {
        return ResponseEntity.ok(showtimeService.updateShowtime(id, showtime));
    }

    /**
     * Delete showtime (MANAGER/ADMIN only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteShowtime(@PathVariable Long id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deactivate showtime (MANAGER/ADMIN only)
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deactivateShowtime(@PathVariable Long id) {
        showtimeService.deactivateShowtime(id);
        return ResponseEntity.noContent().build();
    }
}
