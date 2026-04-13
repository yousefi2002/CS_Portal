package com.manus.digitalecosystem.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends ApiExceptionBase {
    public DuplicateResourceException(String messageKey, Object... messageArgs) {
        super(messageKey, HttpStatus.CONFLICT, messageArgs);
    }
}
