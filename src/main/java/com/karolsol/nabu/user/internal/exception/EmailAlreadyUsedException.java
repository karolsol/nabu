package com.karolsol.nabu.user.internal.exception;

import com.karolsol.nabu.shared.DomainException;
import com.karolsol.nabu.shared.ErrorCategory;

public class EmailAlreadyUsedException extends DomainException {
    public EmailAlreadyUsedException(String email) {
        super("user.email_already_used", ErrorCategory.CONFLICT, "Email already in use: " + email);
    }
}
