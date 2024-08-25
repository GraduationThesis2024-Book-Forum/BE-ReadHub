package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}