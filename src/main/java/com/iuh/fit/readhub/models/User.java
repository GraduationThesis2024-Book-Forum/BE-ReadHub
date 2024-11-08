package com.iuh.fit.readhub.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "User")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "full_name")
    private String fullName;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "url_avatar")
    private String urlAvatar;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user")
    private Set<Note> notes;

    @OneToMany(mappedBy = "creator")
    private Set<Discussion> discussions;

    @OneToMany(mappedBy = "user")
    private Set<ReadingHistory> readingHistories;

    @OneToMany(mappedBy = "user")
    private Set<Recommendation> recommendations;

    @OneToMany(mappedBy = "user")
    private Set<UserBookProgress> userBookProgresses;

    @OneToMany(mappedBy = "user")
    private Set<UserGenrePreference> userGenrePreferences;

    @OneToMany(mappedBy = "user")
    private Set<Review> reviews;

    @OneToMany(mappedBy = "user")
    private Set<SavedBook> savedBooks;

}

