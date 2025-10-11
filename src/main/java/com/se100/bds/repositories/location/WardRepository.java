package com.se100.bds.repositories.location;

import com.se100.bds.entities.location.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WardRepository extends JpaRepository<Ward, UUID>, JpaSpecificationExecutor<Ward> {
}
