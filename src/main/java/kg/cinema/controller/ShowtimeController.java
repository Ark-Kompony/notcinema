package kg.cinema.controller;

import kg.cinema.dto.response.SeatResponse;
import kg.cinema.dto.response.ShowtimeResponse;
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
import java.util.stream.Collectors;

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
    public ResponseEntity<ShowtimeResponse> getShowtimeById(@PathVariable Long id) {
        Showtime showtime = showtimeService.getShowtimeById(id);
        return ResponseEntity.ok(convertToResponse(showtime));
    }

    /**
     * Get showtimes by movie
     */
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowtimeResponse>> getShowtimesByMovie(@PathVariable Long movieId) {
        List<Showtime> showtimes = showtimeService.getShowtimesByMovie(movieId);
        return ResponseEntity.ok(showtimes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList()));
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
    public ResponseEntity<List<SeatResponse>> getAvailableSeats(@PathVariable Long id) {
        List<Seat> seats = bookingService.getAvailableSeats(id);
        return ResponseEntity.ok(seats.stream()
                .map(this::convertSeatToResponse)
                .collect(Collectors.toList()));
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

    /**
     * Convert Showtime entity to ShowtimeResponse DTO
     */
    private ShowtimeResponse convertToResponse(Showtime showtime) {
        ShowtimeResponse response = new ShowtimeResponse();
        response.setId(showtime.getId());
        response.setMovieId(showtime.getMovie().getId());
        response.setMovieTitle(showtime.getMovie().getTitle());
        response.setStartTime(showtime.getStartTime());
        response.setEndTime(showtime.getEndTime());
        response.setLanguage(showtime.getLanguage().name());
        response.setSubtitles(showtime.getSubtitles().name());
        response.setBasePrice(showtime.getBasePrice());
        response.setIsActive(showtime.getIsActive());

        // Hall info
        ShowtimeResponse.HallInfo hallInfo = new ShowtimeResponse.HallInfo();
        hallInfo.setId(showtime.getHall().getId());
        hallInfo.setName(showtime.getHall().getName());
        hallInfo.setType(showtime.getHall().getType().name());

        // Cinema info
        ShowtimeResponse.CinemaInfo cinemaInfo = new ShowtimeResponse.CinemaInfo();
        cinemaInfo.setId(showtime.getHall().getCinema().getId());
        cinemaInfo.setName(showtime.getHall().getCinema().getName());
        cinemaInfo.setAddress(showtime.getHall().getCinema().getAddress());
        cinemaInfo.setCity(showtime.getHall().getCinema().getCity());

        hallInfo.setCinema(cinemaInfo);
        response.setHall(hallInfo);

        return response;
    }

    /**
     * Convert Seat entity to SeatResponse DTO
     */
    private SeatResponse convertSeatToResponse(Seat seat) {
        SeatResponse response = new SeatResponse();
        response.setId(seat.getId());
        response.setRowNumber(seat.getRowNumber());
        response.setSeatNumber(seat.getSeatNumber());
        response.setSeatType(seat.getSeatType().name());
        response.setIsAvailable(true); // Already filtered by getAvailableSeats
        return response;
    }
}
