package com.iuh.fit.readhub.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    private String title;
    private String language;

    private String coverImage;
    private String link;
    @OneToMany(mappedBy = "book")
    private Set<BookCategory> bookCategories;


    @OneToMany(mappedBy = "book")
    private Set<BookAuthor> bookAuthors;

    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", title='" + title + '\'' +
                ", language=" + language +
                ", coverImage='" + coverImage + '\'' +
                ", link='" + link + '\'' +
                ", bookCategories=" + bookCategories +
                ", bookAuthors=" + bookAuthors +
                '}';
    }
}
