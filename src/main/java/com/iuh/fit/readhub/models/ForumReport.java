package com.iuh.fit.readhub.models;

import com.iuh.fit.readhub.constants.ReportReason;
import com.iuh.fit.readhub.constants.ReportStatus;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "reason")
    private ReportReason reason;

    private String additionalInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReportStatus status = ReportStatus.PENDING;

    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        reportedAt = LocalDateTime.now();
    }
}