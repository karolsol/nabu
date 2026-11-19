package com.karolsol.nabu.user.internal.exception;

import com.karolsol.nabu.shared.DomainException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyUsedException extends DomainException {
  public EmailAlreadyUsedException(String email) {
    super("user.email_already_used", HttpStatus.CONFLICT, "Email already in use: " + email);
  }
}
