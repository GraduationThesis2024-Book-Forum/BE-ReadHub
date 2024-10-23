package com.iuh.fit.readhub.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Bookmark")
public class Bookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookmarkId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Long bookId;

    private String location;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;




    @Override
    public String toString() {
        return "Bookmark{" +
                "bookmarkId=" + bookmarkId +
                ", user=" + user +
                ", bookId=" + bookId +
                ", location='" + location +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

