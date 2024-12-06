package com.iuh.fit.readhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GutendexBookDTO {
    private Long id;
    private String title;
    private String author;
    private String coverUrl;
}