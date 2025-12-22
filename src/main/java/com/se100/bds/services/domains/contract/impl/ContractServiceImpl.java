package com.se100.bds.services.domains.contract.impl;

import com.se100.bds.dtos.requests.contract.CancelContractRequest;
import com.se100.bds.dtos.requests.contract.CreateContractRequest;
import com.se100.bds.dtos.requests.contract.UpdateContractRequest;
import com.se100.bds.dtos.responses.contract.ContractDetailResponse;
import com.se100.bds.dtos.responses.contract.ContractListItem;
import com.se100.bds.exceptions.NotFoundException;
import com.se100.bds.models.entities.contract.Contract;
import com.se100.bds.models.entities.contract.Payment;
import com.se100.bds.models.entities.property.Property;
import com.se100.bds.models.entities.user.Customer;
import com.se100.bds.models.entities.user.SaleAgent;
import com.se100.bds.models.entities.user.User;
import com.se100.bds.repositories.domains.contract.ContractRepository;
import com.se100.bds.repositories.domains.property.PropertyRepository;
import com.se100.bds.repositories.domains.user.CustomerRepository;
import com.se100.bds.repositories.domains.user.SaleAgentRepository;
import com.se100.bds.services.domains.contract.ContractService;
import com.se100.bds.services.domains.ranking.RankingService;
import com.se100.bds.services.domains.user.UserService;
import com.se100.bds.utils.Constants;
import com.se100.bds.utils.Constants.ContractStatusEnum;
import com.se100.bds.utils.Constants.ContractTypeEnum;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final PropertyRepository propertyRepository;
    private final CustomerRepository customerRepository;
    private final SaleAgentRepository saleAgentRepository;
    private final UserService userService;
    private final RankingService rankingService;

    private static final AtomicLong contractCounter = new AtomicLong(System.currentTimeMillis() % 100000);

    @Override
    @Transactional
    public ContractDetailResponse createContract(CreateContractRequest request) {
        // Validate entities exist
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new NotFoundException("Property not found: " + request.getPropertyId()));

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Customer not found: " + request.getCustomerId()));

        SaleAgent agent = saleAgentRepository.findById(request.getAgentId())
                .orElseThrow(() -> new NotFoundException("Agent not found: " + request.getAgentId()));

        // Generate contract number
        String contractNumber = generateContractNumber(request.getContractType());

        // Calculate financial fields
        BigDecimal remainingAmount = request.getTotalContractAmount()
                .subtract(request.getDepositAmount())
                .subtract(request.getAdvancePaymentAmount() != null ? request.getAdvancePaymentAmount() : BigDecimal.ZERO);

        BigDecimal finalPaymentAmount = remainingAmount;
        if (request.getInstallmentAmount() != null && request.getInstallmentAmount() > 0) {
            // For mortgages, final payment is the last installment
            finalPaymentAmount = remainingAmount.divide(
                    BigDecimal.valueOf(request.getInstallmentAmount()), 2, RoundingMode.HALF_UP);
        }

        Contract contract = Contract.builder()
                .property(property)
                .customer(customer)
                .agent(agent)
                .contractType(request.getContractType())
                .contractNumber(contractNumber)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .specialTerms(request.getSpecialTerms() != null ? request.getSpecialTerms() : "")
                .status(ContractStatusEnum.DRAFT)
                .contractPaymentType(request.getContractPaymentType())
                .totalContractAmount(request.getTotalContractAmount())
                .depositAmount(request.getDepositAmount())
                .remainingAmount(remainingAmount)
                .advancePaymentAmount(request.getAdvancePaymentAmount() != null ? request.getAdvancePaymentAmount() : BigDecimal.ZERO)
                .installmentAmount(request.getInstallmentAmount() != null ? request.getInstallmentAmount() : 0)
                .progressMilestone(request.getProgressMilestone() != null ? request.getProgressMilestone() : BigDecimal.ZERO)
                .finalPaymentAmount(finalPaymentAmount)
                .latePaymentPenaltyRate(request.getLatePaymentPenaltyRate())
                .specialConditions(request.getSpecialConditions() != null ? request.getSpecialConditions() : "")
                .signedAt(LocalDateTime.now()) // Will be updated on actual signing
                .completedAt(LocalDateTime.now()) // Will be updated on completion
                .build();

        Contract saved = contractRepository.save(contract);
        log.info("Created contract {} for property {} with customer {}", 
                saved.getContractNumber(), property.getId(), customer.getId());

        return mapToDetailResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDetailResponse getContractById(UUID contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found: " + contractId));
        return mapToDetailResponse(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContractListItem> getContracts(
            Pageable pageable,
            List<ContractTypeEnum> contractTypes,
            List<ContractStatusEnum> statuses,
            UUID customerId,
            UUID agentId,
            UUID propertyId,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            LocalDate endDateFrom,
            LocalDate endDateTo,
            String search
    ) {
        Specification<Contract> spec = buildContractSpecification(
                contractTypes, statuses, customerId, agentId, propertyId,
                startDateFrom, startDateTo, endDateFrom, endDateTo, search
        );

        Page<Contract> contracts = contractRepository.findAll(spec, pageable);
        return contracts.map(this::mapToListItem);
    }

    @Override
    @Transactional
    public ContractDetailResponse updateContract(UUID contractId, UpdateContractRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found: " + contractId));

        // Only allow updates for DRAFT or PENDING_SIGNING status
        if (contract.getStatus() != ContractStatusEnum.DRAFT && 
            contract.getStatus() != ContractStatusEnum.PENDING_SIGNING) {
            throw new IllegalStateException("Cannot update contract in status: " + contract.getStatus());
        }

        if (request.getEndDate() != null) {
            contract.setEndDate(request.getEndDate());
        }
        if (request.getSpecialTerms() != null) {
            contract.setSpecialTerms(request.getSpecialTerms());
        }
        if (request.getLatePaymentPenaltyRate() != null) {
            contract.setLatePaymentPenaltyRate(request.getLatePaymentPenaltyRate());
        }
        if (request.getSpecialConditions() != null) {
            contract.setSpecialConditions(request.getSpecialConditions());
        }
        if (request.getStatus() != null) {
            // Validate status transition
            validateStatusTransition(contract.getStatus(), request.getStatus());
            contract.setStatus(request.getStatus());
        }

        Contract saved = contractRepository.save(contract);
        log.info("Updated contract {}", saved.getContractNumber());

        return mapToDetailResponse(saved);
    }

    @Override
    @Transactional
    public ContractDetailResponse signContract(UUID contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found: " + contractId));

        if (contract.getStatus() != ContractStatusEnum.DRAFT && 
            contract.getStatus() != ContractStatusEnum.PENDING_SIGNING) {
            throw new IllegalStateException("Contract cannot be signed in status: " + contract.getStatus());
        }

        contract.setStatus(ContractStatusEnum.ACTIVE);
        contract.setSignedAt(LocalDateTime.now());

        Contract saved = contractRepository.save(contract);
        log.info("Contract {} signed", saved.getContractNumber());

        // Track ranking actions
        if (contract.getAgent() != null) {
            rankingService.agentAction(contract.getAgent().getId(), Constants.AgentActionEnum.CONTRACT_SIGNED, null);
        }
        if (contract.getCustomer() != null) {
            rankingService.customerAction(contract.getCustomer().getId(), Constants.CustomerActionEnum.CONTRACT_SIGNED, null);
        }

        return mapToDetailResponse(saved);
    }

    @Override
    @Transactional
    public ContractDetailResponse completeContract(UUID contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found: " + contractId));

        if (contract.getStatus() != ContractStatusEnum.ACTIVE) {
            throw new IllegalStateException("Contract cannot be completed in status: " + contract.getStatus());
        }

        // Check if all payments are complete
        boolean hasUnpaidPayments = contract.getPayments().stream()
                .anyMatch(p -> p.getStatus() != Constants.PaymentStatusEnum.SUCCESS && 
                              p.getStatus() != Constants.PaymentStatusEnum.SYSTEM_SUCCESS);
        if (hasUnpaidPayments) {
            throw new IllegalStateException("Cannot complete contract with unpaid payments");
        }

        contract.setStatus(ContractStatusEnum.COMPLETED);
        contract.setCompletedAt(LocalDateTime.now());

        Contract saved = contractRepository.save(contract);
        log.info("Contract {} completed", saved.getContractNumber());

        return mapToDetailResponse(saved);
    }

    @Override
    @Transactional
    public ContractDetailResponse cancelContract(UUID contractId, CancelContractRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found: " + contractId));

        User currentUser = userService.getUser();

        if (contract.getStatus() == ContractStatusEnum.CANCELLED) {
            throw new IllegalStateException("Contract is already cancelled");
        }
        if (contract.getStatus() == ContractStatusEnum.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed contract");
        }

        // TODO: Replace this temporary penalty estimation with the finalized business-approved formula.
        // Calculate penalty
        BigDecimal penalty = BigDecimal.ZERO;
        if (!Boolean.TRUE.equals(request.getWaivePenalty()) || 
            currentUser.getRole() != Constants.RoleEnum.ADMIN) {
            penalty = calculateCancellationPenalty(contractId);
        }

        contract.setStatus(ContractStatusEnum.CANCELLED);
        contract.setCancellationReason(request.getReason());
        contract.setCancellationPenalty(penalty);
        contract.setCancelledBy(currentUser.getRole());

        Contract saved = contractRepository.save(contract);
        log.info("Contract {} cancelled by {} with penalty {}", 
                saved.getContractNumber(), currentUser.getRole(), penalty);

        return mapToDetailResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateCancellationPenalty(UUID contractId) {
        // TODO: Align this penalty breakdown (deposit + percentage of remaining amount) with Finance once official policy is ready.
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found: " + contractId));

        if (contract.getStatus() == ContractStatusEnum.DRAFT) {
            return BigDecimal.ZERO; // No penalty for draft contracts
        }

        // Calculate based on contract type and progress
        BigDecimal totalAmount = contract.getTotalContractAmount();
        BigDecimal depositAmount = contract.getDepositAmount();
        
        // Base penalty: lose deposit
        BigDecimal penalty = depositAmount;

        // Additional penalty based on contract progress (placeholder tiers pending confirmation)
        if (contract.getStatus() == ContractStatusEnum.ACTIVE) {
            long totalDays = ChronoUnit.DAYS.between(contract.getStartDate(), contract.getEndDate());
            long elapsedDays = ChronoUnit.DAYS.between(contract.getStartDate(), LocalDate.now());
            
            if (elapsedDays > 0 && totalDays > 0) {
                double progressPercent = (double) elapsedDays / totalDays;
                
                // Additional penalty based on how far into the contract we are
                // Early cancellation (< 25%): 10% of remaining amount
                // Mid cancellation (25-50%): 20% of remaining amount
                // Late cancellation (> 50%): 30% of remaining amount
                BigDecimal additionalPenalty;
                if (progressPercent < 0.25) {
                    additionalPenalty = contract.getRemainingAmount().multiply(BigDecimal.valueOf(0.10));
                } else if (progressPercent < 0.50) {
                    additionalPenalty = contract.getRemainingAmount().multiply(BigDecimal.valueOf(0.20));
                } else {
                    additionalPenalty = contract.getRemainingAmount().multiply(BigDecimal.valueOf(0.30));
                }
                
                penalty = penalty.add(additionalPenalty).setScale(2, RoundingMode.HALF_UP);
            }
        }

        return penalty;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContractListItem> getMyContracts(Pageable pageable, List<ContractStatusEnum> statuses) {
        User currentUser = userService.getUser();
        
        Specification<Contract> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("customer").get("id"), currentUser.getId()));
            
            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Contract> contracts = contractRepository.findAll(spec, pageable);
        return contracts.map(this::mapToListItem);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContractListItem> getMyAgentContracts(Pageable pageable, List<ContractStatusEnum> statuses) {
        User currentUser = userService.getUser();
        
        Specification<Contract> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("agent").get("id"), currentUser.getId()));
            
            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Contract> contracts = contractRepository.findAll(spec, pageable);
        return contracts.map(this::mapToListItem);
    }

    @Override
    @Transactional
    public ContractDetailResponse rateContract(UUID contractId, Short rating, String comment) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contract not found: " + contractId));

        if (contract.getStatus() != ContractStatusEnum.COMPLETED) {
            throw new IllegalStateException("Can only rate completed contracts");
        }

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        contract.setRating(rating);
        contract.setComment(comment);

        Contract saved = contractRepository.save(contract);
        log.info("Contract {} rated with {}", saved.getContractNumber(), rating);

        // Track rating for agent
        if (contract.getAgent() != null) {
            rankingService.agentAction(contract.getAgent().getId(), Constants.AgentActionEnum.RATED, BigDecimal.valueOf(rating));
        }

        return mapToDetailResponse(saved);
    }

    private String generateContractNumber(ContractTypeEnum type) {
        String prefix = switch (type) {
            case PURCHASE -> "PUR";
            case RENTAL -> "RNT";
        };
        
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(5);
        String counter = String.format("%05d", contractCounter.incrementAndGet() % 100000);
        
        return prefix + "-" + timestamp + "-" + counter;
    }

    private void validateStatusTransition(ContractStatusEnum from, ContractStatusEnum to) {
        // Valid transitions:
        // DRAFT -> PENDING_SIGNING, CANCELLED
        // PENDING_SIGNING -> ACTIVE, CANCELLED
        // ACTIVE -> COMPLETED, CANCELLED
        // COMPLETED -> (none)
        // CANCELLED -> (none)

        boolean valid = switch (from) {
            case DRAFT -> to == ContractStatusEnum.PENDING_SIGNING || to == ContractStatusEnum.CANCELLED;
            case PENDING_SIGNING -> to == ContractStatusEnum.ACTIVE || to == ContractStatusEnum.CANCELLED;
            case ACTIVE -> to == ContractStatusEnum.COMPLETED || to == ContractStatusEnum.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };

        if (!valid) {
            throw new IllegalStateException("Invalid status transition from " + from + " to " + to);
        }
    }

    private Specification<Contract> buildContractSpecification(
            List<ContractTypeEnum> contractTypes,
            List<ContractStatusEnum> statuses,
            UUID customerId,
            UUID agentId,
            UUID propertyId,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            LocalDate endDateFrom,
            LocalDate endDateTo,
            String search
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (contractTypes != null && !contractTypes.isEmpty()) {
                predicates.add(root.get("contractType").in(contractTypes));
            }
            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), customerId));
            }
            if (agentId != null) {
                predicates.add(cb.equal(root.get("agent").get("id"), agentId));
            }
            if (propertyId != null) {
                predicates.add(cb.equal(root.get("property").get("id"), propertyId));
            }
            if (startDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDateFrom));
            }
            if (startDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), startDateTo));
            }
            if (endDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("endDate"), endDateFrom));
            }
            if (endDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), endDateTo));
            }
            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate searchByNumber = cb.like(cb.lower(root.get("contractNumber")), searchPattern);
                Predicate searchByPropertyTitle = cb.like(cb.lower(root.get("property").get("title")), searchPattern);
                predicates.add(cb.or(searchByNumber, searchByPropertyTitle));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private ContractListItem mapToListItem(Contract contract) {
        ContractListItem.ContractListItemBuilder builder = ContractListItem.builder()
                .id(contract.getId())
                .contractNumber(contract.getContractNumber())
                .contractType(contract.getContractType() != null ? contract.getContractType().name() : null)
                .status(contract.getStatus() != null ? contract.getStatus().name() : null)
                .totalContractAmount(contract.getTotalContractAmount())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .signedAt(contract.getSignedAt())
                .createdAt(contract.getCreatedAt());

        // Property info
        Property property = contract.getProperty();
        if (property != null) {
            builder.propertyId(property.getId())
                   .propertyTitle(property.getTitle())
                   .propertyAddress(property.getFullAddress());
        }

        // Customer info
        Customer customer = contract.getCustomer();
        if (customer != null && customer.getUser() != null) {
            builder.customerId(customer.getId())
                   .customerFirstName(customer.getUser().getFirstName())
                   .customerLastName(customer.getUser().getLastName());
        }

        // Agent info
        SaleAgent agent = contract.getAgent();
        if (agent != null) {
            builder.agentId(agent.getId())
                   .agentEmployeeCode(agent.getEmployeeCode());
            if (agent.getUser() != null) {
                builder.agentFirstName(agent.getUser().getFirstName())
                       .agentLastName(agent.getUser().getLastName());
            }
        }

        return builder.build();
    }

    // TODO: split to mapper class later
    private ContractDetailResponse mapToDetailResponse(Contract contract) {
        ContractDetailResponse.ContractDetailResponseBuilder builder = ContractDetailResponse.builder()
                .id(contract.getId())
                .contractNumber(contract.getContractNumber())
                .contractType(contract.getContractType() != null ? contract.getContractType().name() : null)
                .status(contract.getStatus() != null ? contract.getStatus().name() : null)
                .contractPaymentType(contract.getContractPaymentType() != null ? contract.getContractPaymentType().name() : null)
                .totalContractAmount(contract.getTotalContractAmount())
                .depositAmount(contract.getDepositAmount())
                .remainingAmount(contract.getRemainingAmount())
                .advancePaymentAmount(contract.getAdvancePaymentAmount())
                .installmentAmount(contract.getInstallmentAmount())
                .progressMilestone(contract.getProgressMilestone())
                .finalPaymentAmount(contract.getFinalPaymentAmount())
                .latePaymentPenaltyRate(contract.getLatePaymentPenaltyRate())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .signedAt(contract.getSignedAt())
                .completedAt(contract.getCompletedAt())
                .specialTerms(contract.getSpecialTerms())
                .specialConditions(contract.getSpecialConditions())
                .cancellationReason(contract.getCancellationReason())
                .cancellationPenalty(contract.getCancellationPenalty())
                .cancelledBy(contract.getCancelledBy() != null ? contract.getCancelledBy().name() : null)
                .rating(contract.getRating())
                .comment(contract.getComment())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt());

        // Property info
        Property property = contract.getProperty();
        if (property != null) {
            builder.propertyId(property.getId())
                   .propertyTitle(property.getTitle())
                   .propertyAddress(property.getFullAddress())
                   .propertyPrice(property.getPriceAmount())
                   .propertyType(property.getPropertyType() != null ? property.getPropertyType().getTypeName() : null)
                   .propertyTransactionType(property.getTransactionType() != null ? property.getTransactionType().name() : null);

            // Owner info
            if (property.getOwner() != null && property.getOwner().getUser() != null) {
                User ownerUser = property.getOwner().getUser();
                builder.ownerId(property.getOwner().getId())
                       .ownerFirstName(ownerUser.getFirstName())
                       .ownerLastName(ownerUser.getLastName())
                       .ownerPhone(ownerUser.getPhoneNumber());
            }
        }

        // Customer info
        Customer customer = contract.getCustomer();
        if (customer != null && customer.getUser() != null) {
            User customerUser = customer.getUser();
            builder.customerId(customer.getId())
                   .customerFirstName(customerUser.getFirstName())
                   .customerLastName(customerUser.getLastName())
                   .customerPhone(customerUser.getPhoneNumber())
                   .customerEmail(customerUser.getEmail());
        }

        // Agent info
        SaleAgent agent = contract.getAgent();
        if (agent != null) {
            builder.agentId(agent.getId())
                   .agentEmployeeCode(agent.getEmployeeCode());
            if (agent.getUser() != null) {
                builder.agentFirstName(agent.getUser().getFirstName())
                       .agentLastName(agent.getUser().getLastName())
                       .agentPhone(agent.getUser().getPhoneNumber());
            }
        }

        // Payment summary
        List<Payment> payments = contract.getPayments();
        if (payments != null && !payments.isEmpty()) {
            BigDecimal totalPaid = payments.stream()
                    .filter(p -> p.getStatus() == Constants.PaymentStatusEnum.SUCCESS || 
                                p.getStatus() == Constants.PaymentStatusEnum.SYSTEM_SUCCESS)
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<ContractDetailResponse.PaymentSummary> paymentSummaries = payments.stream()
                    .map(p -> ContractDetailResponse.PaymentSummary.builder()
                            .id(p.getId())
                            .paymentType(p.getPaymentType() != null ? p.getPaymentType().name() : null)
                            .status(p.getStatus() != null ? p.getStatus().name() : null)
                            .amount(p.getAmount())
                            .dueDate(p.getDueDate())
                            .paidDate(p.getPaidDate())
                            .installmentNumber(p.getInstallmentNumber())
                            .build())
                    .collect(Collectors.toList());

            builder.totalPaymentsMade(totalPaid)
                   .paymentCount(payments.size())
                   .payments(paymentSummaries);
        } else {
            builder.totalPaymentsMade(BigDecimal.ZERO)
                   .paymentCount(0)
                   .payments(List.of());
        }

        return builder.build();
    }
}
