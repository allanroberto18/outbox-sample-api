package br.com.alr.api.user.application;

import br.com.alr.api.user.domain.DuplicateUsernameException;
import br.com.alr.api.outbox.application.UserActivationOutboxService;
import br.com.alr.api.shared.domain.PasswordHasher;
import br.com.alr.api.user.api.CreateUserRequest;
import br.com.alr.api.user.infrastructure.persistence.UserEntity;
import br.com.alr.api.user.infrastructure.persistence.UserRepository;
import br.com.alr.api.user.api.UserResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RegisterUserUseCase {

    @Inject
    UserRepository userRepository;

    @Inject
    UserActivationOutboxService userActivationOutboxService;

    @Inject
    PasswordHasher passwordHasher;

    @Transactional
    public UserResponse execute(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUsernameException(request.username());
        }

        UserEntity user = new UserEntity();
        user.firstName = request.firstName();
        user.lastName = request.lastName();
        user.username = request.username();
        user.password = passwordHasher.hash(request.password());
        user.enabled = false;

        try {
            userRepository.persist(user);
            userRepository.flush();
        } catch (PersistenceException ex) {
            if (isUniqueViolation(ex)) {
                throw new DuplicateUsernameException(request.username());
            }
            throw ex;
        }

        userActivationOutboxService.publishUserActivationEmail(user);

        return UserResponse.fromEntity(user);
    }

    private boolean isUniqueViolation(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current.getMessage() != null && current.getMessage().toLowerCase().contains("unique")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
