package com.se100.bds.entities.report;

import com.se100.bds.entities.AbstractBaseEntity;
import com.se100.bds.entities.location.City;
import com.se100.bds.entities.location.District;
import com.se100.bds.entities.location.Ward;
import com.se100.bds.entities.property.PropertyType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "FinancialReport")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinancialReport extends AbstractBaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "report_id", referencedColumnName = "report_id")
    private Report report;

    @Column(name = "total_revenue_current_month", precision = 15, scale = 2)
    private BigDecimal totalRevenueCurrentMonth;

    @Column(name = "total_revenue", precision = 15, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "total_service_fees_current_month", precision = 15, scale = 2)
    private BigDecimal totalServiceFeesCurrentMonth;

    @Column(name = "total_service_fees", precision = 15, scale = 2)
    private BigDecimal totalServiceFees;

    @Column(name = "contract_count_current_month")
    private Integer contractCountCurrentMonth;

    @Column(name = "contract_count")
    private Integer contractCount;

    @Column(name = "total_commission_earned_current_month", precision = 15, scale = 2)
    private BigDecimal totalCommissionEarnedCurrentMonth;

    @Column(name = "total_commission_earned", precision = 15, scale = 2)
    private BigDecimal totalCommissionEarned;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "top_performing_city_id", nullable = false)
    private City topPerformingCity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "top_performing_district_id", nullable = false)
    private District topPerformingDistrict;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "top_performing_ward_id")
    private Ward topPerformingWard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "top_performing_property_type_id")
    private PropertyType topPerformingPropertyType;

    @Column(name = "net_profit", precision = 15, scale = 2)
    private BigDecimal netProfit;

    @Column(name = "avg_property_price", precision = 15, scale = 2)
    private BigDecimal avgPropertyPrice;

    @Column(name = "total_rates")
    private Integer totalRates;

    @Column(name = "avg_rating", precision = 5, scale = 2)
    private BigDecimal avgRating;

    @Column(name = "total_rates_current_month")
    private Integer totalRatesCurrentMonth;

    @Column(name = "avg_rating_current_month", precision = 5, scale = 2)
    private BigDecimal avgRatingCurrentMonth;
}

