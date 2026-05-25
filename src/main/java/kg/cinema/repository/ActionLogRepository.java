package kg.cinema.repository;

import kg.cinema.entity.ActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {

    List<ActionLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ActionLog> findByEventTypeOrderByCreatedAtDesc(String eventType);

    List<ActionLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
}
