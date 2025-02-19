package com.iuh.fit.readhub.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Long bookId;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String review;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Thêm mối quan hệ với ReviewReact
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
    private List<ReviewReact> reviewReacts = new ArrayList<>(); // Initialize empty list
}

