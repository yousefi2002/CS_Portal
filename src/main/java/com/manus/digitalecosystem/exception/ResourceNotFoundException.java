package com.manus.digitalecosystem.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiExceptionBase {
    public ResourceNotFoundException(String messageKey, Object... messageArgs) {
        super(messageKey, HttpStatus.NOT_FOUND, messageArgs);
    }
}
