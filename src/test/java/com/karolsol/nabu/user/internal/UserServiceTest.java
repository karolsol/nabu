package com.karolsol.nabu.user.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.karolsol.nabu.user.RegisterUserCommand;
import com.karolsol.nabu.user.UserDto;
import com.karolsol.nabu.user.UserMapper;
import com.karolsol.nabu.user.UserRegistered;
import com.karolsol.nabu.user.internal.exception.EmailAlreadyUsedException;
import com.karolsol.nabu.user.internal.exception.UserNotFoundException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  private static final String DEFAULT_EMAIL = "ada@example.com";
  private static final String DEFAULT_PASSWORD = "secret123";
  private static final String ENCODED_PASSWORD = "encoded-password";
  private static final String FIRST_NAME = "Ada";
  private static final String LAST_NAME = "Lovelace";

  @Mock private UserRepository userRepository;

  @Mock private UserMapper userMapper;

  @Mock private PasswordConfig passwordConfig;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private ApplicationEventPublisher events;

  @InjectMocks private UserService userService;

  private RegisterUserCommand defaultCommand() {
    return new RegisterUserCommand(DEFAULT_EMAIL, DEFAULT_PASSWORD, FIRST_NAME, LAST_NAME);
  }

  private UserEntity defaultUserEntity(UUID id) {
    Instant now = Instant.now();
    return UserEntity.builder()
        .id(id)
        .email(DEFAULT_EMAIL)
        .passwordHash(ENCODED_PASSWORD)
        .firstName(FIRST_NAME)
        .lastName(LAST_NAME)
        .createdAt(now)
        .updatedAt(now)
        .build();
  }

  private void mockPasswordEncoding() {
    when(passwordConfig.passwordEncoder()).thenReturn(passwordEncoder);
    when(passwordEncoder.encode(any())).thenReturn(ENCODED_PASSWORD);
  }

  @Nested
  class Register {

    @Test
    void should_save_encoded_user_and_return_dto() {
      // Arrange
      RegisterUserCommand command = defaultCommand();
      UUID generatedId = UUID.randomUUID();
      UserEntity savedUser = defaultUserEntity(generatedId);
      UserDto expectedDto =
          new UserDto(
              generatedId,
              savedUser.getEmail(),
              savedUser.getFirstName(),
              savedUser.getLastName(),
              savedUser.getCreatedAt(),
              savedUser.getUpdatedAt(),
              null);

      when(userRepository.existsByEmail(command.email())).thenReturn(false);
      mockPasswordEncoding();
      when(userRepository.saveAndFlush(any(UserEntity.class))).thenReturn(savedUser);
      when(userMapper.toDto(any(UserEntity.class))).thenReturn(expectedDto);

      // Act
      UserDto result = userService.register(command);

      // Assert
      assertThat(result).isEqualTo(expectedDto);

      ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
      verify(userRepository).saveAndFlush(userCaptor.capture());
      UserEntity userToSave = userCaptor.getValue();
      assertThat(userToSave.getEmail()).isEqualTo(command.email());
      assertThat(userToSave.getPasswordHash()).isEqualTo(ENCODED_PASSWORD);
      assertThat(userToSave.getFirstName()).isEqualTo(command.firstName());
      assertThat(userToSave.getLastName()).isEqualTo(command.lastName());
      assertThat(userToSave.getCreatedAt()).isNotNull();
      assertThat(userToSave.getUpdatedAt()).isNotNull();
      assertThat(userToSave.getUpdatedAt()).isEqualTo(userToSave.getCreatedAt());

      verify(passwordEncoder).encode(command.password());
    }

    @Test
    void should_reject_already_used_email() {
      // Arrange
      RegisterUserCommand command = defaultCommand();
      when(userRepository.existsByEmail(command.email())).thenReturn(true);

      // Act & Assert
      assertThatThrownBy(() -> userService.register(command))
          .isInstanceOf(EmailAlreadyUsedException.class)
          .hasMessage("Email already in use: " + command.email());

      verifyNoInteractions(passwordConfig, passwordEncoder, userMapper);
      verify(userRepository, never()).saveAndFlush(any(UserEntity.class));
      verifyNoInteractions(events);
    }

    @Test
    void should_publish_user_registered_event() {
      // Arrange
      RegisterUserCommand command = defaultCommand();
      UserEntity savedUser = defaultUserEntity(UUID.randomUUID());
      UserDto expectedDto = mock(UserDto.class);

      when(userRepository.existsByEmail(command.email())).thenReturn(false);
      mockPasswordEncoding();
      when(userRepository.saveAndFlush(any(UserEntity.class))).thenReturn(savedUser);
      when(userMapper.toDto(any(UserEntity.class))).thenReturn(expectedDto);

      // Act
      userService.register(command);

      // Assert
      ArgumentCaptor<UserRegistered> eventCaptor = ArgumentCaptor.forClass(UserRegistered.class);
      verify(events).publishEvent(eventCaptor.capture());

      UserRegistered publishedEvent = eventCaptor.getValue();
      assertThat(publishedEvent.userId()).isEqualTo(savedUser.getId());
      assertThat(publishedEvent.email()).isEqualTo(savedUser.getEmail());
    }

    @Test
    void should_throw_exception_when_losing_race_condition() {
      // Arrange
      RegisterUserCommand command = defaultCommand();

      when(userRepository.existsByEmail(command.email())).thenReturn(false);
      mockPasswordEncoding();
      when(userRepository.saveAndFlush(any(UserEntity.class)))
          .thenThrow(new DataIntegrityViolationException(""));

      // Act & Assert
      assertThatThrownBy(() -> userService.register(command))
          .isInstanceOf(EmailAlreadyUsedException.class)
          .hasMessage("Email already in use: " + command.email());

      verify(userRepository).saveAndFlush(any(UserEntity.class));
      verifyNoInteractions(events);
    }

    @Test
    void should_normalize_email_before_saving() {
      // Arrange
      when(userRepository.existsByEmail(any())).thenReturn(false);
      mockPasswordEncoding();
      when(userRepository.saveAndFlush(any(UserEntity.class)))
          .then(invocation -> invocation.getArgument(0));
      when(userMapper.toDto(any(UserEntity.class))).thenReturn(mock(UserDto.class));

      RegisterUserCommand command =
          new RegisterUserCommand("ada@EXAMPLE.cOm ", DEFAULT_PASSWORD, FIRST_NAME, LAST_NAME);

      // Act
      userService.register(command);

      // Assert
      verify(userRepository).existsByEmail(DEFAULT_EMAIL);

      var captor = ArgumentCaptor.forClass(UserEntity.class);
      verify(userRepository).saveAndFlush(captor.capture());
      assertThat(captor.getValue().getEmail()).isEqualTo(DEFAULT_EMAIL);
    }
  }

  @Nested
  class FindById {

    @Test
    void should_map_found_user() {
      // Arrange
      UUID id = UUID.randomUUID();
      UserEntity entity = UserEntity.builder().id(id).build();
      UserDto expected =
          new UserDto(id, DEFAULT_EMAIL, FIRST_NAME, LAST_NAME, Instant.now(), Instant.now(), null);

      when(userRepository.findById(id)).thenReturn(Optional.of(entity));
      when(userMapper.toDto(entity)).thenReturn(expected);

      // Act & Assert
      assertThat(userService.findById(id)).isEqualTo(expected);
    }

    @Test
    void should_throw_exception_when_user_does_not_exist() {
      // Arrange
      UUID id = UUID.randomUUID();
      when(userRepository.findById(id)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> userService.findById(id))
          .isInstanceOf(UserNotFoundException.class)
          .hasMessage("User not found: " + id);

      verifyNoInteractions(userMapper);
    }
  }
}
