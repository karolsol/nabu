package com.karolsol.nabu.shared;

import org.springframework.http.HttpStatus;

public abstract class DomainException extends RuntimeException {

  private final String code;
  private final HttpStatus category;

  protected DomainException(String code, HttpStatus category, String message) {
    super(message);
    this.code = code;
    this.category = category;
  }

  public String code() {
    return code;
  }

  public HttpStatus category() {
    return category;
  }
}
