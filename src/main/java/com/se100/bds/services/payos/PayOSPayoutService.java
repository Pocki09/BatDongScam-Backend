package com.se100.bds.services.payos;

import com.se100.bds.models.entities.contract.Contract;
import com.se100.bds.models.entities.contract.Payment;
import vn.payos.model.v1.payouts.Payout;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PayOSPayoutService {
    void payoutToOwner(Contract contract, Payment payment, BigDecimal netAmount);
    void payoutToSaleAgent(Contract contract, Payment payment, BigDecimal commissionAmount);
    Payout createSalaryPayoutForAgent(UUID saleAgentId, String referenceId, BigDecimal amount, String description, List<String> categories);
}