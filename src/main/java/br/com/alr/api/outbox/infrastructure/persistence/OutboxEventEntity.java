package br.com.alr.api.outbox.infrastructure.persistence;

import br.com.alr.api.outbox.domain.OutboxEventStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(name = "event_type", nullable = false, length = 120)
  public String eventType;

  @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
  public String payload;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  public OutboxEventStatus status;

  @Column(nullable = false)
  public int attempts;

  @Column(name = "error_message", length = 1000)
  public String errorMessage;

  @Column(name = "last_error_at")
  public LocalDateTime lastErrorAt;

  @Column(name = "next_attempt_at")
  public LocalDateTime nextAttemptAt;

  @Column(name = "max_attempts", nullable = false)
  public int maxAttempts;

  @Column(name = "created_at", nullable = false)
  public LocalDateTime createdAt;

  @Column(name = "processed_at")
  public LocalDateTime processedAt;

  @PrePersist
  void onInsert() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
    if (status == null) {
      status = OutboxEventStatus.PENDING;
    }
    if (maxAttempts <= 0) {
      maxAttempts = 5;
    }
  }

  public void markSent() {
    status = OutboxEventStatus.SENT;
    attempts = attempts + 1;
    errorMessage = null;
    lastErrorAt = null;
    nextAttemptAt = null;
    processedAt = LocalDateTime.now();
  }

  public void markFailed(String error, int configuredMaxAttempts, long retryDelaySeconds) {
    if (maxAttempts <= 0) {
      maxAttempts = configuredMaxAttempts;
    }
    attempts = attempts + 1;
    errorMessage = error;
    lastErrorAt = LocalDateTime.now();
    if (attempts >= maxAttempts) {
      status = OutboxEventStatus.FAILED;
      nextAttemptAt = null;
    } else {
      status = OutboxEventStatus.PENDING;
      nextAttemptAt = lastErrorAt.plusSeconds(retryDelaySeconds);
    }
    processedAt = LocalDateTime.now();
  }
}
