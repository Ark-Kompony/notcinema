package kg.cinema.controller;

import kg.cinema.service.PaymentService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create Stripe payment intent
     */
    @PostMapping("/create-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody Map<String, Long> request) {
        try {
            Long orderId = request.get("orderId");
            Map<String, String> response = paymentService.createPaymentIntent(orderId);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create payment intent: " + e.getMessage());
        }
    }

    /**
     * Stripe webhook handler
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload) {
        // In production, you should verify the webhook signature
        // For now, we'll just acknowledge receipt
        return ResponseEntity.ok("Webhook received");
    }

    /**
     * Confirm payment success (called after Stripe confirms)
     */
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmPayment(@RequestBody Map<String, String> request) {
        String paymentIntentId = request.get("paymentIntentId");
        paymentService.handleSuccessfulPayment(paymentIntentId);
        return ResponseEntity.ok().build();
    }

    /**
     * Process cash payment (for in-person purchases)
     */
    @PostMapping("/cash")
    public ResponseEntity<Void> processCashPayment(@RequestBody Map<String, Long> request) {
        Long orderId = request.get("orderId");
        paymentService.processCashPayment(orderId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get payment status
     */
    @GetMapping("/status/{orderId}")
    public ResponseEntity<Map<String, String>> getPaymentStatus(@PathVariable Long orderId) {
        var status = paymentService.getPaymentStatus(orderId);

        Map<String, String> response = Map.of(
            "status", status != null ? status.name() : "UNKNOWN"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Refund order
     */
    @PostMapping("/refund")
    public ResponseEntity<Void> refundOrder(@RequestBody Map<String, Object> request) {
        try {
            Long orderId = Long.parseLong(request.get("orderId").toString());
            String reason = (String) request.get("reason");
            paymentService.refundOrder(orderId, reason);
            return ResponseEntity.ok().build();
        } catch (StripeException e) {
            throw new RuntimeException("Failed to process refund: " + e.getMessage());
        }
    }
}
