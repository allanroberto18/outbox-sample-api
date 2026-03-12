package br.com.alr.api.user.api;

import br.com.alr.api.email.domain.ActivationEmailSender;
import br.com.alr.api.outbox.infrastructure.scheduler.OutboxDispatcherService;
import br.com.alr.api.outbox.infrastructure.persistence.OutboxEventEntity;
import br.com.alr.api.outbox.domain.OutboxEventStatus;
import br.com.alr.api.outbox.infrastructure.persistence.OutboxRepository;
import br.com.alr.api.user.TestDataHelper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

@QuarkusTest
class OutboxDispatcherFailureTest {

    @Inject
    TestDataHelper testDataHelper;

    @Inject
    OutboxRepository outboxRepository;

    @Inject
    OutboxDispatcherService outboxDispatcherService;

    @InjectMock
    ActivationEmailSender activationEmailSender;

    @BeforeEach
    void setUp() {
        testDataHelper.clearDatabase();
    }

    @Test
    void shouldMarkOutboxAsFailedWhenEmailDispatchFails() {
        doThrow(new RuntimeException("smtp unavailable"))
                .when(activationEmailSender)
                .sendActivationEmail(anyString(), anyString(), anyLong());

        given()
                .contentType("application/json")
                .body(Map.of(
                        "firstName", "John",
                        "lastName", "Doe",
                        "username", "john.doe@mail.test",
                        "password", "123456"
                ))
                .when().post("/users")
                .then()
                .statusCode(201);

        outboxDispatcherService.dispatchPending();

        OutboxEventEntity event = outboxRepository.findAll().firstResult();
        assertEquals(OutboxEventStatus.PENDING, event.status);
        assertEquals(1, event.attempts);
        assertTrue(event.errorMessage.contains("smtp unavailable"));
        assertNotNull(event.nextAttemptAt);
    }
}
