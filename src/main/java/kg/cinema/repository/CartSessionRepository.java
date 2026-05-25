package kg.cinema.repository;

import kg.cinema.entity.CartSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartSessionRepository extends JpaRepository<CartSession, Long> {

    List<CartSession> findByUserIdAndStatus(Long userId, CartSession.CartStatus status);

    List<CartSession> findBySessionToken(String sessionToken);

    @Query("SELECT c FROM CartSession c WHERE c.showtime.id = :showtimeId AND c.seat.id = :seatId " +
           "AND c.status = 'ACTIVE' AND c.expiresAt > :now")
    Optional<CartSession> findActiveReservation(@Param("showtimeId") Long showtimeId,
                                                @Param("seatId") Long seatId,
                                                @Param("now") LocalDateTime now);

    @Query("SELECT c FROM CartSession c WHERE c.status = 'ACTIVE' AND c.expiresAt <= :now")
    List<CartSession> findExpiredSessions(@Param("now") LocalDateTime now);
}
