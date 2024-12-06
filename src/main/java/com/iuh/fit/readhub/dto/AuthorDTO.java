package com.iuh.fit.readhub.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthorDTO {
    private String name;
    private Integer birthYear;
    private Integer deathYear;
}
