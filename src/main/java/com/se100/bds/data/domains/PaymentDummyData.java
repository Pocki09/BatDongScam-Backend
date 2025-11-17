package com.se100.bds.data.domains;

import com.se100.bds.models.entities.contract.Contract;
import com.se100.bds.models.entities.contract.Payment;
import com.se100.bds.repositories.domains.contract.ContractRepository;
import com.se100.bds.repositories.domains.contract.PaymentRepository;
import com.se100.bds.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentDummyData {

    private final PaymentRepository paymentRepository;
    private final ContractRepository contractRepository;
    private final Random random = new Random();

    public void createDummy() {
        createDummyPayments();
    }

    private void createDummyPayments() {
        log.info("Creating dummy payments");

        List<Contract> contracts = contractRepository.findAll();
        if (contracts.isEmpty()) {
            log.warn("Cannot create payments - no contracts found");
            return;
        }

        List<Payment> payments = new ArrayList<>();

        for (Contract contract : contracts) {
            // Create deposit payment
            Payment deposit = Payment.builder()
                    .contract(contract)
                    .property(contract.getProperty())
                    .saleAgent(contract.getAgent())
                    .paymentType(Constants.PaymentTypeEnum.DEPOSIT)
                    .amount(contract.getDepositAmount())
                    .dueDate(contract.getStartDate())
                    .paidDate(contract.getStartDate())
                    .installmentNumber(null)
                    .paymentMethod("Bank Transfer")
                    .transactionReference(String.format("TXN%012d", random.nextInt(999999999)))
                    .status(Constants.PaymentStatusEnum.SUCCESS)
                    .penaltyAmount(BigDecimal.ZERO)
                    .notes("Initial deposit payment")
                    .build();
            payments.add(deposit);

            // Create installment or full payment based on contract type
            if (contract.getContractPaymentType() == Constants.ContractPaymentTypeEnum.MONTHLY_RENT) {
                // Create monthly rental payments
                int months = contract.getInstallmentAmount();
                for (int i = 0; i < months; i++) {
                    LocalDate dueDate = contract.getStartDate().plusMonths(i);
                    LocalDate paidDate = random.nextBoolean() ? dueDate.plusDays(random.nextInt(5)) : null;

                    Payment installment = Payment.builder()
                            .contract(contract)
                            .property(contract.getProperty())
                            .saleAgent(contract.getAgent())
                            .paymentType(Constants.PaymentTypeEnum.MONTHLY)
                            .amount(contract.getRemainingAmount().divide(BigDecimal.valueOf(months), 2, BigDecimal.ROUND_HALF_UP))
                            .dueDate(dueDate)
                            .paidDate(paidDate)
                            .installmentNumber(i + 1)
                            .paymentMethod(paidDate != null ? "Bank Transfer" : null)
                            .transactionReference(paidDate != null ? String.format("TXN%012d", random.nextInt(999999999)) : null)
                            .status(paidDate != null ? Constants.PaymentStatusEnum.SUCCESS : Constants.PaymentStatusEnum.PENDING)
                            .penaltyAmount(BigDecimal.ZERO)
                            .notes(String.format("Monthly payment %d/%d", i + 1, months))
                            .build();
                    payments.add(installment);
                }
            } else {
                // Create full payment
                Payment fullPay = Payment.builder()
                        .contract(contract)
                        .property(contract.getProperty())
                        .saleAgent(contract.getAgent())
                        .paymentType(Constants.PaymentTypeEnum.FULL_PAY)
                        .amount(contract.getRemainingAmount())
                        .dueDate(contract.getStartDate().plusDays(30))
                        .paidDate(random.nextBoolean() ? contract.getStartDate().plusDays(30) : null)
                        .installmentNumber(null)
                        .paymentMethod("Bank Transfer")
                        .transactionReference(String.format("TXN%012d", random.nextInt(999999999)))
                        .status(random.nextBoolean() ? Constants.PaymentStatusEnum.SUCCESS : Constants.PaymentStatusEnum.PENDING)
                        .penaltyAmount(BigDecimal.ZERO)
                        .notes("Full payment for property purchase")
                        .build();
                payments.add(fullPay);
            }
        }

        paymentRepository.saveAll(payments);
        log.info("Saved {} payments to database", payments.size());
    }
}

