package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.models.UserRole;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    @Query(value = "SELECT * FROM user WHERE LOWER(username) = LOWER(:username) LIMIT 1", nativeQuery = true)
    Optional<User> findByUsernameIgnoreCase(String username);

    @Query(value = "SELECT * FROM user WHERE LOWER(email) = LOWER(:email) LIMIT 1", nativeQuery = true)
    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findByRole(UserRole role);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.userId = :id")
    Optional<User> findByIdWithLock(@Param("id") Long id);

    long countByCreatedAtAfter(LocalDateTime date);
}