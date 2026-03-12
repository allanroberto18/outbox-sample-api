package br.com.alr.api.outbox.domain;

public enum OutboxEventStatus {
  PENDING,
  SENT,
  FAILED
}
