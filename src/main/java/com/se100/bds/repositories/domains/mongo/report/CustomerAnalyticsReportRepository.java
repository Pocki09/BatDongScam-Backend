package com.se100.bds.repositories.domains.mongo.report;

import com.se100.bds.models.schemas.report.CustomerAnalyticsReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerAnalyticsReportRepository extends MongoRepository<CustomerAnalyticsReport, String> {
    List<CustomerAnalyticsReport> findAllByBaseReportData_Year(Integer baseReportDataYear);
}
