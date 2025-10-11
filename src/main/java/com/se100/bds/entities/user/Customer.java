package com.se100.bds.entities.user;

import com.se100.bds.entities.AbstractBaseEntity;
import com.se100.bds.entities.appointment.Appointment;
import com.se100.bds.entities.contract.Contract;
import com.se100.bds.entities.search.SearchLog;
import com.se100.bds.utils.Constants;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends AbstractBaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "customer_id", referencedColumnName = "user_id")
    private User user;

    @Column(name = "current_month_spending", nullable = false)
    private BigDecimal currentMonthSpending;

    @Column(name = "total_spending", nullable = false)
    private BigDecimal totalSpending;

    @Column(name = "current_month_purchases", nullable = false)
    private int currentMonthPurchases;

    @Column(name = "total_purchases", nullable = false)
    private int totalPurchases;

    @Column(name = "current_month_rentals", nullable = false)
    private int currentMonthRentals;

    @Column(name = "total_rentals", nullable = false)
    private int totalRentals;

    @Column(name = "current_month_searches", nullable = false)
    private int currentMonthSearches;

    @Column(name = "current_month_viewings", nullable = false)
    private int currentMonthViewings;

    @Column(name = "customer_tier", nullable = false)
    private Constants.CustomerTierEnum customerTier;

    @Column(name = "lead_score", nullable = false)
    private int leadScore;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Contract> contracts = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SearchLog> searchLogs = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.se100.bds.entities.ranking.IndividualCustomerLead> customerLeads = new ArrayList<>();
}
