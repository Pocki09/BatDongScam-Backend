package com.se100.bds.entities.report;

import com.se100.bds.entities.AbstractBaseEntity;
import com.se100.bds.utils.Constants;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Report")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "report_id", nullable = false)),
})
@Inheritance(strategy = InheritanceType.JOINED)
public class Report extends AbstractBaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private Constants.ReportTypeEnum reportType;

    @Column(name = "report_month")
    private Integer reportMonth;

    @Column(name = "report_year")
    private Integer reportYear;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_format", nullable = false)
    private String fileFormat;

    @Column(name = "status")
    private String status;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToOne(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private FinancialReport financialReport;

    @OneToOne(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private AgentPerformanceReport agentPerformanceReport;

    @OneToOne(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private PropertyStatisticsReport propertyStatisticsReport;

    @OneToOne(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private CustomerAnalyticsReport customerAnalyticsReport;

    @OneToOne(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private ViolationReportDetails violationReportDetails;
}
