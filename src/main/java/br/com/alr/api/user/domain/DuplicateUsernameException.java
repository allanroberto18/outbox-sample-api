package br.com.alr.api.user.domain;

public class DuplicateUsernameException extends RuntimeException {

  public DuplicateUsernameException(String username) {
    super("Username already exists: " + username);
  }
}
