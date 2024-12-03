package com.iuh.fit.readhub.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BookDTO {
    private Long id;
    private String title;
    private String author;
    private String coverUrl;
}