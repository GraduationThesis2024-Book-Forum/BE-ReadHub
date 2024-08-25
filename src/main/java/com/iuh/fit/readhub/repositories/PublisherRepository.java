package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {
}