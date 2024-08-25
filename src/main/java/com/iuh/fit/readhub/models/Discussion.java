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
@Table(name = "Discussion")
public class Discussion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long discussionId;

    private String title;
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany
    private Set<Comment> comments;

    @OneToMany
    private Set<DiscussionParticipant> discussionParticipants;

    @Override
    public String toString() {
        return "Discussion{" +
                "discussionId=" + discussionId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", user=" + user +
                ", updatedAt=" + updatedAt +
                ", comments=" + comments +
                ", discussionParticipants=" + discussionParticipants +
                '}';
    }
}
