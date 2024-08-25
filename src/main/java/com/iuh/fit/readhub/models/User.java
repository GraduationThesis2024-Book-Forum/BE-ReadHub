package com.iuh.fit.readhub.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "User")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user")
    private Set<Note> notes;

    @OneToMany(mappedBy = "user")
    private Set<Discussion> discussions;

    @OneToMany(mappedBy = "user")
    private Set<Rating> ratings;

    @OneToMany(mappedBy = "user")
    private Set<ReadingHistory> readingHistories;

    @OneToMany(mappedBy = "user")
    private Set<Recommendation> recommendations;

    @OneToMany(mappedBy = "user")
    private Set<UserBookProgress> userBookProgresses;

    @OneToMany(mappedBy = "user")
    private Set<UserGenrePreference> userGenrePreferences;


    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", createdAt=" + createdAt +
                ", notes=" + notes +
                ", discussions=" + discussions +
                ", ratings=" + ratings +
                ", readingHistories=" + readingHistories +
                ", recommendations=" + recommendations +
                ", userBookProgresses=" + userBookProgresses +
                ", userGenrePreferences=" + userGenrePreferences +
                '}';
    }
}

