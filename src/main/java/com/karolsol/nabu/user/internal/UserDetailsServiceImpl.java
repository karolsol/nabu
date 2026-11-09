package com.karolsol.nabu.user.internal;

import java.util.Collections;
import java.util.Locale;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
  private final UserRepository userRepository;

  private String normalizeEmail(String email) {
    return email.toLowerCase(Locale.ROOT).trim();
  }

  @Override
  @NonNull
  public UserDetails loadUserByUsername(@NonNull String email) {
    UserEntity u =
        userRepository
            .findByEmail(normalizeEmail(email))
            .orElseThrow(() -> new UsernameNotFoundException("email not found: " + email));
    return User.withUsername(u.getId().toString())
        .password(u.getPasswordHash())
        .disabled(!u.isEnabled())
        .authorities(Collections.emptyList())
        .build();
  }
}
