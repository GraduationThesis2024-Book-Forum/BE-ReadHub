package com.iuh.fit.readhub.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "forum_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "forum_id")
    private Discussion forum;

    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;

    private String reason;
    private LocalDateTime reportedAt;
    private String status; // PENDING, RESOLVED, REJECTED
}