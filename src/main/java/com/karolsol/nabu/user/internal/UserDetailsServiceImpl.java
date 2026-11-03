package com.karolsol.nabu.user.internal;

import com.karolsol.nabu.user.internal.exception.UserNameNotFoundException;
import java.util.Collections;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  @NonNull
  public UserDetails loadUserByUsername(@NonNull String email) {
    UserEntity u =
        userRepository.findByEmail(email).orElseThrow(() -> new UserNameNotFoundException(email));
    return User.withUsername(u.getId().toString())
        .password(u.getPasswordHash())
        .disabled(!u.isEnabled())
        .authorities(Collections.emptyList())
        .build();
  }
}
