package com.manus.digitalecosystem.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiExceptionBase {
    public UnauthorizedException(String messageKey, Object... messageArgs) {
        super(messageKey, HttpStatus.UNAUTHORIZED, messageArgs);
    }
}