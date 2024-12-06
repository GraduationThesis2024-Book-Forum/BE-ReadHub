package com.iuh.fit.readhub.dto.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookSearchRequest {
    private String title;
    private String author;
    private String genre;
    private String language;
    private Integer page;
    private Integer size;
}