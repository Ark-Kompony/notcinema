package kg.cinema.service;

import kg.cinema.entity.Order;
import kg.cinema.entity.PaymentTransaction;
import kg.cinema.entity.Ticket;
import kg.cinema.repository.OrderRepository;
import kg.cinema.repository.PaymentTransactionRepository;
import kg.cinema.repository.TicketRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final TicketRepository ticketRepository;
    private final LoyaltyService loyaltyService;
    private final NotificationService notificationService;

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @Value("${stripe.currency:KGS}")
    private String currency;

    /**
     * Create Stripe payment intent
     */
    @Transactional
    public Map<String, String> createPaymentIntent(Long orderId) throws StripeException {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getPaymentStatus() != Order.PaymentStatus.PENDING) {
            throw new RuntimeException("Order is not in pending status");
        }

        // Initialize Stripe
        Stripe.apiKey = stripeApiKey;

        // Convert amount to smallest currency unit (cents/tyiyn)
        long amountInCents = order.getFinalAmount()
            .multiply(BigDecimal.valueOf(100))
            .longValue();

        // Create payment intent
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(amountInCents)
            .setCurrency(currency.toLowerCase())
            .putMetadata("order_id", order.getId().toString())
            .putMetadata("user_id", order.getUser().getId().toString())
            .setDescription("Cinema tickets - Order #" + order.getId())
            .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        // Create payment transaction record
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrder(order);
        transaction.setProvider(PaymentTransaction.Provider.STRIPE);
        transaction.setBankTransactionId(paymentIntent.getId());
        transaction.setAmount(order.getFinalAmount());
        transaction.setStatus(PaymentTransaction.Status.PENDING);
        transaction.setRawResponse(paymentIntent.toJson());
        paymentTransactionRepository.save(transaction);

        // Update order payment method
        order.setPaymentMethod(Order.PaymentMethod.CARD);
        orderRepository.save(order);

        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", paymentIntent.getClientSecret());
        response.put("paymentIntentId", paymentIntent.getId());
        return response;
    }

    /**
     * Handle successful payment (called by webhook or after payment confirmation)
     */
    @Transactional
    public void handleSuccessfulPayment(String paymentIntentId) {
        PaymentTransaction transaction = paymentTransactionRepository.findByBankTransactionId(paymentIntentId)
            .orElseThrow(() -> new RuntimeException("Payment transaction not found"));

        Order order = transaction.getOrder();

        // Update transaction status
        transaction.setStatus(PaymentTransaction.Status.SUCCESS);
        paymentTransactionRepository.save(transaction);

        // Update order status
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);

        // Update tickets to SOLD
        List<Ticket> tickets = ticketRepository.findByOrderId(order.getId());
        for (Ticket ticket : tickets) {
            ticket.setStatus(Ticket.TicketStatus.SOLD);
            ticketRepository.save(ticket);
        }

        // Process loyalty cashback
        loyaltyService.processCashback(order);

        // Update LTV
        loyaltyService.updateLTV(order.getUser().getId(), order.getFinalAmount());

        // Redeem bonus if used
        if (order.getBonusUsed().compareTo(BigDecimal.ZERO) > 0) {
            loyaltyService.redeemBonus(order, order.getBonusUsed());
        }

        // Send notifications
        notificationService.sendOrderConfirmation(order);
        notificationService.sendTicketReady(order);
    }

    /**
     * Handle failed payment
     */
    @Transactional
    public void handleFailedPayment(String paymentIntentId, String errorMessage) {
        PaymentTransaction transaction = paymentTransactionRepository.findByBankTransactionId(paymentIntentId)
            .orElseThrow(() -> new RuntimeException("Payment transaction not found"));

        // Update transaction status
        transaction.setStatus(PaymentTransaction.Status.FAILED);
        transaction.setErrorMessage(errorMessage);
        paymentTransactionRepository.save(transaction);

        // Order remains in PENDING status - user can retry payment
    }

    /**
     * Process cash payment (for in-person purchases)
     */
    @Transactional
    public void processCashPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getPaymentStatus() != Order.PaymentStatus.PENDING) {
            throw new RuntimeException("Order is not in pending status");
        }

        // Create payment transaction record
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrder(order);
        transaction.setProvider(PaymentTransaction.Provider.CASH);
        transaction.setAmount(order.getFinalAmount());
        transaction.setStatus(PaymentTransaction.Status.SUCCESS);
        paymentTransactionRepository.save(transaction);

        // Update order
        order.setPaymentMethod(Order.PaymentMethod.CASH);
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);

        // Update tickets
        List<Ticket> tickets = ticketRepository.findByOrderId(order.getId());
        for (Ticket ticket : tickets) {
            ticket.setStatus(Ticket.TicketStatus.SOLD);
            ticketRepository.save(ticket);
        }

        // Process loyalty
        loyaltyService.processCashback(order);
        loyaltyService.updateLTV(order.getUser().getId(), order.getFinalAmount());

        if (order.getBonusUsed().compareTo(BigDecimal.ZERO) > 0) {
            loyaltyService.redeemBonus(order, order.getBonusUsed());
        }
    }

    /**
     * Get payment status
     */
    public PaymentTransaction.Status getPaymentStatus(Long orderId) {
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByOrderId(orderId);
        if (transactions.isEmpty()) {
            return null;
        }
        // Return latest transaction status
        return transactions.get(transactions.size() - 1).getStatus();
    }

    /**
     * Refund order
     */
    @Transactional
    public void refundOrder(Long orderId, String reason) throws StripeException {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getPaymentStatus() != Order.PaymentStatus.PAID) {
            throw new RuntimeException("Order is not paid");
        }

        // Find successful payment transaction
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByOrderId(orderId);
        PaymentTransaction successfulTransaction = transactions.stream()
            .filter(t -> t.getStatus() == PaymentTransaction.Status.SUCCESS)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No successful payment found"));

        // Process refund based on provider
        if (successfulTransaction.getProvider() == PaymentTransaction.Provider.STRIPE) {
            Stripe.apiKey = stripeApiKey;

            Map<String, Object> params = new HashMap<>();
            params.put("payment_intent", successfulTransaction.getBankTransactionId());

            com.stripe.model.Refund.create(params);
        }

        // Update transaction
        successfulTransaction.setStatus(PaymentTransaction.Status.REFUNDED);
        paymentTransactionRepository.save(successfulTransaction);

        // Update order
        order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
        order.setRefundReason(reason);
        orderRepository.save(order);

        // Update tickets
        List<Ticket> tickets = ticketRepository.findByOrderId(orderId);
        for (Ticket ticket : tickets) {
            ticket.setStatus(Ticket.TicketStatus.CANCELLED);
            ticketRepository.save(ticket);
        }

        // Reverse loyalty points (optional - depends on business logic)
        // For now, we keep the points as a goodwill gesture
    }
}
