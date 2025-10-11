package com.se100.bds.entities.ranking;

import com.se100.bds.entities.AbstractBaseEntity;
import com.se100.bds.entities.user.Customer;
import com.se100.bds.utils.Constants;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "IndividualCustomerLead")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "lead_id", nullable = false)),
})
public class IndividualCustomerLead extends AbstractBaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "lead_month")
    private Integer leadMonth;

    @Column(name = "lead_year")
    private Integer leadYear;

    @Column(name = "month_viewings_requested")
    private Integer monthViewingsRequested;

    @Column(name = "month_viewings_attended")
    private Integer monthViewingsAttended;

    @Column(name = "month_spending", precision = 15, scale = 2)
    private BigDecimal monthSpending;

    @Column(name = "month_purchases", precision = 15, scale = 2)
    private BigDecimal monthPurchases;

    @Column(name = "month_rentals", precision = 15, scale = 2)
    private BigDecimal monthRentals;

    @Column(name = "month_contracts_signed")
    private Integer monthContractsSigned;

    @Column(name = "lead_score")
    private Integer leadScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "lead_classification")
    private Constants.LeadClassificationEnum leadClassification;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_tier")
    private Constants.CustomerTierEnum customerTier;
}
