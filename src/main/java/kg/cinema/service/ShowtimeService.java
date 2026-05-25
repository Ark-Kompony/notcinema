package kg.cinema.service;

import kg.cinema.entity.Showtime;
import kg.cinema.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;

    public Showtime getShowtimeById(Long id) {
        return showtimeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Showtime not found with id: " + id));
    }

    public List<Showtime> getShowtimesByMovie(Long movieId) {
        return showtimeRepository.findByMovieIdAndIsActiveTrue(movieId);
    }

    public List<Showtime> getShowtimesByHall(Long hallId) {
        return showtimeRepository.findByHallIdAndIsActiveTrue(hallId);
    }

    public List<Showtime> getShowtimesByMovieAndDate(Long movieId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return showtimeRepository.findByMovieAndDateRange(movieId, startOfDay, endOfDay);
    }

    public List<Showtime> getShowtimesByCinemaAndDate(Long cinemaId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return showtimeRepository.findByCinemaAndDateRange(cinemaId, startOfDay, endOfDay);
    }

    public List<Showtime> getShowtimesByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return showtimeRepository.findByDateRange(start, end);
    }

    @Transactional
    public Showtime createShowtime(Showtime showtime) {
        // Check for conflicting showtimes in the same hall
        List<Showtime> conflicts = showtimeRepository.findConflictingShowtimes(
            showtime.getHall().getId(),
            showtime.getStartTime(),
            showtime.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Showtime conflicts with existing showtime in the same hall");
        }

        // Validate start time is before end time
        if (!showtime.getStartTime().isBefore(showtime.getEndTime())) {
            throw new RuntimeException("Start time must be before end time");
        }

        // Validate showtime is not in the past
        if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot create showtime in the past");
        }

        return showtimeRepository.save(showtime);
    }

    @Transactional
    public Showtime updateShowtime(Long id, Showtime showtimeDetails) {
        Showtime showtime = getShowtimeById(id);

        // Check for conflicts if time or hall changed
        if (!showtime.getHall().getId().equals(showtimeDetails.getHall().getId()) ||
            !showtime.getStartTime().equals(showtimeDetails.getStartTime()) ||
            !showtime.getEndTime().equals(showtimeDetails.getEndTime())) {

            List<Showtime> conflicts = showtimeRepository.findConflictingShowtimes(
                showtimeDetails.getHall().getId(),
                showtimeDetails.getStartTime(),
                showtimeDetails.getEndTime()
            );

            // Remove current showtime from conflicts
            conflicts.removeIf(s -> s.getId().equals(id));

            if (!conflicts.isEmpty()) {
                throw new RuntimeException("Showtime conflicts with existing showtime in the same hall");
            }
        }

        showtime.setMovie(showtimeDetails.getMovie());
        showtime.setHall(showtimeDetails.getHall());
        showtime.setStartTime(showtimeDetails.getStartTime());
        showtime.setEndTime(showtimeDetails.getEndTime());
        showtime.setLanguage(showtimeDetails.getLanguage());
        showtime.setSubtitles(showtimeDetails.getSubtitles());
        showtime.setBasePrice(showtimeDetails.getBasePrice());
        showtime.setIsActive(showtimeDetails.getIsActive());

        return showtimeRepository.save(showtime);
    }

    @Transactional
    public void deleteShowtime(Long id) {
        Showtime showtime = getShowtimeById(id);
        showtimeRepository.delete(showtime);
    }

    @Transactional
    public void deactivateShowtime(Long id) {
        Showtime showtime = getShowtimeById(id);
        showtime.setIsActive(false);
        showtimeRepository.save(showtime);
    }
}
