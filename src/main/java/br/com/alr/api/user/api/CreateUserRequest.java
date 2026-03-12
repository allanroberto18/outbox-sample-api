package br.com.alr.api.user.api;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank String username,
    @NotBlank String password
) {
}
