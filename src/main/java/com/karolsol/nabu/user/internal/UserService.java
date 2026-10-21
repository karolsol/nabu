package com.karolsol.nabu.user.internal;

import com.karolsol.nabu.user.RegisterUserCommand;
import com.karolsol.nabu.user.UserDto;
import com.karolsol.nabu.user.UserMapper;
import com.karolsol.nabu.user.UserRegistered;
import com.karolsol.nabu.user.internal.exception.EmailAlreadyUsedException;
import com.karolsol.nabu.user.internal.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordConfig passwordConfig;
    private final ApplicationEventPublisher events;

    private String normalizeEmail(String email) {
        return email.toLowerCase(Locale.ROOT).trim();
    }

    @Transactional
    public UserDto register(RegisterUserCommand command) {
        Instant now = Instant.now();
        String normalizedEmail = normalizeEmail(command.email());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyUsedException(normalizedEmail);
        }

        UserEntity user = UserEntity.builder()
                .email(normalizedEmail)
                .passwordHash(passwordConfig.passwordEncoder().encode(command.password()))
                .firstName(command.firstName())
                .lastName(command.lastName())
                .createdAt(now)
                .updatedAt(now).build();
        try {
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyUsedException(normalizedEmail);
        }
        events.publishEvent(new UserRegistered(user.getId(), user.getEmail()));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto findById(UUID id) {
        return userRepository.findById(id).map(userMapper::toDto).orElseThrow(() -> new UserNotFoundException(id));
    }
}
