package br.com.alr.api.user.domain;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(Long userId) {
    super("User not found: " + userId);
  }
}
