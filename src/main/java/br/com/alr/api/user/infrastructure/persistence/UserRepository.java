package br.com.alr.api.user.infrastructure.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<UserEntity> {

  public boolean existsByUsername(String username) {
    return count("username", username) > 0;
  }

  public Optional<UserEntity> findByUsername(String username) {
    return find("username", username).firstResultOptional();
  }
}
