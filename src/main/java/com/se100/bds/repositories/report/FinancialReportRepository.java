package com.se100.bds.repositories.report;

import com.se100.bds.entities.report.FinancialReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FinancialReportRepository extends JpaRepository<FinancialReport, UUID>, JpaSpecificationExecutor<FinancialReport> {
}

