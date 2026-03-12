package br.com.alr.api.user.infrastructure.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class UserEntity extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(name = "first_name", nullable = false, length = 120)
  public String firstName;

  @Column(name = "last_name", nullable = false, length = 120)
  public String lastName;

  @Column(nullable = false, unique = true, length = 120)
  public String username;

  @Column(nullable = false, length = 255)
  public String password;

  @Column(nullable = false)
  public boolean enabled;

  @Column(name = "created_at", nullable = false)
  public LocalDateTime createdAt;

  @Column(name = "updated_at")
  public LocalDateTime updatedAt;

  @PrePersist
  void onInsert() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
