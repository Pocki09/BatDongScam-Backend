package com.se100.bds.services.domains.report.impl;

import com.se100.bds.dtos.responses.statisticreport.AgentPerformanceStats;
import com.se100.bds.dtos.responses.statisticreport.CustomerStats;
import com.se100.bds.dtos.responses.statisticreport.PropertyOwnerStats;
import com.se100.bds.models.schemas.ranking.IndividualSalesAgentPerformanceMonth;
import com.se100.bds.models.schemas.report.AgentPerformanceReport;
import com.se100.bds.models.schemas.ranking.IndividualCustomerPotentialMonth;
import com.se100.bds.models.schemas.ranking.IndividualPropertyOwnerContributionMonth;
import com.se100.bds.models.schemas.report.CustomerAnalyticsReport;
import com.se100.bds.models.schemas.report.PropertyOwnerContributionReport;
import com.se100.bds.repositories.domains.mongo.ranking.IndividualSalesAgentPerformanceMonthRepository;
import com.se100.bds.repositories.domains.mongo.report.AgentPerformanceReportRepository;
import com.se100.bds.repositories.domains.mongo.ranking.IndividualCustomerPotentialMonthRepository;
import com.se100.bds.repositories.domains.mongo.ranking.IndividualPropertyOwnerContributionMonthRepository;
import com.se100.bds.repositories.domains.mongo.report.CustomerAnalyticsReportRepository;
import com.se100.bds.repositories.domains.mongo.report.PropertyOwnerContributionReportRepository;
import com.se100.bds.services.domains.ranking.RankingService;
import com.se100.bds.services.domains.report.ReportService;
import com.se100.bds.services.domains.report.scheduler.UserReportScheduler;
import com.se100.bds.services.domains.user.UserService;
import com.se100.bds.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final RankingService rankingService;
    private final UserService userService;
    private final UserReportScheduler userReportScheduler; // God forgive me
    private final AgentPerformanceReportRepository agentPerformanceReportRepository;
    private final IndividualSalesAgentPerformanceMonthRepository individualSalesAgentPerformanceMonthRepository;

    // Added repositories for CustomerStats and PropertyOwnerStats
    private final CustomerAnalyticsReportRepository customerAnalyticsReportRepository;
    private final PropertyOwnerContributionReportRepository propertyOwnerContributionReportRepository;
    private final IndividualCustomerPotentialMonthRepository individualCustomerPotentialMonthRepository;
    private final IndividualPropertyOwnerContributionMonthRepository individualPropertyOwnerContributionMonthRepository;

    @Override
    public AgentPerformanceStats getAgentPerformanceStats(int year) {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        if (year > currentYear) return null;
        if (currentYear == year) {
            // Generate to get the latest current month data
            // Use join to await them
            userReportScheduler.generateAgentPerformanceReport(currentMonth, year).join();
        }
        List<AgentPerformanceReport> agentPerformanceReports = agentPerformanceReportRepository.findAllByBaseReportData_Year(year);

        AgentPerformanceStats agentPerformanceStats = new AgentPerformanceStats();
        Map<Integer, Integer> totalAgents = new HashMap<>();
        Map<Integer, Double> avgRating = new HashMap<>();
        Map<Integer, Integer> totalRates = new HashMap<>();
        Map<Integer, Double> customerSatisfaction = new HashMap<>();


        for (AgentPerformanceReport agentPerformanceReport : agentPerformanceReports) {
            int month = agentPerformanceReport.getBaseReportData().getMonth();
            totalAgents.put(month, agentPerformanceReport.getTotalAgents());
            avgRating.put(month, agentPerformanceReport.getAvgRating().doubleValue());
            totalRates.put(month, agentPerformanceReport.getTotalRates());
            customerSatisfaction.put(month, agentPerformanceReport.getAvgCustomerSatisfaction().doubleValue());
        }

        agentPerformanceStats.setTotalAgents(totalAgents);
        agentPerformanceStats.setAvgRating(avgRating);
        agentPerformanceStats.setTotalRates(totalRates);
        agentPerformanceStats.setCustomerSatisfaction(customerSatisfaction);

        List<IndividualSalesAgentPerformanceMonth> salesAgentPerformanceMonths = individualSalesAgentPerformanceMonthRepository.findAllByMonthAndYear(currentMonth, year);
        Map<Constants.PerformanceTierEnum, Integer> tierNumber = Arrays.stream(Constants.PerformanceTierEnum.values())
                .collect(Collectors.toMap(e -> e, e -> 0));

        for (IndividualSalesAgentPerformanceMonth salesAgentPerformanceMonth : salesAgentPerformanceMonths) {
            Constants.PerformanceTierEnum tier = salesAgentPerformanceMonth.getPerformanceTier();
            tierNumber.put(tier, tierNumber.get(tier) + 1);
        }

        int totalAgent = salesAgentPerformanceMonths.size();
        Map<Constants.PerformanceTierEnum, Map<Integer, Double>> tierDistribution = new HashMap<>();
        for (Map.Entry<Constants.PerformanceTierEnum, Integer> entry : tierNumber.entrySet()) {
            Constants.PerformanceTierEnum tier = entry.getKey();
            int count = entry.getValue();
            double percentage = totalAgent > 0 ? (double) count / totalAgent * 100 : 0.0;
            tierDistribution.put(tier, Map.of(count, percentage));
        }

        agentPerformanceStats.setTierDistribution(tierDistribution);

        return agentPerformanceStats;
    }

    @Override
    public CustomerStats getCustomerStats(int year) {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        if (year > currentYear) return null;
        if (currentYear == year) {
            // Generate to get the latest current month data
            userReportScheduler.generateCustomerAnalyticsReport(currentMonth, year).join();
        }
        List<CustomerAnalyticsReport> customerAnalyticsReports = customerAnalyticsReportRepository.findAllByBaseReportData_Year(year);

        CustomerStats customerStats = new CustomerStats();
        Map<Integer, Integer> totalCustomers = new HashMap<>();
        Map<Integer, BigDecimal> totalSpending = new HashMap<>();
        Map<Integer, BigDecimal> avgSpendingPerCustomer = new HashMap<>();

        for (CustomerAnalyticsReport customerAnalyticsReport : customerAnalyticsReports) {
            int month = customerAnalyticsReport.getBaseReportData().getMonth();
            totalCustomers.put(month, customerAnalyticsReport.getTotalCustomers());
            BigDecimal avg = customerAnalyticsReport.getAvgCustomerTransactionValue();
            avgSpendingPerCustomer.put(month, avg);
            BigDecimal total = avg.multiply(BigDecimal.valueOf(customerAnalyticsReport.getTotalCustomers()));
            totalSpending.put(month, total);
        }

        customerStats.setTotalCustomers(totalCustomers);
        customerStats.setTotalSpending(totalSpending);
        customerStats.setAvgSpendingPerCustomer(avgSpendingPerCustomer);

        List<IndividualCustomerPotentialMonth> customerPotentialMonths = individualCustomerPotentialMonthRepository.findAllByMonthAndYear(currentMonth, year);
        Map<Constants.CustomerTierEnum, Integer> tierNumber = Arrays.stream(Constants.CustomerTierEnum.values())
                .collect(Collectors.toMap(e -> e, e -> 0));

        for (IndividualCustomerPotentialMonth customerPotentialMonth : customerPotentialMonths) {
            Constants.CustomerTierEnum tier = customerPotentialMonth.getCustomerTier();
            tierNumber.put(tier, tierNumber.get(tier) + 1);
        }

        int totalCustomer = customerPotentialMonths.size();
        Map<Constants.CustomerTierEnum, Map<Integer, Double>> tierDistribution = new HashMap<>();
        for (Map.Entry<Constants.CustomerTierEnum, Integer> entry : tierNumber.entrySet()) {
            Constants.CustomerTierEnum tier = entry.getKey();
            int count = entry.getValue();
            double percentage = totalCustomer > 0 ? (double) count / totalCustomer * 100 : 0.0;
            tierDistribution.put(tier, Map.of(count, percentage));
        }

        customerStats.setTierDistribution(tierDistribution);

        return customerStats;
    }

    @Override
    public PropertyOwnerStats getPropertyOwnerStats(int year) {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        if (year > currentYear) return null;
        if (currentYear == year) {
            // Generate to get the latest current month data
            userReportScheduler.generatePropertyOwnerContributionReport(currentMonth, year).join();
        }
        List<PropertyOwnerContributionReport> propertyOwnerContributionReports = propertyOwnerContributionReportRepository.findAllByBaseReportData_Year(year);

        PropertyOwnerStats propertyOwnerStats = new PropertyOwnerStats();
        Map<Integer, Integer> totalOwners = new HashMap<>();
        Map<Integer, BigDecimal> totalContributionValue = new HashMap<>();
        Map<Integer, BigDecimal> avgContributionPerOwner = new HashMap<>();

        for (PropertyOwnerContributionReport propertyOwnerContributionReport : propertyOwnerContributionReports) {
            int month = propertyOwnerContributionReport.getBaseReportData().getMonth();
            totalOwners.put(month, propertyOwnerContributionReport.getTotalOwners());
            totalContributionValue.put(month, propertyOwnerContributionReport.getContributionValue());
            avgContributionPerOwner.put(month, propertyOwnerContributionReport.getAvgOwnersContributionValue());
        }

        propertyOwnerStats.setTotalOwners(totalOwners);
        propertyOwnerStats.setTotalContributionValue(totalContributionValue);
        propertyOwnerStats.setAvgContributionPerOwner(avgContributionPerOwner);

        List<IndividualPropertyOwnerContributionMonth> ownerContributionMonths = individualPropertyOwnerContributionMonthRepository.findAllByMonthAndYear(currentMonth, year);
        Map<Constants.ContributionTierEnum, Integer> tierNumber = Arrays.stream(Constants.ContributionTierEnum.values())
                .collect(Collectors.toMap(e -> e, e -> 0));

        for (IndividualPropertyOwnerContributionMonth ownerContributionMonth : ownerContributionMonths) {
            Constants.ContributionTierEnum tier = ownerContributionMonth.getContributionTier();
            tierNumber.put(tier, tierNumber.get(tier) + 1);
        }

        int totalOwner = ownerContributionMonths.size();
        Map<Constants.ContributionTierEnum, Map<Integer, Double>> tierDistribution = new HashMap<>();
        for (Map.Entry<Constants.ContributionTierEnum, Integer> entry : tierNumber.entrySet()) {
            Constants.ContributionTierEnum tier = entry.getKey();
            int count = entry.getValue();
            double percentage = totalOwner > 0 ? (double) count / totalOwner * 100 : 0.0;
            tierDistribution.put(tier, Map.of(count, percentage));
        }

        propertyOwnerStats.setTierDistribution(tierDistribution);

        return propertyOwnerStats;
    }
}
