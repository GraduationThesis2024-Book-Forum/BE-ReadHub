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
@Table(name = "ReadingHistory")
public class ReadingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private Long bookId;
    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;



    @Override
    public String toString() {
        return "ReadingHistory{" +
                "historyId=" + historyId +
                ", user=" + user +
                ", bookId=" + bookId +
                ", createdAt=" + createdAt +
                '}';
    }
}