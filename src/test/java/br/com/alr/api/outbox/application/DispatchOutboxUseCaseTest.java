package br.com.alr.api.outbox.application;

import br.com.alr.api.email.domain.ActivationEmailSender;
import br.com.alr.api.outbox.domain.ActivationOutboxPayload;
import br.com.alr.api.outbox.infrastructure.persistence.OutboxEventEntity;
import br.com.alr.api.outbox.domain.OutboxEventStatus;
import br.com.alr.api.outbox.infrastructure.persistence.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DispatchOutboxUseCaseTest {

    @Test
    void shouldMarkSentWhenEmailDispatchSucceeds() throws Exception {
        OutboxRepository outboxRepository = mock(OutboxRepository.class);
        ActivationEmailSender sender = mock(ActivationEmailSender.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        OutboxEventEntity event = new OutboxEventEntity();
        event.payload = "payload";
        event.status = OutboxEventStatus.PENDING;
        event.maxAttempts = 5;
        when(outboxRepository.findDispatchablePending(any(), eq(20))).thenReturn(List.of(event));
        when(objectMapper.readValue("payload", ActivationOutboxPayload.class))
                .thenReturn(new ActivationOutboxPayload(1L, "john@mail.test", "John"));

        DispatchOutboxUseCase useCase = new DispatchOutboxUseCase();
        useCase.outboxRepository = outboxRepository;
        useCase.activationEmailSender = sender;
        useCase.objectMapper = objectMapper;
        useCase.batchSize = 20;
        useCase.maxAttempts = 5;
        useCase.retryDelaySeconds = 30;

        useCase.execute();

        assertEquals(OutboxEventStatus.SENT, event.status);
        assertEquals(1, event.attempts);
        verify(sender).sendActivationEmail("john@mail.test", "John", 1L);
    }

    @Test
    void shouldKeepPendingAndScheduleRetryBeforeMaxAttempts() throws Exception {
        OutboxRepository outboxRepository = mock(OutboxRepository.class);
        ActivationEmailSender sender = mock(ActivationEmailSender.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        OutboxEventEntity event = new OutboxEventEntity();
        event.payload = "payload";
        event.status = OutboxEventStatus.PENDING;
        event.maxAttempts = 3;
        when(outboxRepository.findDispatchablePending(any(), eq(20))).thenReturn(List.of(event));
        when(objectMapper.readValue("payload", ActivationOutboxPayload.class))
                .thenReturn(new ActivationOutboxPayload(1L, "john@mail.test", "John"));
        doThrow(new RuntimeException("smtp down")).when(sender)
                .sendActivationEmail("john@mail.test", "John", 1L);

        DispatchOutboxUseCase useCase = new DispatchOutboxUseCase();
        useCase.outboxRepository = outboxRepository;
        useCase.activationEmailSender = sender;
        useCase.objectMapper = objectMapper;
        useCase.batchSize = 20;
        useCase.maxAttempts = 3;
        useCase.retryDelaySeconds = 30;

        useCase.execute();

        assertEquals(OutboxEventStatus.PENDING, event.status);
        assertEquals(1, event.attempts);
        assertNotNull(event.nextAttemptAt);
    }

    @Test
    void shouldMarkFailedAfterMaxAttemptsOrInvalidPayload() throws Exception {
        OutboxRepository outboxRepository = mock(OutboxRepository.class);
        ActivationEmailSender sender = mock(ActivationEmailSender.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        OutboxEventEntity event = new OutboxEventEntity();
        event.payload = "bad-payload";
        event.status = OutboxEventStatus.PENDING;
        event.maxAttempts = 1;
        event.createdAt = LocalDateTime.now();
        when(outboxRepository.findDispatchablePending(any(), eq(20))).thenReturn(List.of(event));
        when(objectMapper.readValue("bad-payload", ActivationOutboxPayload.class))
                .thenThrow(new JsonProcessingException("bad") {
                });

        DispatchOutboxUseCase useCase = new DispatchOutboxUseCase();
        useCase.outboxRepository = outboxRepository;
        useCase.activationEmailSender = sender;
        useCase.objectMapper = objectMapper;
        useCase.batchSize = 20;
        useCase.maxAttempts = 1;
        useCase.retryDelaySeconds = 30;

        useCase.execute();

        assertEquals(OutboxEventStatus.FAILED, event.status);
        assertTrue(event.errorMessage.contains("Invalid outbox payload"));
    }
}
