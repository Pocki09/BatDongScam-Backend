package com.se100.bds.models.entities.contract;

import com.se100.bds.models.entities.AbstractBaseEntity;
import com.se100.bds.models.entities.property.Property;
import com.se100.bds.models.entities.user.Customer;
import com.se100.bds.models.entities.user.SaleAgent;
import com.se100.bds.utils.Constants;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contract")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "contract_id", nullable = false)),
})
public class Contract extends AbstractBaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private SaleAgent agent;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false)
    private Constants.ContractTypeEnum contractType;

    @Column(name = "contract_number", nullable = false, unique = true, length = 50)
    private String contractNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "special_terms", nullable = false, columnDefinition = "TEXT")
    private String specialTerms;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Constants.ContractStatusEnum status;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "cancellation_penalty", precision = 15, scale = 2)
    private BigDecimal cancellationPenalty;

    @Column(name = "cancelled_by")
    private Constants.RoleEnum cancelledBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_payment_type", nullable = false)
    private Constants.ContractPaymentTypeEnum contractPaymentType;

    @Column(name = "total_contract_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalContractAmount;

    @Column(name = "commission_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "deposit_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "remaining_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingAmount;

    @Column(name = "advance_payment_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal advancePaymentAmount;

    @Column(name = "installment_amount", nullable = false)
    private Integer installmentAmount;

    @Column(name = "progress_milestone", nullable = false, precision = 15, scale = 2)
    private BigDecimal progressMilestone;

    @Column(name = "final_payment_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal finalPaymentAmount;

    @Column(name = "late_payment_penalty_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal latePaymentPenaltyRate;

    @Column(name = "special_conditions", nullable = false, columnDefinition = "TEXT")
    private String specialConditions;

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @Column(name = "rating")
    private Short rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
}
