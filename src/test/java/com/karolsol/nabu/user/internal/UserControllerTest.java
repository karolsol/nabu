package com.karolsol.nabu.user.internal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karolsol.nabu.user.RegisterUserCommand;
import com.karolsol.nabu.user.UserDto;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Mock private UserService userService;

  @InjectMocks private UserController userController;

  private MockMvc mockMvc;

  private UserDto userDto(UUID id) {
    Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
    return new UserDto(id, "ada@example.com", "Ada", "Lovelace", timestamp, timestamp, null);
  }

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
  }

  @Nested
  class Register {

    @Test
    void returnsCreatedUserAndLocation() throws Exception {
      UUID id = UUID.randomUUID();
      RegisterUserCommand command =
          new RegisterUserCommand("ada@example.com", "secret123", "Ada", "Lovelace");
      UserDto created = userDto(id);
      when(userService.register(eq(command))).thenReturn(created);

      mockMvc
          .perform(
              post("/api/v1/users")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(new ObjectMapper().writeValueAsString(command)))
          .andExpect(status().isCreated())
          .andExpect(header().string("Location", "http://localhost/api/v1/users/" + id))
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.id").value(id.toString()))
          .andExpect(jsonPath("$.email").value("ada@example.com"))
          .andExpect(jsonPath("$.firstName").value("Ada"))
          .andExpect(jsonPath("$.lastName").value("Lovelace"))
          .andExpect(jsonPath("$.createdAt").value("2026-01-01T00:00:00Z"))
          .andExpect(jsonPath("$.updatedAt").value("2026-01-01T00:00:00Z"))
          .andExpect(jsonPath("$.lastLoginAt").doesNotExist());
      verify(userService).register(command);
    }
  }

  @Nested
  class Get {

    @Test
    void returnsTheUser() throws Exception {
      UUID id = UUID.randomUUID();
      when(userService.findById(id)).thenReturn(userDto(id));

      mockMvc
          .perform(get("/api/v1/users/{id}", id))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(id.toString()))
          .andExpect(jsonPath("$.lastName").value("Lovelace"));
      verify(userService).findById(id);
    }
  }
}
