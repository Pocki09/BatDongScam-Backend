package com.se100.bds.repositories.domains.violation;

import com.se100.bds.models.entities.violation.ViolationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ViolationRepository extends JpaRepository<ViolationReport, UUID>, JpaSpecificationExecutor<ViolationReport> {
}

