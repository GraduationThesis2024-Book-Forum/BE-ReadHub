package com.iuh.fit.readhub.models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookInfo {
    private Long id;
    private String title;
    private String author;
    private String coverUrl;
}