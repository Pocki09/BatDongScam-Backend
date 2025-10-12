package com.se100.bds.entities.report;

import com.se100.bds.entities.AbstractBaseEntity;
import com.se100.bds.entities.user.SaleAgent;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "agent_performance_reports")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentPerformanceReport extends AbstractBaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "report_id", referencedColumnName = "report_id")
    private Report report;

    @Column(name = "total_ agents")
    private Integer totalAgents;

    @Column(name = "total_active_agents")
    private Integer totalActiveAgents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "top_performer_agent_id_current_month", nullable = false)
    private SaleAgent topPerformerAgentCurrentMonth;

    @Column(name = "top_performer_revenue_current_month", precision = 15, scale = 2)
    private BigDecimal topPerformerRevenueCurrentMonth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "top_performer_agent_id", nullable = false)
    private SaleAgent topPerformerAgent;

    @Column(name = "top_performer_revenue", precision = 15, scale = 2)
    private BigDecimal topPerformerRevenue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bottom_performer_agent_id_current_month", nullable = false)
    private SaleAgent bottomPerformerAgentCurrentMonth;

    @Column(name = "bottom_performer_revenue_current_month", precision = 15, scale = 2)
    private BigDecimal bottomPerformerRevenueCurrentMonth;

    @Column(name = "avg_revenue_per_agent", precision = 15, scale = 2)
    private BigDecimal avgRevenuePerAgent;

    @Column(name = "avg_customer_satisfaction", precision = 15, scale = 2)
    private BigDecimal avgCustomerSatisfaction;

    @Column(name = "total_rates")
    private Integer totalRates;

    @Column(name = "avg_rating", precision = 5, scale = 2)
    private BigDecimal avgRating;

    @Column(name = "total_rates_current_month")
    private Integer totalRatesCurrentMonth;

    @Column(name = "avg_rating_current_month", precision = 5, scale = 2)
    private BigDecimal avgRatingCurrentMonth;
}
