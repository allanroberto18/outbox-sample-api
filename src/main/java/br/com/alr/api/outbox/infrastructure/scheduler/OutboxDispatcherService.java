package br.com.alr.api.outbox.infrastructure.scheduler;

import br.com.alr.api.outbox.application.DispatchOutboxUseCase;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OutboxDispatcherService {

  @Inject
  DispatchOutboxUseCase dispatchOutboxUseCase;

  @Scheduled(every = "{outbox.dispatch.every}")
  @RunOnVirtualThread
  void scheduledDispatch() {
    dispatchPending();
  }

  public void dispatchPending() {
    dispatchOutboxUseCase.execute();
  }
}
