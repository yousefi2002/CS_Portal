package com.manus.digitalecosystem.exception;

import com.manus.digitalecosystem.dto.response.Response;
import com.manus.digitalecosystem.util.ApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final ApiResponseFactory apiResponseFactory;

    public GlobalExceptionHandler(ApiResponseFactory apiResponseFactory) {
        this.apiResponseFactory = apiResponseFactory;
    }

    @ExceptionHandler(value = {ApiExceptionBase.class})
    public ResponseEntity<Response<Object>> handleApiExceptionBase(ApiExceptionBase ex, HttpServletRequest request) {
        Map<String, Object> errors = Map.of("path", request.getRequestURI());
        return apiResponseFactory.error(ex.getHttpStatus(), ex.getMessageKey(), ex.getMessage(), errors, ex.getMessageArgs());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Object>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            errors.put(fieldName, error.getDefaultMessage());
        });
        errors.put("path", request.getRequestURI());

        return apiResponseFactory.error(
                HttpStatus.BAD_REQUEST,
                "error.validation.failed",
                "Validation failed",
                errors
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Response<Object>> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        String messageKey = "error.auth.bad_credentials";
        String fallback = "Invalid email or password";

        if (ex instanceof DisabledException) {
            status = HttpStatus.FORBIDDEN;
            messageKey = "error.auth.user_disabled";
            fallback = "Account is disabled";
        } else if (!(ex instanceof BadCredentialsException)) {
            messageKey = "error.auth.unauthorized";
            fallback = "Unauthorized";
        }

        return apiResponseFactory.error(
                status,
                messageKey,
                fallback,
                Map.of("path", request.getRequestURI())
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response<Object>> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        return apiResponseFactory.error(
                HttpStatus.FORBIDDEN,
                "error.auth.forbidden",
                "Forbidden",
                Map.of("path", request.getRequestURI())
        );
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Response<Object>> handleGeneralException(Exception ex, HttpServletRequest request) {
        return apiResponseFactory.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "error.unexpected",
                "An unexpected error occurred",
                Map.of("path", request.getRequestURI())
        );
    }
}
