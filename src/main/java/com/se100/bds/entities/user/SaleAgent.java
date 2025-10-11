package com.se100.bds.entities.user;

import com.se100.bds.entities.AbstractBaseEntity;
import com.se100.bds.entities.appointment.Appointment;
import com.se100.bds.entities.ranking.IndividualSalesAgentRanking;
import com.se100.bds.utils.Constants;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleAgent extends AbstractBaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "sale_agent_id", referencedColumnName = "user_id")
    private User user;

    @Column(name = "employee_code", nullable = false, unique = true)
    private String employeeCode;

    @Column(name = "max_properties", nullable = false)
    private int maxProperties;

    @Column(name = "hired_date", nullable = false)
    private LocalDateTime hiredDate;

    @Column(name = "current_month_revenue", nullable = false)
    private BigDecimal currentMonthRevenue;

    @Column(name = "total_revenue", nullable = false)
    private BigDecimal totalRevenue;

    @Column(name = "current_month_deals", nullable = false)
    private int currentMonthDeals;

    @Column(name = "total_deals", nullable = false)
    private int totalDeals;

    @Column(name = "active_properties", nullable = false)
    private int activeProperties;

    @Column(name = "performance_tier", nullable = false)
    private Constants.PerformanceTierEnum performanceTier;

    @Column(name = "current_month_ranking", nullable = false)
    private int currentMonthRanking;

    @Column(name = "career_ranking", nullable = false)
    private int careerRanking;

    @OneToMany(mappedBy = "assignedAgent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<com.se100.bds.entities.property.Property> assignedProperties = new ArrayList<>();

    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.se100.bds.entities.contract.Contract> contracts = new ArrayList<>();

    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IndividualSalesAgentRanking> rankings = new ArrayList<>();
}
