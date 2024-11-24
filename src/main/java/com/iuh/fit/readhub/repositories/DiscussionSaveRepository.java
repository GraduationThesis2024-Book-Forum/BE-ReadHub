package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Discussion;
import com.iuh.fit.readhub.models.DiscussionSave;
import com.iuh.fit.readhub.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DiscussionSaveRepository extends JpaRepository<DiscussionSave, Long> {
    boolean existsByDiscussionAndUser(Discussion discussion, User user);
    @Modifying
    @Query("DELETE FROM DiscussionSave fs WHERE fs.discussion = ?1 AND fs.user = ?2")
    void deleteByDiscussionAndUser(Discussion discussion, User user);
    List<DiscussionSave> findByUser(User user);

    @Modifying
    @Query("DELETE FROM DiscussionSave fs WHERE fs.discussion = ?1")
    void deleteByDiscussion(Discussion discussion);
}