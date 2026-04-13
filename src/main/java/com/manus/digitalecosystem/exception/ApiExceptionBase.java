package com.manus.digitalecosystem.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiExceptionBase extends RuntimeException {
    private final String messageKey;
    private final Object[] messageArgs;
    private final HttpStatus httpStatus;

    protected ApiExceptionBase(String messageKey, HttpStatus httpStatus, Object... messageArgs) {
        super(messageKey);
        this.messageKey = messageKey;
        this.httpStatus = httpStatus;
        this.messageArgs = messageArgs == null ? new Object[0] : messageArgs;
    }
}
