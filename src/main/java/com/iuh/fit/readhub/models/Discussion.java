package com.iuh.fit.readhub.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "Discussion")
public class Discussion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long discussionId;

    private Long bookId;
    private String bookTitle;
    private String authors;

    @Column(nullable = false)
    private String forumTitle;

    @Column(length = 1000)
    private String forumDescription;

    private String imageUrl;

    @ElementCollection
    private List<String> subjects;

    @ElementCollection
    private List<String> categories;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User creator;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany
    private Set<Comment> comments;

    @OneToMany(mappedBy = "discussion")
    private Set<DiscussionMember> members;

    public boolean hasMember(User user) {
        return members.stream()
                .anyMatch(member -> member.getUser().getUserId().equals(user.getUserId()));
    }

    @OneToMany(mappedBy = "discussion", cascade = CascadeType.ALL)
    private Set<DiscussionLike> likes = new HashSet<>();

    @OneToMany(mappedBy = "discussion", cascade = CascadeType.ALL)
    private Set<DiscussionSave> saves = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
