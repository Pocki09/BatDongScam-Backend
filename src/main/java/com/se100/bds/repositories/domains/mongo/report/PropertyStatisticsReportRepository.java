package com.se100.bds.repositories.domains.mongo.report;

import com.se100.bds.models.schemas.report.PropertyStatisticsReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyStatisticsReportRepository extends MongoRepository<PropertyStatisticsReport, String> {
    @Query("{ 'base_report_data.year': ?0, 'base_report_data.month': ?1 }")
    Optional<PropertyStatisticsReport> findByYearAndMonth(int year, int month);
}
