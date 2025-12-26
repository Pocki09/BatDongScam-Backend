package com.se100.bds.dtos.responses.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailResponse {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String paymentType;
    private String status;
    private BigDecimal amount;
    private BigDecimal penaltyAmount;
    private LocalDate dueDate;
    private LocalDateTime paidTime;
    private Integer installmentNumber;
    private String paymentMethod;
    private String transactionReference;
    private String notes;
    
    // Calculated fields
    private Long overdueDays;
    private Boolean penaltyApplied;
    
    // Payer info
    private UUID payerId;
    private String payerFirstName;
    private String payerLastName;
    private String payerRole;
    private String payerPhone;
    
    // Payee info
    private UUID payeeId;
    private String payeeFirstName;
    private String payeeLastName;
    private String payeeRole;
    private String payeePhone;
    
    // Contract context
    private UUID contractId;
    private String contractNumber;
    private String contractType;
    private String contractStatus;
    
    // Property context
    private UUID propertyId;
    private String propertyTitle;
    private String propertyAddress;
    
    // Agent context (for salary/bonus)
    private UUID agentId;
    private String agentFirstName;
    private String agentLastName;
    private String agentEmployeeCode;
}
