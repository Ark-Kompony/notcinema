package kg.cinema.repository;

import kg.cinema.entity.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {

    List<LoyaltyTransaction> findByLoyaltyAccountIdOrderByCreatedAtDesc(Long loyaltyAccountId);

    List<LoyaltyTransaction> findByOrderId(Long orderId);
}
