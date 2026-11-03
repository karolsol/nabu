package com.karolsol.nabu.user.internal.exception;

import com.karolsol.nabu.shared.DomainException;
import com.karolsol.nabu.shared.ErrorCategory;

public class UserNameNotFoundException extends DomainException {
  public UserNameNotFoundException(String username) {
    super("user.username_not_found", ErrorCategory.NOT_FOUND, "User not found: " + username);
  }
}
