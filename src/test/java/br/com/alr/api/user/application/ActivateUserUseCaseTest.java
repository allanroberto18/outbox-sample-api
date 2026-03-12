package br.com.alr.api.user.application;

import br.com.alr.api.user.domain.UserNotFoundException;
import br.com.alr.api.user.infrastructure.persistence.UserEntity;
import br.com.alr.api.user.infrastructure.persistence.UserRepository;
import br.com.alr.api.user.api.UserResponse;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActivateUserUseCaseTest {

    @Test
    void shouldActivateUser() {
        UserRepository userRepository = mock(UserRepository.class);
        UserEntity entity = new UserEntity();
        entity.id = 1L;
        entity.enabled = false;
        when(userRepository.findByIdOptional(1L)).thenReturn(Optional.of(entity));

        ActivateUserUseCase useCase = new ActivateUserUseCase();
        useCase.userRepository = userRepository;

        UserResponse response = useCase.execute(1L);

        assertTrue(response.enabled());
        assertNotNull(response.updatedAt());
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

        ActivateUserUseCase useCase = new ActivateUserUseCase();
        useCase.userRepository = userRepository;

        assertThrows(UserNotFoundException.class, () -> useCase.execute(99L));
    }
}
