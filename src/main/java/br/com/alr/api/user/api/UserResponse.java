package br.com.alr.api.user.api;

import br.com.alr.api.user.infrastructure.persistence.UserEntity;

import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String firstName,
    String lastName,
    String username,
    boolean enabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
  public static UserResponse fromEntity(UserEntity user) {
    return new UserResponse(
        user.id,
        user.firstName,
        user.lastName,
        user.username,
        user.enabled,
        user.createdAt,
        user.updatedAt
    );
  }
}
