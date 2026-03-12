package br.com.alr.api.user.application;

import br.com.alr.api.user.domain.DuplicateUsernameException;
import br.com.alr.api.outbox.application.UserActivationOutboxService;
import br.com.alr.api.shared.domain.PasswordHasher;
import br.com.alr.api.user.api.CreateUserRequest;
import br.com.alr.api.user.infrastructure.persistence.UserEntity;
import br.com.alr.api.user.infrastructure.persistence.UserRepository;
import br.com.alr.api.user.api.UserResponse;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegisterUserUseCaseTest {

    @Test
    void shouldRegisterUserWithHashedPasswordAndPublishOutbox() {
        UserRepository userRepository = mock(UserRepository.class);
        UserActivationOutboxService outboxService = mock(UserActivationOutboxService.class);
        PasswordHasher passwordHasher = mock(PasswordHasher.class);

        when(userRepository.existsByUsername("john@mail.test")).thenReturn(false);
        when(passwordHasher.hash("123456")).thenReturn("hashed-password");

        RegisterUserUseCase useCase = new RegisterUserUseCase();
        useCase.userRepository = userRepository;
        useCase.userActivationOutboxService = outboxService;
        useCase.passwordHasher = passwordHasher;

        UserResponse response = useCase.execute(new CreateUserRequest("John", "Doe", "john@mail.test", "123456"));

        assertFalse(response.enabled());
        verify(passwordHasher).hash("123456");
        verify(userRepository).persist(any(UserEntity.class));
        verify(userRepository).flush();
        verify(outboxService).publishUserActivationEmail(any(UserEntity.class));
    }

    @Test
    void shouldThrowDuplicateWhenUsernameAlreadyExists() {
        UserRepository userRepository = mock(UserRepository.class);
        UserActivationOutboxService outboxService = mock(UserActivationOutboxService.class);
        PasswordHasher passwordHasher = mock(PasswordHasher.class);

        when(userRepository.existsByUsername("john@mail.test")).thenReturn(true);

        RegisterUserUseCase useCase = new RegisterUserUseCase();
        useCase.userRepository = userRepository;
        useCase.userActivationOutboxService = outboxService;
        useCase.passwordHasher = passwordHasher;

        assertThrows(DuplicateUsernameException.class,
                () -> useCase.execute(new CreateUserRequest("John", "Doe", "john@mail.test", "123456")));
        verify(outboxService, never()).publishUserActivationEmail(any(UserEntity.class));
    }

    @Test
    void shouldTranslateUniqueConstraintViolation() {
        UserRepository userRepository = mock(UserRepository.class);
        UserActivationOutboxService outboxService = mock(UserActivationOutboxService.class);
        PasswordHasher passwordHasher = mock(PasswordHasher.class);

        when(userRepository.existsByUsername("john@mail.test")).thenReturn(false);
        when(passwordHasher.hash("123456")).thenReturn("hashed-password");
        doThrow(new PersistenceException("unique index violation")).when(userRepository).flush();

        RegisterUserUseCase useCase = new RegisterUserUseCase();
        useCase.userRepository = userRepository;
        useCase.userActivationOutboxService = outboxService;
        useCase.passwordHasher = passwordHasher;

        assertThrows(DuplicateUsernameException.class,
                () -> useCase.execute(new CreateUserRequest("John", "Doe", "john@mail.test", "123456")));
        verify(outboxService, never()).publishUserActivationEmail(any(UserEntity.class));
    }
}
