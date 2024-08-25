package com.iuh.fit.readhub.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "DiscussionParticipant")
public class DiscussionParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long participantId;

    @ManyToOne
    @JoinColumn(name = "discussion_id", nullable = false)
    private Discussion discussion;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private RoleDiscussion role;

    @Enumerated(EnumType.STRING)
    private DiscussionStatus status;



    @Override
    public String toString() {
        return "DiscussionParticipant{" +
                "participantId=" + participantId +
                ", discussion=" + discussion +
                ", user=" + user +
                ", role=" + role +
                ", status=" + status +
                '}';
    }
}
