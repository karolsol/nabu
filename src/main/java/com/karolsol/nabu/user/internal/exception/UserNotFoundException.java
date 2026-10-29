package com.karolsol.nabu.user.internal.exception;

import com.karolsol.nabu.shared.DomainException;
import com.karolsol.nabu.shared.ErrorCategory;
import java.util.UUID;

public class UserNotFoundException extends DomainException {
  public UserNotFoundException(UUID id) {
    super("user.not_found", ErrorCategory.NOT_FOUND, "User not found: " + id);
  }
}
