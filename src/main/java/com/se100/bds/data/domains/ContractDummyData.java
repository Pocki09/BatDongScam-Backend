package com.se100.bds.data.domains;

import com.se100.bds.data.util.TimeGenerator;
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
    private final TimeGenerator timeGenerator = new TimeGenerator();

    public void createDummy() {
        createDummyContracts();
    }

    private void createDummyContracts() {
        log.info("Creating dummy contracts");

        List<Contract> contracts = new ArrayList<>();

        // Create 50 contracts
        for (int i = 1; i <= 50; i++) {
            LocalDateTime createdAt = timeGenerator.getRandomTime();

            // Get list of properties with valid date
            List<Property> properties = propertyRepository.findAllByCreatedAtBefore(createdAt);
            if (properties.isEmpty()) {
                // Fallback to all properties if no properties found before createdAt
                properties = propertyRepository.findAll();
            }
            if (properties.isEmpty()) {
                log.warn("No properties available to create contracts, stopping at {} contracts", i - 1);
                break;
            }

            // Get list of customers with valid date
            List<Customer> customers = customerRepository.findAllByCreatedAtBefore(createdAt);
            if (customers.isEmpty()) {
                // Fallback to all customers if no customers found before createdAt
                customers = customerRepository.findAll();
            }
            if (customers.isEmpty()) {
                log.warn("No customers available to create contracts, stopping at {} contracts", i - 1);
                break;
            }

            // Get list of agents with valid date
            List<SaleAgent> agents = saleAgentRepository.findAllByCreatedAtBefore(createdAt);
            if (agents.isEmpty()) {
                // Fallback to all agents if no agents found before createdAt
                agents = saleAgentRepository.findAll();
            }
            if (agents.isEmpty()) {
                log.warn("No agents available to create contracts, stopping at {} contracts", i - 1);
                break;
            }

            Property property = properties.get(random.nextInt(properties.size()));
            Customer customer = customers.get(random.nextInt(customers.size()));
            SaleAgent agent = agents.get(random.nextInt(agents.size()));

            Constants.ContractTypeEnum contractType = property.getTransactionType() == Constants.TransactionTypeEnum.SALE
                    ? Constants.ContractTypeEnum.PURCHASE
                    : Constants.ContractTypeEnum.RENTAL;

            BigDecimal totalAmount = property.getPriceAmount();
            BigDecimal depositAmount = totalAmount.multiply(new BigDecimal("0.1")); // 10% deposit
            BigDecimal commissionAmount = totalAmount.multiply(property.getCommissionRate());

            LocalDateTime updatedAt = timeGenerator.getRandomTimeAfter(createdAt, LocalDateTime.now());

            LocalDate startDate = createdAt.toLocalDate().plusDays(random.nextInt(30));
            LocalDate endDate = contractType == Constants.ContractTypeEnum.RENTAL
                    ? startDate.plusYears(1)
                    : startDate.plusDays(90);

            Constants.ContractStatusEnum[] statuses = Constants.ContractStatusEnum.values();
            Constants.ContractStatusEnum status = statuses[random.nextInt(statuses.length)];

            LocalDateTime signedAt = timeGenerator.getRandomTimeAfter(updatedAt, LocalDateTime.now());
            LocalDateTime completedAt = status == Constants.ContractStatusEnum.COMPLETED
                    ? timeGenerator.getRandomTimeAfter(signedAt, LocalDateTime.now())
                    : null;

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
                    .signedAt(signedAt)
                    .completedAt(completedAt)
                    .payments(new ArrayList<>())
                    .build();

            contract.setCreatedAt(createdAt);
            contract.setUpdatedAt(updatedAt);

            contracts.add(contract);
        }

        contractRepository.saveAll(contracts);
        log.info("Saved {} contracts to database", contracts.size());
    }
}
