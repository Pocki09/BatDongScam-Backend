package com.se100.bds.repositories.report;

import com.se100.bds.entities.report.PropertyStatisticsReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PropertyStatisticsReportRepository extends JpaRepository<PropertyStatisticsReport, UUID>, JpaSpecificationExecutor<PropertyStatisticsReport> {
}

