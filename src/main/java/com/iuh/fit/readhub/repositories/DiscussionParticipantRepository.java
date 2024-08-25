package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.DiscussionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscussionParticipantRepository extends JpaRepository<DiscussionParticipant, Long> {
}