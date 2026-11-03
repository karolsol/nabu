package com.karolsol.nabu.user.internal;

import com.karolsol.nabu.shared.CurrentUserId;
import com.karolsol.nabu.user.RegisterUserCommand;
import com.karolsol.nabu.user.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Register, login and manage user account")
public class UserController {

  private final UserService userService;

  @Operation(
      summary = "Register a new user",
      description =
          "Creates a new user account with the provided information. The email must be unique.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "User created successfully",
        content = @Content(schema = @Schema(implementation = UserDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Bad request - invalid input data",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    @ApiResponse(
        responseCode = "409",
        description = "User with the same email already exists",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  })
  @PostMapping
  ResponseEntity<UserDto> register(@Valid @RequestBody RegisterUserCommand command) {
    UserDto created = userService.register(command);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();
    return ResponseEntity.created(location).body(created);
  }

  @Operation(
      summary = "Getting user info",
      description = "Returns user information for logged in user")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "User found successfully",
        content = @Content(schema = @Schema(implementation = UserDto.class))),
    @ApiResponse(
        responseCode = "401",
        description = "User not authenticated",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  })
  @GetMapping("/me")
  UserDto get(@Parameter(hidden = true) @CurrentUserId UUID userId) {
    return userService.findById(userId);
  }

  @Operation(
      summary = "Delete user account",
      description = "Deletes user account for logged in user")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "User account deleted successfully"),
    @ApiResponse(
        responseCode = "401",
        description = "User not authenticated",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  })
  @DeleteMapping("/me")
  public ResponseEntity<Void> delete(@Parameter(hidden = true) @CurrentUserId UUID userId) {
    userService.deleteById(userId);
    return ResponseEntity.noContent().build();
  }
}
