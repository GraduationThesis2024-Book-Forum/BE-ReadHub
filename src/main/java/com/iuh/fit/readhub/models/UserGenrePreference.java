package com.iuh.fit.readhub.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "UserGenrePreference")
public class UserGenrePreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userGenrePreferenceId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private Genre genre;



    @Override
    public String toString() {
        return "UserGenrePreference{" +
                "userGenrePreferenceId=" + userGenrePreferenceId +
                ", user=" + user +
                ", genre=" + genre +
                '}';
    }
}

