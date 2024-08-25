package com.iuh.fit.readhub.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "Series")
public class Series {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seriesId;

    @Column(unique = true, nullable = false)
    private String title;

    @OneToMany(mappedBy = "series")
    private Set<Book> books;



    @Override
    public String toString() {
        return "Series{" +
                "seriesId=" + seriesId +
                ", title='" + title + '\'' +
                ", books=" + books +
                '}';
    }
}

