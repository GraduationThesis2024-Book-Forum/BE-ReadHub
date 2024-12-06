package com.iuh.fit.readhub.dto.request;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookSearchRequest {
    private String title;
    private String author;
    private List<String> genres;
    private List<String> languages;
    private Integer page;
    private Integer size;
}