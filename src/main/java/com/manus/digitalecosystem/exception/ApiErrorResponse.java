package com.manus.digitalecosystem.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {
    private final String message;
    private final String messageKey;
    private final HttpStatus httpStatus;
    private final ZonedDateTime timestamp;
    private final String path;
    private final Map<String, String> errors;
}
