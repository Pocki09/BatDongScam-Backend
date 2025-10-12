package com.se100.bds.entities.report;

import com.se100.bds.entities.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "violation_report_details")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ViolationReportDetails extends AbstractBaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "report_id", referencedColumnName = "report_id")
    private Report report;

    @Column(name = "total_violations_reported")
    private Integer totalViolationsReported;

    @Column(name = "total_violations_reported_current_month")
    private Integer totalViolationsReportedCurrentMonth;

    @Column(name = "serious_violations_count_curent_month")
    private Integer seriousViolationsCountCurrentMonth;

    @Column(name = "serious_violations_count")
    private Integer seriousViolationsCount;

    @Column(name = "minor_violations_count_curent_month")
    private Integer minorViolationsCountCurrentMonth;

    @Column(name = "minor_violations_count")
    private Integer minorViolationsCount;

    @Column(name = "avg_resolution_time_hours")
    private Integer avgResolutionTimeHours;

    @Column(name = "violation_trend", precision = 5, scale = 2)
    private BigDecimal violationTrend;

    @Column(name = "most_common_violation_type")
    private String mostCommonViolationType;

    @Column(name = "accounts_suspended")
    private Integer accountsSuspended;

    @Column(name = "compliance_rate", precision = 5, scale = 2)
    private BigDecimal complianceRate;
}
