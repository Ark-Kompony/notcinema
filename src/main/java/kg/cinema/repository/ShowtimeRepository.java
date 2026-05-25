package kg.cinema.repository;

import kg.cinema.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    List<Showtime> findByMovieIdAndIsActiveTrue(Long movieId);

    List<Showtime> findByHallIdAndIsActiveTrue(Long hallId);

    @Query("SELECT s FROM Showtime s WHERE s.movie.id = :movieId " +
           "AND s.startTime >= :startDate AND s.startTime < :endDate " +
           "AND s.isActive = true ORDER BY s.startTime")
    List<Showtime> findByMovieAndDateRange(@Param("movieId") Long movieId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM Showtime s WHERE s.hall.cinema.id = :cinemaId " +
           "AND s.startTime >= :startDate AND s.startTime < :endDate " +
           "AND s.isActive = true ORDER BY s.startTime")
    List<Showtime> findByCinemaAndDateRange(@Param("cinemaId") Long cinemaId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM Showtime s WHERE s.startTime >= :startDate AND s.startTime < :endDate " +
           "AND s.isActive = true ORDER BY s.startTime")
    List<Showtime> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM Showtime s WHERE s.hall.id = :hallId " +
           "AND s.startTime < :endTime AND s.endTime > :startTime")
    List<Showtime> findConflictingShowtimes(@Param("hallId") Long hallId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);
}
