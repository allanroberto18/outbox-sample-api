package br.com.alr.api.shared.api;

import br.com.alr.api.user.domain.DuplicateUsernameException;
import br.com.alr.api.user.domain.UserNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.stream.Collectors;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

  @Override
  public Response toResponse(Throwable exception) {
    if (exception instanceof DuplicateUsernameException duplicate) {
      return build(Response.Status.CONFLICT, "USERNAME_ALREADY_EXISTS", duplicate.getMessage());
    }

    if (exception instanceof UserNotFoundException userNotFound) {
      return build(Response.Status.NOT_FOUND, "USER_NOT_FOUND", userNotFound.getMessage());
    }

    if (exception instanceof ConstraintViolationException validation) {
      String message = validation.getConstraintViolations()
          .stream()
          .map(ConstraintViolation::getMessage)
          .collect(Collectors.joining(", "));
      return build(Response.Status.BAD_REQUEST, "VALIDATION_ERROR", message.isBlank() ? "Validation failed" : message);
    }

    if (exception instanceof WebApplicationException webException) {
      int statusCode = webException.getResponse() == null ? 500 : webException.getResponse().getStatus();
      String message = webException.getMessage() == null ? "Request failed" : webException.getMessage();
      return build(statusCode, "HTTP_" + statusCode, message);
    }

    return build(Response.Status.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected internal error");
  }

  private Response build(Response.Status status, String code, String message) {
    return build(status.getStatusCode(), code, message);
  }

  private Response build(int status, String code, String message) {
    return Response.status(status)
        .entity(ErrorResponse.of(code, message))
        .type(MediaType.APPLICATION_JSON)
        .build();
  }
}
