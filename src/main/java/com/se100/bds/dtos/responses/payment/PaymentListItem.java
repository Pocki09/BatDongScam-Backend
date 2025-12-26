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
public class PaymentListItem {
    private UUID id;
    private LocalDateTime createdAt;
    
    private String paymentType;
    private String status;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDateTime paidTime;
    
    // Derived payer info (customer paying for contract, owner paying service fee)
    private UUID payerId;
    private String payerName;
    private String payerRole;
    
    // Derived payee info (company, owner, agent)
    private UUID payeeId;
    private String payeeName;
    private String payeeRole;
    
    // Context
    private UUID contractId;
    private String contractNumber;
    private UUID propertyId;
    private String propertyTitle;
}
