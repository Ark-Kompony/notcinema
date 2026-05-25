package kg.cinema.repository;

import kg.cinema.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByPaymentStatus(Order.PaymentStatus status);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.paymentStatus = :status")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId,
                                      @Param("status") Order.PaymentStatus status);

    @Query("SELECT o FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt < :endDate " +
           "AND o.paymentStatus = 'PAID'")
    List<Order> findPaidOrdersByDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
}
