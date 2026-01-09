package com.se100.bds.models.entities.contract;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_contract")
@DiscriminatorValue("PURCHASE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseContract extends Contract {

    /// Reference to the deposit contract that preceded this purchase contract
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deposit_contract_id")
    private DepositContract depositContract;

    /// The property value / purchase price
    @Column(name = "property_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal propertyValue;

    /// Down payment / advance payment amount
    @Column(name = "advance_payment_amount", precision = 15, scale = 2)
    private BigDecimal advancePaymentAmount;

    /// Remaining amount to be paid
    @Column(name = "remaining_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingAmount;

    /// Commission amount for this purchase contract
    @Column(name = "commission_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal commissionAmount;

    /// Late payment penalty rate (e.g., 0.05 for 5% per month)
    @Column(name = "late_payment_penalty_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal latePaymentPenaltyRate;
}

