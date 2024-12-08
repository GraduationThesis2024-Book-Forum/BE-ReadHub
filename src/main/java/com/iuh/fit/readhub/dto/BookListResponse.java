package com.iuh.fit.readhub.dto;

import com.iuh.fit.readhub.models.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookListResponse {
    private Long count;
    private String next;
    private String previous;
    private List<Book> results;
}