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
@Table(name = "Author")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long authorId;

    private String name;
    private LocalDate birthDate;
    private LocalDate deathDate;


    @OneToMany(mappedBy = "author")
    private Set<BookAuthor> bookAuthors;

    @Override
    public String toString() {
        return "Author{" +
                "authorId=" + authorId +
                ", name='" + name + '\'' +
                ", birthDate=" + birthDate +
                ", deathDate=" + deathDate +
                ", bookAuthors=" + bookAuthors +
                '}';
    }
}
