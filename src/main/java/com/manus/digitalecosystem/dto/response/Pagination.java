package com.manus.digitalecosystem.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Pagination {
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
}

