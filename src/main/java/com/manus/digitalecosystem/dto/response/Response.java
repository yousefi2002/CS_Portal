package com.manus.digitalecosystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {
    private boolean success;
    private int status;
    private String message;
    private T data;
    private Map<String, Object> errors;
    private Map<String, Object> meta;
    private Pagination pagination;
    private LocalDateTime timestamp;
    private String requestId;
}

