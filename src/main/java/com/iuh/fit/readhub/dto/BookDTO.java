package com.iuh.fit.readhub.dto;


import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class BookDTO {
    private Integer id;
    private String title;
    private List<AuthorDTO> authors;
    private List<String> genres;
    private String language;
    private Map<String, String> formats;
    private String coverUrl;
}