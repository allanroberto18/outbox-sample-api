package br.com.alr.api.user.api;

import br.com.alr.api.user.application.ActivateUserUseCase;
import br.com.alr.api.user.application.RegisterUserUseCase;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Users")
public class UserResource {

  @Inject
  RegisterUserUseCase registerUserUseCase;

  @Inject
  ActivateUserUseCase activateUserUseCase;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(summary = "Register a new user")
  public Response register(@Valid CreateUserRequest request) {
    UserResponse response = registerUserUseCase.execute(request);
    return Response.status(Response.Status.CREATED).entity(response).build();
  }

  @GET
  @Path("/{id}/activate")
  @Operation(summary = "Activate an existing user")
  public UserResponse activate(@PathParam("id") Long id) {
    return activateUserUseCase.execute(id);
  }
}
