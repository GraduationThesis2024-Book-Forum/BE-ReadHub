package com.iuh.fit.readhub.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookSearchResponse {
    private List<BookDTO> books;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;
}