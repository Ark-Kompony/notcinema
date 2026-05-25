package kg.cinema.controller;

import kg.cinema.entity.LoyaltyAccount;
import kg.cinema.entity.LoyaltyTransaction;
import kg.cinema.service.AuthService;
import kg.cinema.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;
    private final AuthService authService;

    /**
     * Get user's loyalty account
     */
    @GetMapping("/account")
    public ResponseEntity<Map<String, Object>> getLoyaltyAccount() {
        Long userId = authService.getCurrentUserId();
        LoyaltyAccount account = loyaltyService.getLoyaltyAccount(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("account", account);
        response.put("tier", loyaltyService.getLoyaltyTier(account.getLtv()));

        return ResponseEntity.ok(response);
    }

    /**
     * Get loyalty transaction history
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<LoyaltyTransaction>> getTransactionHistory() {
        Long userId = authService.getCurrentUserId();
        return ResponseEntity.ok(loyaltyService.getTransactionHistory(userId));
    }

    /**
     * Get loyalty tier info
     */
    @GetMapping("/tier")
    public ResponseEntity<Map<String, Object>> getTierInfo() {
        Long userId = authService.getCurrentUserId();
        LoyaltyAccount account = loyaltyService.getLoyaltyAccount(userId);

        String currentTier = loyaltyService.getLoyaltyTier(account.getLtv());

        Map<String, Object> response = new HashMap<>();
        response.put("currentTier", currentTier);
        response.put("ltv", account.getLtv());
        response.put("discountPct", account.getDiscountPct());
        response.put("bonusBalance", account.getBonusBalance());

        // Add tier thresholds
        Map<String, Integer> tiers = new HashMap<>();
        tiers.put("Bronze", 0);
        tiers.put("Silver", 5000);
        tiers.put("Gold", 15000);
        tiers.put("Platinum", 50000);
        response.put("tiers", tiers);

        return ResponseEntity.ok(response);
    }
}
