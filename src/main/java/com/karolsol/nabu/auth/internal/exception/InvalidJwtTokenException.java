package com.karolsol.nabu.auth.internal.exception;

import com.karolsol.nabu.shared.DomainException;
import org.springframework.http.HttpStatus;

public class InvalidJwtTokenException extends DomainException {
  public InvalidJwtTokenException() {
    super("auth.token_invalid_or_expired", HttpStatus.UNAUTHORIZED, "Token is invalid or expired");
  }
}
