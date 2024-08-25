package com.iuh.fit.readhub.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "BookCategory")
public class BookCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookCategoryId;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private String categoryName;

    @Override
    public String toString() {
        return "BookCategory{" +
                "bookCategoryId=" + bookCategoryId +
                ", book=" + book +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}

