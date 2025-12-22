package com.se100.bds.data.domains;

import com.se100.bds.models.entities.contract.Contract;
import com.se100.bds.models.entities.property.Property;
import com.se100.bds.models.entities.user.Customer;
import com.se100.bds.models.entities.user.SaleAgent;
import com.se100.bds.repositories.domains.contract.ContractRepository;
import com.se100.bds.repositories.domains.property.PropertyRepository;
import com.se100.bds.repositories.domains.user.CustomerRepository;
import com.se100.bds.repositories.domains.user.SaleAgentRepository;
import com.se100.bds.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
public class ContractDummyData {

    private final ContractRepository contractRepository;
    private final PropertyRepository propertyRepository;
    private final CustomerRepository customerRepository;
    private final SaleAgentRepository saleAgentRepository;

    private final Random random = new Random();

    public void createDummy() {
        createDummyContracts();
    }

    private void createDummyContracts() {
        log.info("Creating dummy contracts");

        List<Property> properties = propertyRepository.findAll();
        List<Customer> customers = customerRepository.findAll();
        List<SaleAgent> agents = saleAgentRepository.findAll();

        if (properties.isEmpty() || customers.isEmpty() || agents.isEmpty()) {
            log.warn("Cannot create contracts - missing required data");
            return;
        }

        List<Contract> contracts = new ArrayList<>();

        // Create 50 contracts
        for (int i = 1; i <= 50; i++) {
            Property property = properties.get(random.nextInt(properties.size()));
            Customer customer = customers.get(random.nextInt(customers.size()));
            SaleAgent agent = agents.get(random.nextInt(agents.size()));

            Constants.ContractTypeEnum contractType = property.getTransactionType() == Constants.TransactionTypeEnum.SALE
                    ? Constants.ContractTypeEnum.PURCHASE
                    : Constants.ContractTypeEnum.RENTAL;

            BigDecimal totalAmount = property.getPriceAmount();
            BigDecimal depositAmount = totalAmount.multiply(new BigDecimal("0.1")); // 10% deposit
            BigDecimal commissionAmount = totalAmount.multiply(property.getCommissionRate());
            BigDecimal serviceFeeAmount = totalAmount.multiply(new BigDecimal("0.01")); // 1% service fee

            LocalDate startDate = LocalDate.now().minusDays(random.nextInt(180));
            LocalDate endDate = contractType == Constants.ContractTypeEnum.RENTAL
                    ? startDate.plusYears(1)
                    : startDate.plusDays(90);

            Constants.ContractStatusEnum[] statuses = Constants.ContractStatusEnum.values();
            Constants.ContractStatusEnum status = statuses[random.nextInt(statuses.length)];

            Contract contract = Contract.builder()
                    .property(property)
                    .customer(customer)
                    .agent(agent)
                    .contractType(contractType)
                    .contractNumber(String.format("CT%06d%04d", LocalDate.now().getYear(), i))
                    .commissionAmount(commissionAmount)
                    .startDate(startDate)
                    .endDate(endDate)
                    .specialTerms("Standard terms and conditions apply")
                    .status(status)
                    .cancellationReason("")
                    .cancellationPenalty(BigDecimal.ZERO)
                    .contractPaymentType(contractType == Constants.ContractTypeEnum.RENTAL
                            ? Constants.ContractPaymentTypeEnum.MONTHLY_RENT
                            : Constants.ContractPaymentTypeEnum.PAID_IN_FULL)
                    .totalContractAmount(totalAmount)
                    .depositAmount(depositAmount)
                    .remainingAmount(totalAmount.subtract(depositAmount))
                    .advancePaymentAmount(BigDecimal.ZERO)
                    .installmentAmount(contractType == Constants.ContractTypeEnum.RENTAL ? 12 : 1)
                    .progressMilestone(BigDecimal.ZERO)
                    .finalPaymentAmount(totalAmount.subtract(depositAmount))
                    .latePaymentPenaltyRate(new BigDecimal("0.05"))
                    .specialConditions("No special conditions")
                    .signedAt(LocalDateTime.now().minusDays(random.nextInt(180)))
                    .completedAt(status == Constants.ContractStatusEnum.COMPLETED
                            ? LocalDateTime.now().minusDays(random.nextInt(90))
                            : LocalDateTime.now().plusDays(365))
                    .payments(new ArrayList<>())
                    .build();

            contracts.add(contract);
        }

        contractRepository.saveAll(contracts);
        log.info("Saved {} contracts to database", contracts.size());
    }
}

