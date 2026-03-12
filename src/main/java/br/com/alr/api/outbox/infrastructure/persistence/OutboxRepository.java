package br.com.alr.api.outbox.infrastructure.persistence;

import br.com.alr.api.outbox.domain.OutboxEventStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class OutboxRepository implements PanacheRepository<OutboxEventEntity> {

  public List<OutboxEventEntity> findDispatchablePending(LocalDateTime now, int batchSize) {
    return find("status = ?1 and (nextAttemptAt is null or nextAttemptAt <= ?2) order by createdAt asc",
            OutboxEventStatus.PENDING, now)
            .range(0, Math.max(0, batchSize - 1))
            .list();
  }
}
