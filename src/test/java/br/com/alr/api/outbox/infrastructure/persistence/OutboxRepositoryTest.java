package br.com.alr.api.outbox.infrastructure.persistence;

import br.com.alr.api.outbox.domain.OutboxEventStatus;
import br.com.alr.api.user.TestDataHelper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class OutboxRepositoryTest {

    @Inject
    OutboxRepository outboxRepository;

    @Inject
    TestDataHelper testDataHelper;

    @BeforeEach
    void setUp() {
        testDataHelper.clearDatabase();
    }

    @Test
    @Transactional
    void shouldReturnOnlyDispatchablePendingEvents() {
        OutboxEventEntity dispatchable = new OutboxEventEntity();
        dispatchable.eventType = "USER_ACTIVATION_EMAIL";
        dispatchable.payload = "{}";
        dispatchable.status = OutboxEventStatus.PENDING;
        dispatchable.maxAttempts = 5;
        dispatchable.nextAttemptAt = LocalDateTime.now().minusSeconds(1);
        outboxRepository.persist(dispatchable);

        OutboxEventEntity future = new OutboxEventEntity();
        future.eventType = "USER_ACTIVATION_EMAIL";
        future.payload = "{}";
        future.status = OutboxEventStatus.PENDING;
        future.maxAttempts = 5;
        future.nextAttemptAt = LocalDateTime.now().plusMinutes(2);
        outboxRepository.persist(future);

        OutboxEventEntity sent = new OutboxEventEntity();
        sent.eventType = "USER_ACTIVATION_EMAIL";
        sent.payload = "{}";
        sent.status = OutboxEventStatus.SENT;
        sent.maxAttempts = 5;
        outboxRepository.persist(sent);

        List<OutboxEventEntity> result = outboxRepository.findDispatchablePending(LocalDateTime.now(), 20);

        assertEquals(1, result.size());
        assertEquals(dispatchable.id, result.getFirst().id);
    }
}
