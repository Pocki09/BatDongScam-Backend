package com.se100.bds.services.payos.impl;

import com.se100.bds.models.entities.contract.Contract;
import com.se100.bds.models.entities.contract.Payment;
import com.se100.bds.models.entities.property.Property;
import com.se100.bds.models.entities.user.PropertyOwner;
import com.se100.bds.models.entities.user.SaleAgent;
import com.se100.bds.models.entities.user.User;
import com.se100.bds.repositories.domains.contract.PaymentRepository;
import com.se100.bds.repositories.domains.user.SaleAgentRepository;
import com.se100.bds.services.payos.PayOSPayoutService;
import com.se100.bds.utils.Constants.PaymentStatusEnum;
import com.se100.bds.utils.Constants.PaymentTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.payos.PayOS;
import vn.payos.exception.PayOSException;
import vn.payos.model.v1.payouts.Payout;
import vn.payos.model.v1.payouts.PayoutRequests;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PayOSPayoutServiceImpl implements PayOSPayoutService {
    private static final List<String> OWNER_PAYOUT_CATEGORY = List.of("owner");
    private static final List<String> AGENT_COMMISSION_CATEGORY = List.of("commission");
    private static final List<String> SALARY_CATEGORY = List.of("salary");

    private final PayOS payOS;
    private final SaleAgentRepository saleAgentRepository;
    private final PaymentRepository paymentRepository;

    public PayOSPayoutServiceImpl(
            @Qualifier("payOSPayoutClient") final PayOS payOS,
            final SaleAgentRepository saleAgentRepository,
            final PaymentRepository paymentRepository
    ) {
        this.payOS = payOS;
        this.saleAgentRepository = saleAgentRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public void payoutToOwner(Contract contract, Payment payment, BigDecimal netAmount) {
        if (netAmount == null || netAmount.signum() <= 0) {
            log.info("Skipping owner payout for payment {} because net amount is {}", payment.getId(), netAmount);
            return;
        }

        Property property = contract.getProperty();
        if (property == null) {
            log.warn("Owner payout skipped for payment {} because contract {} has no property", payment.getId(), contract.getId());
            return;
        }

        PropertyOwner propertyOwner = property.getOwner();
        if (propertyOwner == null) {
            log.warn("Owner payout skipped for payment {} because property {} has no owner record", payment.getId(), property.getId());
            return;
        }

        BankAccount ownerAccount = resolveBankAccount(propertyOwner.getUser());
        if (ownerAccount == null) {
            log.warn("Owner payout skipped for payment {} due to missing bank information", payment.getId());
            return;
        }

        String description = String.format("Owner payout for contract %s", contract.getContractNumber());
        String referenceId = String.format("owner_%s", payment.getId());
        PayoutRequests request = buildPayoutRequest(
                referenceId,
                netAmount,
                description,
                ownerAccount.bankBin(),
                ownerAccount.accountNumber(),
                OWNER_PAYOUT_CATEGORY
        );

        executePayout(request, description, ownerAccount.accountName());
    }

    @Override
    @Transactional
    public void payoutToSaleAgent(Contract contract, Payment payment, BigDecimal commissionAmount) {
        if (commissionAmount == null || commissionAmount.signum() <= 0) {
            log.info("Skipping agent payout for payment {} because commission amount is {}", payment.getId(), commissionAmount);
            return;
        }

        SaleAgent agent = contract.getAgent();
        if (agent == null) {
            log.warn("Agent payout skipped for payment {} because contract {} has no assigned agent", payment.getId(), contract.getId());
            return;
        }

        BankAccount agentAccount = resolveBankAccount(agent.getUser());
        if (agentAccount == null) {
            log.warn("Agent payout skipped for payment {} due to missing bank information", payment.getId());
            return;
        }

        String description = String.format("Commission payout for agent %s", contract.getAgent().getId());
        String referenceId = String.format("agent_%s", payment.getId());
        PayoutRequests request = buildPayoutRequest(
                referenceId,
                commissionAmount,
                description,
                agentAccount.bankBin(),
                agentAccount.accountNumber(),
                AGENT_COMMISSION_CATEGORY
        );

        executePayout(request, description, agentAccount.accountName());
    }

    @Override
    @Transactional
    public Payout createSalaryPayoutForAgent(UUID saleAgentId, String referenceId, BigDecimal amount, String description, List<String> categories) {
        SaleAgent saleAgent = saleAgentRepository.findById(saleAgentId)
                .orElseThrow(() -> new IllegalArgumentException("Sale agent not found: " + saleAgentId));

        BankAccount agentAccount = resolveBankAccount(saleAgent.getUser());
        if (agentAccount == null) {
            throw new IllegalStateException("Sale agent does not have bank information configured");
        }

        String effectiveReferenceId = StringUtils.hasText(referenceId)
                ? referenceId
                : String.format("salary_%s_%d", saleAgentId, System.currentTimeMillis() / 1000);
        
        List<String> effectiveCategories = (categories == null || categories.isEmpty()) ? SALARY_CATEGORY : categories;
        PayoutRequests request = buildPayoutRequest(
                effectiveReferenceId,
                amount,
                description,
                agentAccount.bankBin(),
                agentAccount.accountNumber(),
                effectiveCategories
        );

        Payout payout = executePayout(request, description, agentAccount.accountName());

        Payment salaryPayment = Payment.builder()
                .paymentType(PaymentTypeEnum.SALARY)
                .amount(amount)
                .dueDate(LocalDate.now())
                .paidDate(LocalDate.now())
                .paymentMethod("PAYOS")
                .status(PaymentStatusEnum.SUCCESS)
                .notes(description)
                .transactionReference(payout.getReferenceId())
                .payeeUserId(saleAgent.getUser().getId())
                .build();
        paymentRepository.save(salaryPayment);

        return payout;
    }

    private PayoutRequests buildPayoutRequest(String referenceId, BigDecimal amount, String description, String bankBin, String accountNumber, List<String> categories) {
        long amountVnd = amount.setScale(0, RoundingMode.HALF_UP).longValueExact();
        List<String> safeCategories = (categories == null || categories.isEmpty())
                ? Collections.emptyList()
                : categories;
        return PayoutRequests.builder()
                .referenceId(referenceId)
                .amount(amountVnd)
                .description(description)
                .toBin(bankBin)
                .toAccountNumber(accountNumber)
                .category(safeCategories.isEmpty() ? null : safeCategories)
                .build();
    }

    private Payout executePayout(PayoutRequests request, String description, String accountName) {
        try {
            Payout payout = payOS.payouts().create(request);
            log.info("Triggered payout {} to account {} description {} status {}", payout.getReferenceId(), accountName, description, payout.getApprovalState());
            return payout;
        } catch (PayOSException ex) {
            log.error("Failed to execute payout {}: {}", request.getReferenceId(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to execute PayOS payout", ex);
        }
    }

    private BankAccount resolveBankAccount(User user) {
        if (user == null) {
            return null;
        }
        if (!StringUtils.hasText(user.getBankBin())
                || !StringUtils.hasText(user.getBankAccountNumber())
                || !StringUtils.hasText(user.getBankAccountName())) {
            return null;
        }
        return new BankAccount(user.getBankBin(), user.getBankAccountNumber(), user.getBankAccountName());
    }

    private record BankAccount(String bankBin, String accountNumber, String accountName) { }
}
