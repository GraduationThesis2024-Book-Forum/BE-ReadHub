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
@Table(name = "Note")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noteId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private String content;
    private Integer pageNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Float positionInPage;


    @Override
    public String toString() {
        return "Note{" +
                "noteId=" + noteId +
                ", user=" + user +
                ", book=" + book +
                ", content='" + content + '\'' +
                ", pageNumber=" + pageNumber +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", positionInPage=" + positionInPage +
                '}';
    }
}