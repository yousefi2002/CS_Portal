package com.manus.digitalecosystem.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiExceptionBase {
    public BadRequestException(String messageKey, Object... messageArgs) {
        super(messageKey, HttpStatus.BAD_REQUEST, messageArgs);
    }
}

