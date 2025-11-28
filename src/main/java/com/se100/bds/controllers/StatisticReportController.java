package com.se100.bds.controllers;

import com.se100.bds.controllers.base.AbstractBaseController;
import com.se100.bds.dtos.responses.SingleResponse;
import com.se100.bds.dtos.responses.statisticreport.AgentPerformanceStats;
import com.se100.bds.dtos.responses.statisticreport.CustomerStats;
import com.se100.bds.dtos.responses.statisticreport.PropertyOwnerStats;
import com.se100.bds.services.domains.report.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.se100.bds.utils.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/statistic-report")
@Tag(name = "012. Statistic Report Controller", description = "Those Report shit, you know. Admin only")
@Slf4j
public class StatisticReportController extends AbstractBaseController {
    private final ReportService reportService;

    @GetMapping("/agent-performance/{year}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get agent performance stats for a year", security = @SecurityRequirement(name = SECURITY_SCHEME_NAME))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved agent performance stats"),
        @ApiResponse(responseCode = "404", description = "Year not found")
    })
    public ResponseEntity<SingleResponse<AgentPerformanceStats>> getAgentPerformanceStats(
            @Parameter(description = "Year for the stats", required = true)
            @PathVariable int year) {
        AgentPerformanceStats stats = reportService.getAgentPerformanceStats(year);
        return responseFactory.successSingle(stats, "Agent performance stats retrieved successfully");
    }

    @GetMapping("/customer/{year}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get customer stats for a year",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved customer stats"),
        @ApiResponse(responseCode = "404", description = "Year not found")
    })
    public ResponseEntity<SingleResponse<CustomerStats>> getCustomerStats(
            @Parameter(description = "Year for the stats", required = true)
            @PathVariable int year) {
        CustomerStats stats = reportService.getCustomerStats(year);
        return responseFactory.successSingle(stats, "Customer stats retrieved successfully");
    }

    @GetMapping("/property-owner/{year}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get property owner stats for a year", security = @SecurityRequirement(name = SECURITY_SCHEME_NAME))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved property owner stats"),
        @ApiResponse(responseCode = "404", description = "Year not found")
    })
    public ResponseEntity<SingleResponse<PropertyOwnerStats>> getPropertyOwnerStats(
            @Parameter(description = "Year for the stats", required = true)
            @PathVariable int year) {
        PropertyOwnerStats stats = reportService.getPropertyOwnerStats(year);
        return responseFactory.successSingle(stats, "Property owner stats retrieved successfully");
    }
}
