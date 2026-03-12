package br.com.alr.api.user;

import br.com.alr.api.outbox.infrastructure.persistence.OutboxRepository;
import br.com.alr.api.user.infrastructure.persistence.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TestDataHelper {

    @Inject
    UserRepository userRepository;

    @Inject
    OutboxRepository outboxRepository;

    @Transactional
    public void clearDatabase() {
        outboxRepository.deleteAll();
        userRepository.deleteAll();
    }
}
