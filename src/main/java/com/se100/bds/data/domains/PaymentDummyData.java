package com.se100.bds.data.domains;

import com.se100.bds.data.util.TimeGenerator;
import com.se100.bds.models.entities.contract.Contract;
import com.se100.bds.models.entities.contract.Payment;
import com.se100.bds.repositories.domains.contract.ContractRepository;
import com.se100.bds.repositories.domains.contract.PaymentRepository;
import com.se100.bds.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final TimeGenerator timeGenerator = new TimeGenerator();

    public void createDummy() {
        createDummyPayments();
    }

    private void createDummyPayments() {
        log.info("Creating dummy payments");

        List<Contract> contracts = contractRepository.findAllWithCustomerAndUser();
        if (contracts.isEmpty()) {
            log.warn("Cannot create payments - no contracts found");
            return;
        }

        List<Payment> payments = new ArrayList<>();

        for (Contract contract : contracts) {
            // Create deposit payment
            LocalDateTime depositStartTime = contract.getSignedAt() != null ? contract.getSignedAt() : contract.getCreatedAt();
            if (depositStartTime == null || depositStartTime.isAfter(LocalDateTime.now())) {
                depositStartTime = LocalDateTime.now().minusDays(1);
            }
            LocalDateTime depositCreatedAt = timeGenerator.getRandomTimeAfter(depositStartTime, LocalDateTime.now());
            LocalDateTime depositUpdatedAt = timeGenerator.getRandomTimeAfter(depositCreatedAt, LocalDateTime.now());

            Payment deposit = Payment.builder()
                    .contract(contract)
                    .property(contract.getProperty())
                    .payer(contract.getCustomer().getUser())
                    .paymentType(Constants.PaymentTypeEnum.DEPOSIT)
                    .amount(contract.getDepositAmount())
                    .dueDate(contract.getStartDate())
                    .paidTime(contract.getStartDate().atTime(13, 20))
                    .installmentNumber(null)
                    .paymentMethod("Bank Transfer")
                    .transactionReference(String.format("TXN%012d", random.nextInt(999999999)))
                    .status(Constants.PaymentStatusEnum.SUCCESS)
                    .penaltyAmount(BigDecimal.ZERO)
                    .notes("Initial deposit payment")
                    .build();
            deposit.setCreatedAt(depositCreatedAt);
            deposit.setUpdatedAt(depositUpdatedAt);
            payments.add(deposit);

            // Create installment or full payment based on contract type
            if (contract.getContractPaymentType() == Constants.ContractPaymentTypeEnum.MONTHLY_RENT) {
                // Create monthly rental payments
                int months = contract.getInstallmentAmount();
                for (int i = 0; i < months; i++) {
                    LocalDate dueDate = contract.getStartDate().plusMonths(i);
                    LocalDateTime paidTime = random.nextBoolean() ? dueDate.plusDays(random.nextInt(5)).atTime(13, 20) : null;

                    LocalDateTime installmentStartTime = contract.getSignedAt() != null ? contract.getSignedAt() : contract.getCreatedAt();
                    if (installmentStartTime == null || installmentStartTime.isAfter(LocalDateTime.now())) {
                        installmentStartTime = LocalDateTime.now().minusDays(1);
                    }
                    LocalDateTime paymentCreatedAt = timeGenerator.getRandomTimeAfter(installmentStartTime, LocalDateTime.now());
                    LocalDateTime paymentUpdatedAt = paidTime != null ?
                            timeGenerator.getRandomTimeAfter(paymentCreatedAt, paidTime) :
                            timeGenerator.getRandomTimeAfter(paymentCreatedAt, LocalDateTime.now());

                    Payment installment = Payment.builder()
                            .contract(contract)
                            .property(contract.getProperty())
                            .paymentType(Constants.PaymentTypeEnum.MONTHLY)
                            .amount(contract.getRemainingAmount().divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP))
                            .dueDate(dueDate)
                            .paidTime(paidTime)
                            .installmentNumber(i + 1)
                            .paymentMethod(paidTime != null ? "PayOS Transfer" : null)
                            .transactionReference(paidTime != null ? String.format("TXN%012d", random.nextInt(999999999)) : null)
                            .status(paidTime != null ? Constants.PaymentStatusEnum.SUCCESS : Constants.PaymentStatusEnum.PENDING)
                            .penaltyAmount(BigDecimal.ZERO)
                            .notes(String.format("Monthly payment %d/%d", i + 1, months))
                            .build();
                    installment.setCreatedAt(paymentCreatedAt);
                    installment.setUpdatedAt(paymentUpdatedAt);
                    payments.add(installment);
                }
            } else {
                // Create full payment
                LocalDate fullPayDueDate = contract.getStartDate().plusDays(30);
                LocalDateTime fullPaidTime = random.nextBoolean() ? fullPayDueDate.atTime(13, 20) : null;

                LocalDateTime fullPayStartTime = contract.getSignedAt() != null ? contract.getSignedAt() : contract.getCreatedAt();
                if (fullPayStartTime == null || fullPayStartTime.isAfter(LocalDateTime.now())) {
                    fullPayStartTime = LocalDateTime.now().minusDays(1);
                }
                LocalDateTime fullPayCreatedAt = timeGenerator.getRandomTimeAfter(fullPayStartTime, LocalDateTime.now());
                LocalDateTime fullPayUpdatedAt = fullPaidTime != null ?
                        timeGenerator.getRandomTimeAfter(fullPayCreatedAt, fullPaidTime) :
                        timeGenerator.getRandomTimeAfter(fullPayCreatedAt, LocalDateTime.now());

                Payment fullPay = Payment.builder()
                        .contract(contract)
                        .property(contract.getProperty())
                        .paymentType(Constants.PaymentTypeEnum.FULL_PAY)
                        .amount(contract.getRemainingAmount())
                        .dueDate(fullPayDueDate)
                        .paidTime(fullPaidTime)
                        .installmentNumber(null)
                        .paymentMethod("Bank Transfer")
                        .transactionReference(String.format("TXN%012d", random.nextInt(999999999)))
                        .status(random.nextBoolean() ? Constants.PaymentStatusEnum.SUCCESS : Constants.PaymentStatusEnum.PENDING)
                        .penaltyAmount(BigDecimal.ZERO)
                        .notes("Full payment for property purchase")
                        .build();
                fullPay.setCreatedAt(fullPayCreatedAt);
                fullPay.setUpdatedAt(fullPayUpdatedAt);
                payments.add(fullPay);
            }
        }

        paymentRepository.saveAll(payments);
        log.info("Saved {} payments to database", payments.size());
    }
}
