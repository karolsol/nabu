package com.karolsol.nabu.shared;

public abstract class DomainException extends RuntimeException {

    private final String code;
    private final ErrorCategory category;

    protected DomainException(String code, ErrorCategory category, String message) {
        super(message);
        this.code = code;
        this.category = category;
    }

    public String code() { return code; }
    public ErrorCategory category() { return category; }
}
