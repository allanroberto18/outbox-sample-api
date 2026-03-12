package br.com.alr.api.outbox.application;

import br.com.alr.api.outbox.domain.ActivationOutboxPayload;
import br.com.alr.api.outbox.infrastructure.persistence.OutboxEventEntity;
import br.com.alr.api.outbox.infrastructure.persistence.OutboxRepository;
import br.com.alr.api.user.infrastructure.persistence.UserEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class UserActivationOutboxService {

  private static final String EVENT_TYPE_USER_ACTIVATION_EMAIL = "USER_ACTIVATION_EMAIL";

    @Inject
    OutboxRepository outboxRepository;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "outbox.dispatch.max-attempts", defaultValue = "5")
    int maxAttempts;

    public void publishUserActivationEmail(UserEntity user) {
        OutboxEventEntity outboxEvent = new OutboxEventEntity();
        outboxEvent.eventType = EVENT_TYPE_USER_ACTIVATION_EMAIL;
        outboxEvent.payload = toPayload(user);
        outboxEvent.maxAttempts = maxAttempts;
        outboxRepository.persist(outboxEvent);
    }

    private String toPayload(UserEntity user) {
        try {
            return objectMapper.writeValueAsString(new ActivationOutboxPayload(user.id, user.username, user.firstName));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize outbox payload", e);
        }
    }
}
