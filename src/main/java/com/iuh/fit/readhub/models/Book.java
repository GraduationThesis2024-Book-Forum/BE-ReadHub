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
    private String description;
    private LocalDate publishedDate;
    private Language language;

    @Column(unique = true)
    private String ISBN;

    private String coverImage;
    private String externalApiId;
    private String format;

    @OneToMany(mappedBy = "book")
    private Set<Rating> ratings;

    @OneToMany(mappedBy = "book")
    private Set<BookCategory> bookCategories;

    @OneToMany(mappedBy = "book")
    private Set<BookTag> bookTags;

    @OneToMany(mappedBy = "book")
    private Set<UserBookProgress> userBookProgresses;

    @OneToMany(mappedBy = "book")
    private Set<ReadingHistory> readingHistories;

    @OneToMany(mappedBy = "book")
    private Set<BookAuthor> bookAuthors;

    @ManyToOne
    @JoinColumn(name="series_id", nullable = true)
    private Series series;

    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", publishedDate=" + publishedDate +
                ", language=" + language +
                ", ISBN='" + ISBN + '\'' +
                ", coverImage='" + coverImage + '\'' +
                ", externalApiId='" + externalApiId + '\'' +
                ", format='" + format + '\'' +
                ", ratings=" + ratings +
                ", bookCategories=" + bookCategories +
                ", bookTags=" + bookTags +
                ", userBookProgresses=" + userBookProgresses +
                ", readingHistories=" + readingHistories +
                ", bookAuthors=" + bookAuthors +
                '}';
    }


}
