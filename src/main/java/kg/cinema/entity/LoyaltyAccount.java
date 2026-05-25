package kg.cinema.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal ltv = BigDecimal.ZERO;

    @Column(name = "discount_pct", nullable = false)
    private Float discountPct = 0.0f;

    @Column(name = "bonus_balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal bonusBalance = BigDecimal.ZERO;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
