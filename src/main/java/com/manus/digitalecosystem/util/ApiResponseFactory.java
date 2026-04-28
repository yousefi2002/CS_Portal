package com.manus.digitalecosystem.util;

import com.manus.digitalecosystem.dto.response.Pagination;
import com.manus.digitalecosystem.dto.response.Response;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Component
public class ApiResponseFactory {

    private final MessageSource messageSource;

    public ApiResponseFactory(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public <T> ResponseEntity<Response<T>> success(HttpStatus status, String messageKey, T data, Object... messageArgs) {
        Response<T> body = Response.<T>builder()
                .success(true)
                .status(status.value())
                .message(localize(messageKey, messageArgs, "Success"))
                .data(data)
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .requestId(UUID.randomUUID().toString())
                .build();
        return ResponseEntity.status(status).body(body);
    }

    public <T> ResponseEntity<Response<T>> success(HttpStatus status,
                                                   String messageKey,
                                                   T data,
                                                   Pagination pagination,
                                                   Object... messageArgs) {
        Response<T> body = Response.<T>builder()
                .success(true)
                .status(status.value())
                .message(localize(messageKey, messageArgs, "Success"))
                .data(data)
                .pagination(pagination)
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .requestId(UUID.randomUUID().toString())
                .build();
        return ResponseEntity.status(status).body(body);
    }

    public ResponseEntity<Response<Object>> error(HttpStatus status,
                                                  String messageKey,
                                                  String fallback,
                                                  Map<String, Object> errors,
                                                  Object... messageArgs) {
        Response<Object> body = Response.builder()
                .success(false)
                .status(status.value())
                .message(localize(messageKey, messageArgs, fallback))
                .errors(errors)
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .requestId(UUID.randomUUID().toString())
                .build();
        return ResponseEntity.status(status).body(body);
    }

    private String localize(String messageKey, Object[] messageArgs, String fallback) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(messageKey, messageArgs, fallback, locale);
    }
}

