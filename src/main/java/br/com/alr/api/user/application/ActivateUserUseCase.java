package br.com.alr.api.user.application;

import br.com.alr.api.user.domain.UserNotFoundException;
import br.com.alr.api.user.infrastructure.persistence.UserEntity;
import br.com.alr.api.user.infrastructure.persistence.UserRepository;
import br.com.alr.api.user.api.UserResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@ApplicationScoped
public class ActivateUserUseCase {

    @Inject
    UserRepository userRepository;

    @Transactional
    public UserResponse execute(Long id) {
        UserEntity user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.enabled = true;
        user.updatedAt = LocalDateTime.now();

        return UserResponse.fromEntity(user);
    }
}
