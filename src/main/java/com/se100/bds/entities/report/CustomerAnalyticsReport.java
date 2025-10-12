package com.se100.bds.entities.report;

import com.se100.bds.entities.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "customer_analytics_reports")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAnalyticsReport extends AbstractBaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "report_id", referencedColumnName = "report_id")
    private Report report;

    @Column(name = "total_active_customers")
    private Integer totalActiveCustomers;

    @Column(name = "new_customers_acquired_current_month")
    private Integer newCustomersAcquiredCurrentMonth;

    @Column(name = "customer_churn_rate", precision = 5, scale = 2)
    private BigDecimal customerChurnRate;

    @Column(name = "avg_customer_transaction_value", precision = 15, scale = 2)
    private BigDecimal avgCustomerTransactionValue;

    @Column(name = "high_value_customers_count")
    private Integer highValueCustomersCount;

    @Column(name = "customer_satisfaction_score", precision = 5, scale = 2)
    private BigDecimal customerSatisfactionScore;

    @Column(name = "total_rates")
    private Integer totalRates;

    @Column(name = "avg_rating", precision = 5, scale = 2)
    private BigDecimal avgRating;

    @Column(name = "total_rates_current_month")
    private Integer totalRatesCurrentMonth;

    @Column(name = "avg_rating_current_month", precision = 5, scale = 2)
    private BigDecimal avgRatingCurrentMonth;
}
