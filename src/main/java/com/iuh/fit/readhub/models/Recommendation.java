package com.iuh.fit.readhub.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Recommendation")
public class Recommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recommendationId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private String reason;
    private String recommendationType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;



    @Override
    public String toString() {
        return "Recommendation{" +
                "recommendationId=" + recommendationId +
                ", user=" + user +
                ", book=" + book +
                ", reason='" + reason + '\'' +
                ", recommendationType='" + recommendationType + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}