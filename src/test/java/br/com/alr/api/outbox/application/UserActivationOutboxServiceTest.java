package br.com.alr.api.outbox.application;

import br.com.alr.api.outbox.infrastructure.persistence.OutboxEventEntity;
import br.com.alr.api.outbox.infrastructure.persistence.OutboxRepository;
import br.com.alr.api.user.infrastructure.persistence.UserEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserActivationOutboxServiceTest {

    @Test
    void shouldPersistOutboxEvent() throws Exception {
        OutboxRepository outboxRepository = mock(OutboxRepository.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        UserActivationOutboxService service = new UserActivationOutboxService();
        service.outboxRepository = outboxRepository;
        service.objectMapper = objectMapper;
        service.maxAttempts = 5;

        UserEntity user = new UserEntity();
        user.id = 7L;
        user.username = "john@mail.test";
        user.firstName = "John";

        service.publishUserActivationEmail(user);

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxRepository).persist(captor.capture());
        OutboxEventEntity event = captor.getValue();

        assertEquals("USER_ACTIVATION_EMAIL", event.eventType);
        assertEquals("{\"ok\":true}", event.payload);
        assertEquals(5, event.maxAttempts);
    }

    @Test
    void shouldThrowWhenPayloadSerializationFails() throws Exception {
        OutboxRepository outboxRepository = mock(OutboxRepository.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("serialization error") {});

        UserActivationOutboxService service = new UserActivationOutboxService();
        service.outboxRepository = outboxRepository;
        service.objectMapper = objectMapper;
        service.maxAttempts = 5;

        UserEntity user = new UserEntity();
        user.id = 7L;
        user.username = "john@mail.test";
        user.firstName = "John";

        assertThrows(IllegalStateException.class, () -> service.publishUserActivationEmail(user));
    }
}
