package br.com.alr.api.outbox.domain;

public record ActivationOutboxPayload(
    Long userId,
    String username,
    String firstName
) {
}
