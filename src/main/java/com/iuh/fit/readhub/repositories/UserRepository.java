package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
//   find User by username Or Email
@Query(value = "SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1", nativeQuery = true)
Optional<User> findByUsernameIgnoreCase(String username);

    @Query(value = "SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1", nativeQuery = true)
    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findByRole(UserRole role);
}