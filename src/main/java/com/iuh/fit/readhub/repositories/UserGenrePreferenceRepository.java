package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.UserGenrePreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGenrePreferenceRepository extends JpaRepository<UserGenrePreference, Long> {
}