package com.se100.bds.repositories.domains.review;

import com.se100.bds.models.entities.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID>, JpaSpecificationExecutor<Review> {
}

