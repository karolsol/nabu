package com.karolsol.nabu.user;

import com.karolsol.nabu.user.internal.UserRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserFacade {
  private final UserRepository userRepository;

  public boolean existById(UUID id) {
    return userRepository.existsById(id);
  }
}
