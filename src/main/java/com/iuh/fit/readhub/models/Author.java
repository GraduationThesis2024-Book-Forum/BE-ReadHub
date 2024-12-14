package com.iuh.fit.readhub.models;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Author {
    private String name;
    private Long birthYear;
    private Long deathYear;
}