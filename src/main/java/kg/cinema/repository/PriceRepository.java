package kg.cinema.repository;

import kg.cinema.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

    List<Price> findByShowtimeId(Long showtimeId);

    @Query("SELECT p FROM Price p WHERE p.showtime.id = :showtimeId AND p.seatType = :seatType")
    Optional<Price> findByShowtimeIdAndSeatType(@Param("showtimeId") Long showtimeId,
                                                 @Param("seatType") kg.cinema.entity.Seat.SeatType seatType);
}
