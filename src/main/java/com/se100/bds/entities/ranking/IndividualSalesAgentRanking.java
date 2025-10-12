package com.se100.bds.entities.ranking;

import com.se100.bds.entities.AbstractBaseEntity;
import com.se100.bds.entities.user.SaleAgent;
import com.se100.bds.utils.Constants;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "individual_sales_agent_rankings")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "ranking_id", nullable = false)),
})
public class IndividualSalesAgentRanking extends AbstractBaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private SaleAgent agent;

    @Column(name = "ranking_month")
    private Integer rankingMonth;

    @Column(name = "ranking_year")
    private Integer rankingYear;

    @Column(name = "month_revenue", precision = 15, scale = 2)
    private BigDecimal monthRevenue;

    @Column(name = "month_deals")
    private Integer monthDeals;

    @Column(name = "month_properties_assigned")
    private Integer monthPropertiesAssigned;

    @Column(name = "month_appointments_completed")
    private Integer monthAppointmentsCompleted;

    @Column(name = "month_customer_satisfaction_avg", precision = 3, scale = 2)
    private BigDecimal monthCustomerSatisfactionAvg;

    @Enumerated(EnumType.STRING)
    @Column(name = "performance_tier")
    private Constants.PerformanceTierEnum performanceTier;

    @Column(name = "ranking_position")
    private Integer rankingPosition;
}
