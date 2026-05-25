package kg.cinema.service;

import kg.cinema.entity.LoyaltyAccount;
import kg.cinema.entity.LoyaltyTransaction;
import kg.cinema.entity.Order;
import kg.cinema.entity.User;
import kg.cinema.repository.LoyaltyAccountRepository;
import kg.cinema.repository.LoyaltyTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoyaltyService {

    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;

    @Value("${app.loyalty.cashback-percent:5.0}")
    private double cashbackPercent;

    @Value("${app.loyalty.bonus-to-discount-ratio:0.01}")
    private double bonusToDiscountRatio;

    /**
     * Get or create loyalty account for user
     */
    @Transactional
    public LoyaltyAccount getLoyaltyAccount(Long userId) {
        return loyaltyAccountRepository.findByUserId(userId)
            .orElseGet(() -> createLoyaltyAccount(userId));
    }

    /**
     * Create loyalty account for new user
     */
    @Transactional
    public LoyaltyAccount createLoyaltyAccount(Long userId) {
        LoyaltyAccount account = new LoyaltyAccount();
        account.setUser(new User());
        account.getUser().setId(userId);
        account.setLtv(BigDecimal.ZERO);
        account.setDiscountPct(0.0f);
        account.setBonusBalance(BigDecimal.ZERO);
        account.setUpdatedAt(LocalDateTime.now());
        return loyaltyAccountRepository.save(account);
    }

    /**
     * Process cashback when order is paid
     */
    @Transactional
    public void processCashback(Order order) {
        LoyaltyAccount account = getLoyaltyAccount(order.getUser().getId());

        // Calculate cashback amount
        BigDecimal cashbackAmount = order.getFinalAmount()
            .multiply(BigDecimal.valueOf(cashbackPercent))
            .divide(BigDecimal.valueOf(100));

        // Add to bonus balance
        account.setBonusBalance(account.getBonusBalance().add(cashbackAmount));
        account.setUpdatedAt(LocalDateTime.now());
        loyaltyAccountRepository.save(account);

        // Create transaction record
        LoyaltyTransaction transaction = new LoyaltyTransaction();
        transaction.setLoyaltyAccount(account);
        transaction.setOrder(order);
        transaction.setType(LoyaltyTransaction.TransactionType.ACCRUAL);
        transaction.setAmount(cashbackAmount);
        transaction.setDescription("Earned " + cashbackPercent + "% cashback on order #" + order.getId());
        loyaltyTransactionRepository.save(transaction);
    }

    /**
     * Redeem bonus points
     */
    @Transactional
    public void redeemBonus(Order order, BigDecimal bonusAmount) {
        LoyaltyAccount account = getLoyaltyAccount(order.getUser().getId());

        if (account.getBonusBalance().compareTo(bonusAmount) < 0) {
            throw new RuntimeException("Insufficient bonus balance");
        }

        // Deduct from bonus balance
        account.setBonusBalance(account.getBonusBalance().subtract(bonusAmount));
        account.setUpdatedAt(LocalDateTime.now());
        loyaltyAccountRepository.save(account);

        // Create transaction record
        LoyaltyTransaction transaction = new LoyaltyTransaction();
        transaction.setLoyaltyAccount(account);
        transaction.setOrder(order);
        transaction.setType(LoyaltyTransaction.TransactionType.REDEMPTION);
        transaction.setAmount(bonusAmount.negate());
        transaction.setDescription("Used bonus points on order #" + order.getId());
        loyaltyTransactionRepository.save(transaction);
    }

    /**
     * Update LTV and recalculate discount tier
     */
    @Transactional
    public void updateLTV(Long userId, BigDecimal amount) {
        LoyaltyAccount account = getLoyaltyAccount(userId);

        // Update LTV
        account.setLtv(account.getLtv().add(amount));

        // Recalculate discount tier
        float newDiscountPct = calculateDiscountTier(account.getLtv());
        account.setDiscountPct(newDiscountPct);

        account.setUpdatedAt(LocalDateTime.now());
        loyaltyAccountRepository.save(account);
    }

    /**
     * Calculate discount tier based on LTV
     */
    private float calculateDiscountTier(BigDecimal ltv) {
        // Bronze: 0-5000 KGS = 0% discount
        // Silver: 5000-15000 KGS = 5% discount
        // Gold: 15000-50000 KGS = 10% discount
        // Platinum: 50000+ KGS = 15% discount

        if (ltv.compareTo(BigDecimal.valueOf(50000)) >= 0) {
            return 15.0f;
        } else if (ltv.compareTo(BigDecimal.valueOf(15000)) >= 0) {
            return 10.0f;
        } else if (ltv.compareTo(BigDecimal.valueOf(5000)) >= 0) {
            return 5.0f;
        } else {
            return 0.0f;
        }
    }

    /**
     * Get loyalty transaction history
     */
    public List<LoyaltyTransaction> getTransactionHistory(Long userId) {
        LoyaltyAccount account = getLoyaltyAccount(userId);
        return loyaltyTransactionRepository.findByLoyaltyAccountIdOrderByCreatedAtDesc(account.getId());
    }

    /**
     * Get loyalty tier name
     */
    public String getLoyaltyTier(BigDecimal ltv) {
        if (ltv.compareTo(BigDecimal.valueOf(50000)) >= 0) {
            return "Platinum";
        } else if (ltv.compareTo(BigDecimal.valueOf(15000)) >= 0) {
            return "Gold";
        } else if (ltv.compareTo(BigDecimal.valueOf(5000)) >= 0) {
            return "Silver";
        } else {
            return "Bronze";
        }
    }
}
