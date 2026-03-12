package br.com.alr.api.user.api;

import br.com.alr.api.outbox.infrastructure.scheduler.OutboxDispatcherService;
import br.com.alr.api.outbox.infrastructure.persistence.OutboxEventEntity;
import br.com.alr.api.outbox.domain.OutboxEventStatus;
import br.com.alr.api.outbox.infrastructure.persistence.OutboxRepository;
import br.com.alr.api.user.TestDataHelper;
import br.com.alr.api.user.infrastructure.persistence.UserEntity;
import br.com.alr.api.user.infrastructure.persistence.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class UserResourceTest {

    @Inject
    TestDataHelper testDataHelper;

    @Inject
    UserRepository userRepository;

    @Inject
    OutboxRepository outboxRepository;

    @Inject
    OutboxDispatcherService outboxDispatcherService;

    @BeforeEach
    void setUp() {
        testDataHelper.clearDatabase();
    }

    @Test
    void shouldExposeOpenApiAndHealth() {
        given()
                .when().get("/q/openapi")
                .then()
                .statusCode(200)
                .body(containsString("openapi"));

        given()
                .when().get("/q/health")
                .then()
                .statusCode(200)
                .body(containsString("\"status\": \"UP\""));
    }

    @Test
    void shouldRegisterNewUserWithOutboxEvent() {
        given()
                .contentType("application/json")
                .body(validRequest())
                .when().post("/users")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("enabled", equalTo(false))
                .body("createdAt", notNullValue())
                .body("updatedAt", equalTo(null));

        assertEquals(1, userRepository.count());
        UserEntity user = userRepository.findByUsername("john.doe@mail.test").orElseThrow();
        assertFalse(user.enabled);
        assertEquals("John", user.firstName);
        assertFalse("123456".equals(user.password));

        assertEquals(1, outboxRepository.count());
        OutboxEventEntity event = outboxRepository.findAll().firstResult();
        assertEquals(OutboxEventStatus.PENDING, event.status);
    }

    @Test
    void shouldRejectDuplicateUsername() {
        Map<String, Object> request = validRequest();

        given()
                .contentType("application/json")
                .body(request)
                .when().post("/users")
                .then()
                .statusCode(201);

        given()
                .contentType("application/json")
                .body(request)
                .when().post("/users")
                .then()
                .statusCode(409)
                .body("code", equalTo("USERNAME_ALREADY_EXISTS"))
                .body("message", containsString("Username already exists"));
    }

    @Test
    void shouldValidateMandatoryFields() {
        given()
                .contentType("application/json")
                .body(Map.of("firstName", "John"))
                .when().post("/users")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldActivateUser() {
        Long userId = given()
                .contentType("application/json")
                .body(validRequest())
                .when().post("/users")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        given()
                .when().get("/users/{id}/activate", userId)
                .then()
                .statusCode(200)
                .body("enabled", equalTo(true))
                .body("updatedAt", notNullValue());

        UserEntity user = userRepository.findById(userId);
        assertTrue(user.enabled);
    }

    @Test
    void shouldMarkOutboxAsSentAfterDispatch() {
        given()
                .contentType("application/json")
                .body(validRequest())
                .when().post("/users")
                .then()
                .statusCode(201);

        outboxDispatcherService.dispatchPending();

        OutboxEventEntity event = outboxRepository.findAll().firstResult();
        assertEquals(OutboxEventStatus.SENT, event.status);
        assertEquals(1, event.attempts);
    }

    private Map<String, Object> validRequest() {
        return Map.of(
                "firstName", "John",
                "lastName", "Doe",
                "username", "john.doe@mail.test",
                "password", "123456"
        );
    }
}
