package br.com.alr.api.shared.domain;

public interface PasswordHasher {

    String hash(String rawPassword);
}
