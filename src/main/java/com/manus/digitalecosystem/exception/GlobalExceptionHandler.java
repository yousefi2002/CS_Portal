package com.manus.digitalecosystem.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(value = {ApiExceptionBase.class})
    public ResponseEntity<ApiErrorResponse> handleApiExceptionBase(ApiExceptionBase ex, HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = localize(ex.getMessageKey(), ex.getMessageArgs(), ex.getMessage(), locale);

        ApiErrorResponse apiError = ApiErrorResponse.builder()
                .message(message)
                .messageKey(ex.getMessageKey())
                .httpStatus(ex.getHttpStatus())
                .timestamp(nowUtc())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(apiError, ex.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiErrorResponse apiError = ApiErrorResponse.builder()
                .message(localize("error.validation.failed", null, "Validation failed", locale))
                .messageKey("error.validation.failed")
                .errors(errors)
                .httpStatus(HttpStatus.BAD_REQUEST)
                .timestamp(nowUtc())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        ApiErrorResponse apiError = ApiErrorResponse.builder()
                .message(localize("error.auth.bad_credentials", null, "Invalid email or password", locale))
                .messageKey("error.auth.bad_credentials")
                .httpStatus(HttpStatus.UNAUTHORIZED)
                .timestamp(nowUtc())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        ApiErrorResponse apiError = ApiErrorResponse.builder()
                .message(localize("error.auth.forbidden", null, "Forbidden", locale))
                .messageKey("error.auth.forbidden")
                .httpStatus(HttpStatus.FORBIDDEN)
                .timestamp(nowUtc())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        ApiErrorResponse apiError = ApiErrorResponse.builder()
                .message(localize("error.unexpected", null, "An unexpected error occurred", locale))
                .messageKey("error.unexpected")
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .timestamp(nowUtc())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ZonedDateTime nowUtc() {
        return ZonedDateTime.now(ZoneId.of("Z"));
    }

    private String localize(String messageKey, Object[] messageArgs, String fallback, Locale locale) {
        if (messageKey == null || messageKey.isBlank()) {
            return fallback;
        }
        return messageSource.getMessage(messageKey, messageArgs, fallback, locale);
    }
}
