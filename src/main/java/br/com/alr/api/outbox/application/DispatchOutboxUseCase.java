package br.com.alr.api.outbox.application;

import br.com.alr.api.email.domain.ActivationEmailSender;
import br.com.alr.api.outbox.domain.ActivationOutboxPayload;
import br.com.alr.api.outbox.infrastructure.persistence.OutboxEventEntity;
import br.com.alr.api.outbox.domain.OutboxEventStatus;
import br.com.alr.api.outbox.infrastructure.persistence.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class DispatchOutboxUseCase {

    private static final Logger LOG = Logger.getLogger(DispatchOutboxUseCase.class);

    @Inject
    OutboxRepository outboxRepository;

    @Inject
    ActivationEmailSender activationEmailSender;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "outbox.dispatch.batch-size", defaultValue = "20")
    int batchSize;

    @ConfigProperty(name = "outbox.dispatch.max-attempts", defaultValue = "5")
    int maxAttempts;

    @ConfigProperty(name = "outbox.dispatch.retry-delay-seconds", defaultValue = "30")
    long retryDelaySeconds;

    @Transactional
    public void execute() {
        List<OutboxEventEntity> pendingEvents = outboxRepository.findDispatchablePending(LocalDateTime.now(), batchSize);

        LOG.infov("Outbox dispatch cycle started. pendingEvents={0}", pendingEvents.size());

        for (OutboxEventEntity event : pendingEvents) {
            dispatch(event);
        }

        LOG.infov("Outbox dispatch cycle finished. dispatchedEvents={0}", pendingEvents.size());
    }

    void dispatch(OutboxEventEntity event) {
        try {
            ActivationOutboxPayload payload = objectMapper.readValue(event.payload, ActivationOutboxPayload.class);
            activationEmailSender.sendActivationEmail(payload.username(), payload.firstName(), payload.userId());
            event.markSent();
            LOG.infov("Outbox event dispatched successfully. eventId={0}, eventType={1}, status={2}, attempts={3}",
                    event.id, event.eventType, event.status, event.attempts);
        } catch (Exception e) {
            event.markFailed(sanitizeError(e), maxAttempts, retryDelaySeconds);
            LOG.warnv("Outbox event dispatch failed. eventId={0}, eventType={1}, status={2}, attempts={3}, error={4}",
                    event.id, event.eventType, event.status, event.attempts, event.errorMessage);
        }
    }

    private String sanitizeError(Exception e) {
        if (e instanceof JsonProcessingException) {
            return "Invalid outbox payload";
        }

        String message = e.getMessage() == null ? "Unexpected email dispatch error" : e.getMessage();
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
