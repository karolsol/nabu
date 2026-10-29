package com.karolsol.nabu.shared.internal;

import com.karolsol.nabu.shared.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class DomainExceptionHandler {

  @ExceptionHandler(DomainException.class)
  ProblemDetail handle(DomainException e) {
    HttpStatus status =
        switch (e.category()) {
          case NOT_FOUND -> HttpStatus.NOT_FOUND;
          case CONFLICT -> HttpStatus.CONFLICT;
          case INVALID -> HttpStatus.UNPROCESSABLE_CONTENT;
          case FORBIDDEN -> HttpStatus.FORBIDDEN;
        };

    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, e.getMessage());
    problem.setProperty("code", e.code());
    return problem;
  }
}
