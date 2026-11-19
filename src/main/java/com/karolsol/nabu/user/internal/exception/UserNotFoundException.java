package com.karolsol.nabu.user.internal.exception;

import com.karolsol.nabu.shared.DomainException;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends DomainException {
  public UserNotFoundException(UUID id) {
    super("user.not_found", HttpStatus.NOT_FOUND, "User not found: " + id);
  }
}
