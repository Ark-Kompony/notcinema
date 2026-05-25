package kg.cinema.repository;

import kg.cinema.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByHallId(Long hallId);

    List<Seat> findByHallIdAndIsActiveTrue(Long hallId);

    @Query("SELECT s FROM Seat s WHERE s.hall.id = :hallId AND s.rowNumber = :rowNumber ORDER BY s.seatNumber")
    List<Seat> findByHallIdAndRowNumber(@Param("hallId") Long hallId, @Param("rowNumber") Integer rowNumber);
}
