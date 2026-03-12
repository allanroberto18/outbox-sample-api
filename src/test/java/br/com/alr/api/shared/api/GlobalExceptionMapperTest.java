package br.com.alr.api.shared.api;

import br.com.alr.api.user.domain.DuplicateUsernameException;
import br.com.alr.api.user.domain.UserNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionMapperTest {

    @Test
    void shouldMapDuplicateUsernameException() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();

        Response response = mapper.toResponse(new DuplicateUsernameException("john@mail.test"));

        ErrorResponse body = (ErrorResponse) response.getEntity();
        assertEquals(409, response.getStatus());
        assertEquals("USERNAME_ALREADY_EXISTS", body.code());
        assertTrue(body.message().contains("Username already exists"));
    }

    @Test
    void shouldMapUserNotFoundException() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();

        Response response = mapper.toResponse(new UserNotFoundException(9L));

        ErrorResponse body = (ErrorResponse) response.getEntity();
        assertEquals(404, response.getStatus());
        assertEquals("USER_NOT_FOUND", body.code());
        assertTrue(body.message().contains("User not found"));
    }

    @Test
    void shouldMapConstraintViolationException() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("firstName must not be blank");

        Response response = mapper.toResponse(new ConstraintViolationException(Set.of(violation)));

        ErrorResponse body = (ErrorResponse) response.getEntity();
        assertEquals(400, response.getStatus());
        assertEquals("VALIDATION_ERROR", body.code());
        assertTrue(body.message().contains("firstName must not be blank"));
    }

    @Test
    void shouldMapWebApplicationExceptionKeepingStatusCode() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();

        Response response = mapper.toResponse(new NotFoundException("resource missing"));

        assertEquals(404, response.getStatus());
    }

    @Test
    void shouldMapUnexpectedExceptionAsInternalServerError() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();

        Response response = mapper.toResponse(new RuntimeException("boom"));

        ErrorResponse body = (ErrorResponse) response.getEntity();
        assertEquals(500, response.getStatus());
        assertEquals("INTERNAL_ERROR", body.code());
        assertEquals("Unexpected internal error", body.message());
    }
}
