package com.karolsol.nabu.user.internal.exception;

import com.karolsol.nabu.shared.DomainException;
import org.springframework.http.HttpStatus;

public class UserNameNotFoundException extends DomainException {
  public UserNameNotFoundException(String username) {
    super("user.username_not_found", HttpStatus.NOT_FOUND, "User not found: " + username);
  }
}
