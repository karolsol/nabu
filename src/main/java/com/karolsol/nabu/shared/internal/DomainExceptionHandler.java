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
    HttpStatus status = e.category();
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, e.getMessage());
    problem.setProperty("code", e.code());
    return problem;
  }
}
