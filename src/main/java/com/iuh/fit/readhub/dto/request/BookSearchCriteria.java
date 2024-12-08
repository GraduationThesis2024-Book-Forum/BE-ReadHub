package com.iuh.fit.readhub.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSearchCriteria {
    private String title;
    private String author;
    private List<String> languages;
    private List<String> subjects;
    private List<String> bookshelves;
}
