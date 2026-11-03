package com.karolsol.nabu.auth.internal.exception;

import com.karolsol.nabu.shared.DomainException;
import com.karolsol.nabu.shared.ErrorCategory;

public class InvalidJwtTokenException extends DomainException {
  public InvalidJwtTokenException() {
    super(
        "auth.token_invalid_or_expired", ErrorCategory.UNAUTHORIZED, "Token is invalid or expired");
  }
}
