package com.se100.bds.models.schemas.report;

import com.se100.bds.models.schemas.ranking.IndividualSalesAgentPerformanceCareer;
import com.se100.bds.models.schemas.ranking.IndividualSalesAgentPerformanceMonth;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.List;

@Document(collection = "agent_performance_reports")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentPerformanceReport extends AbstractBaseMongoReport {
    @Field("total_agents")
    private Integer totalAgents;

    @Field("avg_revenue_per_agent")
    private BigDecimal avgRevenuePerAgent;

    @Field("avg_customer_satisfaction")
    private BigDecimal avgCustomerSatisfaction;

    @Field("total_rates")
    private Integer totalRates;

    @Field("avg_rating")
    private BigDecimal avgRating;

    @Field("total_rates_current_month")
    private Integer totalRatesCurrentMonth;

    @Field("avg_rating_current_month")
    private BigDecimal avgRatingCurrentMonth;

    @Field("list_performance_month")
    private List<IndividualSalesAgentPerformanceMonth> listPerformanceMonth;

    @Field("list_performance_career")
    private List<IndividualSalesAgentPerformanceCareer> listPerformanceCareer;
}
