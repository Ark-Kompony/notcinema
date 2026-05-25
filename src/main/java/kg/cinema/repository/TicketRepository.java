package kg.cinema.repository;

import kg.cinema.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByOrderId(Long orderId);

    Optional<Ticket> findByQrCode(String qrCode);

    @Query("SELECT t FROM Ticket t WHERE t.showtime.id = :showtimeId AND t.status IN ('RESERVED', 'SOLD', 'USED')")
    List<Ticket> findBookedTicketsByShowtime(@Param("showtimeId") Long showtimeId);

    @Query("SELECT t FROM Ticket t WHERE t.showtime.id = :showtimeId AND t.seat.id = :seatId " +
           "AND t.status IN ('RESERVED', 'SOLD', 'USED')")
    Optional<Ticket> findBookedTicketByShowtimeAndSeat(@Param("showtimeId") Long showtimeId,
                                                        @Param("seatId") Long seatId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.showtime.id = :showtimeId AND t.status IN ('SOLD', 'USED')")
    Long countSoldTicketsByShowtime(@Param("showtimeId") Long showtimeId);
}
