package kg.cinema.service;

import kg.cinema.entity.Notification;
import kg.cinema.entity.Order;
import kg.cinema.entity.User;
import kg.cinema.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Send order confirmation notification
     */
    @Transactional
    public void sendOrderConfirmation(Order order) {
        Notification notification = new Notification();
        notification.setUser(order.getUser());
        notification.setType(Notification.NotificationType.ORDER_CONFIRMED);
        notification.setChannel(Notification.NotificationChannel.EMAIL);
        notification.setTitle("Order Confirmed");
        notification.setBody("Your order #" + order.getId() + " has been confirmed. " +
                           "Total: " + order.getFinalAmount() + " KGS");
        notification.setIsSent(false);

        notificationRepository.save(notification);

        // In a real application, this would trigger actual email sending
        // For now, we just log it
        log.info("Order confirmation notification created for user: {}", order.getUser().getEmail());
    }

    /**
     * Send ticket ready notification
     */
    @Transactional
    public void sendTicketReady(Order order) {
        Notification notification = new Notification();
        notification.setUser(order.getUser());
        notification.setType(Notification.NotificationType.TICKET_READY);
        notification.setChannel(Notification.NotificationChannel.SMS);
        notification.setTitle("Tickets Ready");
        notification.setBody("Your tickets are ready! Show the QR code at the cinema entrance.");
        notification.setIsSent(false);

        notificationRepository.save(notification);

        log.info("Ticket ready notification created for user: {}", order.getUser().getPhone());
    }

    /**
     * Send promotional notification
     */
    @Transactional
    public void sendPromoNotification(User user, String title, String body) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(Notification.NotificationType.PROMO);
        notification.setChannel(Notification.NotificationChannel.EMAIL);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setIsSent(false);

        notificationRepository.save(notification);

        log.info("Promo notification created for user: {}", user.getEmail());
    }

    /**
     * Send reminder notification
     */
    @Transactional
    public void sendReminderNotification(User user, String title, String body) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(Notification.NotificationType.REMINDER);
        notification.setChannel(Notification.NotificationChannel.PUSH);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setIsSent(false);

        notificationRepository.save(notification);

        log.info("Reminder notification created for user: {}", user.getEmail());
    }

    /**
     * Get pending notifications (to be sent by background job)
     */
    public List<Notification> getPendingNotifications() {
        return notificationRepository.findByIsSentFalseOrderByCreatedAtAsc();
    }

    /**
     * Mark notification as sent
     */
    @Transactional
    public void markAsSent(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setIsSent(true);
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    /**
     * Mark notification as failed
     */
    @Transactional
    public void markAsFailed(Long notificationId, String errorMessage) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setErrorMessage(errorMessage);
        notificationRepository.save(notification);
    }

    /**
     * Get user notifications
     */
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
