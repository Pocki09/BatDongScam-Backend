package com.se100.bds.models.schemas.report;

import com.se100.bds.utils.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BaseReportData {
    private Constants.ReportTypeEnum reportType;
    private Integer month;
    private Integer year;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String filePath;
    private String fileName;
    private String fileFormat;
}
