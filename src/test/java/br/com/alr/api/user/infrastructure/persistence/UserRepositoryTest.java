package br.com.alr.api.user.infrastructure.persistence;

import br.com.alr.api.user.TestDataHelper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class UserRepositoryTest {

    @Inject
    UserRepository userRepository;

    @Inject
    TestDataHelper testDataHelper;

    @BeforeEach
    void setUp() {
        testDataHelper.clearDatabase();
    }

    @Test
    @Transactional
    void shouldCheckExistsByUsernameAndFindByUsername() {
        UserEntity user = new UserEntity();
        user.firstName = "John";
        user.lastName = "Doe";
        user.username = "john@mail.test";
        user.password = "hashed";
        user.enabled = false;
        userRepository.persist(user);

        assertTrue(userRepository.existsByUsername("john@mail.test"));
        assertTrue(userRepository.findByUsername("john@mail.test").isPresent());
        assertFalse(userRepository.findByUsername("unknown@mail.test").isPresent());
    }
}
