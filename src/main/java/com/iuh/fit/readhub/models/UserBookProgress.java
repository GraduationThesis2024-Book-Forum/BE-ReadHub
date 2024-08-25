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
@Table(name = "UserBookProgress")
public class UserBookProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long progressId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private Integer lastPageRead;
    private Float progressPercentage;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;



    @Override
    public String toString() {
        return "UserBookProgress{" +
                "progressId=" + progressId +
                ", user=" + user +
                ", book=" + book +
                ", lastPageRead=" + lastPageRead +
                ", progressPercentage=" + progressPercentage +
                ", lastReadAt=" + lastReadAt +
                '}';
    }
}
