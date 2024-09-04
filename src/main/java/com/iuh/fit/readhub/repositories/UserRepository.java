package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}