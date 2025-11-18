package com.se100.bds.services.payos.impl;

import com.se100.bds.dtos.requests.payos.CreateContractPaymentRequest;
import com.se100.bds.models.entities.contract.Contract;
import com.se100.bds.models.entities.contract.Payment;
import com.se100.bds.models.entities.property.Property;
import com.se100.bds.models.entities.user.User;
import com.se100.bds.repositories.domains.contract.ContractRepository;
import com.se100.bds.repositories.domains.contract.PaymentRepository;
import com.se100.bds.repositories.domains.property.PropertyRepository;
import com.se100.bds.services.domains.user.UserService;
import com.se100.bds.services.payos.PayOSService;
import com.se100.bds.utils.Constants.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.v2.paymentRequests.PaymentLinkStatus;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PayOSServiceImpl implements PayOSService {

    private static final String PAYOS_CHECKOUT_BASE_URL = "https://pay.payos.vn/web/";

    private final PayOS payOS;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final PropertyRepository propertyRepository;
//    private final ContractService contractService;
    private final UserService userService;

    @Value("${payos.return-url}")
    private String defaultReturnUrl;

    @Value("${payos.cancel-url}")
    private String defaultCancelUrl;

    public PayOSServiceImpl(
            @Qualifier("payOSPaymentClient") final PayOS payOS,
            final ContractRepository contractRepository,
            final PaymentRepository paymentRepository,
            final PropertyRepository propertyRepository,
//            final ContractService contractService,
            final UserService userService
    ) {
        this.payOS = payOS;
        this.contractRepository = contractRepository;
        this.paymentRepository = paymentRepository;
        this.propertyRepository = propertyRepository;
//        this.contractService = contractService;
        this.userService = userService;
    }

    @Override
    @Transactional
    public CreatePaymentLinkResponse createContractPaymentLink(UUID contractId, CreateContractPaymentRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

        authorizeContractPaymentRequest(contract);

        PaymentTypeEnum paymentType = request.getPaymentType() != null ? request.getPaymentType() : PaymentTypeEnum.FULL_PAY;
        Integer installmentNumber = request.getInstallmentNumber();
        BigDecimal amount = resolveContractPaymentAmount(contract, paymentType, installmentNumber);
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Unable to resolve payable amount for contract " + contractId + " and payment type " + paymentType);
        }

        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);

        Optional<CreatePaymentLinkResponse> reusable = reusePendingContractPaymentLink(contract, paymentType, installmentNumber, normalizedAmount);
        if (reusable.isPresent()) {
            return reusable.get();
        }

        String description = request.getDescription();
        String returnUrl = request.getReturnUrl() != null ? request.getReturnUrl() : defaultReturnUrl;
        String cancelUrl = request.getCancelUrl() != null ? request.getCancelUrl() : defaultCancelUrl;

        long orderCode = generateOrderCode();

        Payment payment = Payment.builder()
            .contract(contract)
            .property(contract.getProperty())
            .saleAgent(contract.getAgent())
            .paymentType(paymentType)
            .amount(normalizedAmount)
            .dueDate(LocalDate.now())
            .installmentNumber(installmentNumber)
            .paymentMethod("PAYOS")
            .status(PaymentStatusEnum.PENDING)
            .notes(description != null ? description : String.format("%s payment", paymentType.name()))
            .payosOrderCode(orderCode)
            .build();

        paymentRepository.save(payment);

        long amountVnd = normalizedAmount.setScale(0, RoundingMode.HALF_UP).longValueExact();

        String shortDescription = buildContractPaymentDescription(paymentType, contract);

        PaymentLinkItem item = PaymentLinkItem.builder()
            .name(shortDescription)
            .quantity(1)
            .price(amountVnd)
            .build();

        CreatePaymentLinkRequest linkRequest = CreatePaymentLinkRequest.builder()
            .orderCode(orderCode)
            .amount(amountVnd)
            .description(shortDescription)
            .returnUrl(returnUrl)
            .cancelUrl(cancelUrl)
            .item(item)
            .build();

        try {
            CreatePaymentLinkResponse resp = payOS.paymentRequests().create(linkRequest);
            log.info("Created PayOS payment link for contract {} payment {} orderCode {} checkoutUrl {}",
                    contractId, payment.getId(), orderCode, resp.getCheckoutUrl());
            return resp;
        } catch (PayOSException e) {
            log.error("Failed to create PayOS payment link: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create PayOS payment link", e);
        }
    }

    @Override
    @Transactional
    public void handlePaymentWebhook(String rawBody) {
        WebhookData data = payOS.webhooks().verify(rawBody);

        long orderCode = data.getOrderCode();
        Payment payment = paymentRepository.findByPayosOrderCode(orderCode).orElse(null);

        if (payment == null) {
            log.warn("Received PayOS webhook for unknown orderCode {}", orderCode);
            return;
        }

        if (payment.getStatus() == PaymentStatusEnum.SUCCESS) {
            log.info("Ignoring duplicate webhook for payment {} already SUCCESS", payment.getId());
            return;
        }

        PaymentStatusEnum newStatus = "00".equals(data.getCode())
                ? PaymentStatusEnum.SUCCESS
                : PaymentStatusEnum.FAILED;

        payment.setStatus(newStatus);
        payment.setPaidDate(LocalDate.now());
        payment.setTransactionReference(data.getReference());
        paymentRepository.save(payment);

        if (newStatus == PaymentStatusEnum.SUCCESS) {
            handleSuccessfulPayment(payment);
        }
    }

    @Override
    @Transactional
    public CreatePaymentLinkResponse createPropertyServicePaymentLink(UUID propertyId, String description, String returnUrl, String cancelUrl) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));

        authorizePropertyServiceFeePaymentRequest(property);

        PropertyStatusEnum propertyStatus = property.getStatus();
        if (propertyStatus == PropertyStatusEnum.AVAILABLE) {
            throw new IllegalArgumentException("This property listing is already active; no service fee is due");
        }
        if (propertyStatus != PropertyStatusEnum.APPROVED) {
            throw new IllegalArgumentException("Property must be approved before paying the service fee");
        }

        BigDecimal outstanding = calculateOutstandingServiceFee(property);
        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Service fee already settled for this property");
        }

        BigDecimal normalizedAmount = outstanding.setScale(2, RoundingMode.HALF_UP);

        returnUrl = returnUrl != null ? returnUrl : defaultReturnUrl;
        cancelUrl = cancelUrl != null ? cancelUrl : defaultCancelUrl;

        String shortDescription = buildPropertyListingDescription(description, property);

        Optional<CreatePaymentLinkResponse> reusable = reusePendingPropertyServicePaymentLink(
                property,
                normalizedAmount,
                shortDescription,
                description
        );
        if (reusable.isPresent()) {
            return reusable.get();
        }

        long orderCode = generateOrderCode();

        Payment payment = Payment.builder()
            .property(property)
            .paymentType(PaymentTypeEnum.SERVICE_FEE)
            .amount(normalizedAmount)
            .dueDate(LocalDate.now())
            .paymentMethod("PAYOS")
            .status(PaymentStatusEnum.PENDING)
            .notes(description != null ? description : shortDescription)
            .payosOrderCode(orderCode)
            .build();
        paymentRepository.save(payment);

        long amountVnd = normalizedAmount.setScale(0, RoundingMode.HALF_UP).longValueExact();

        CreatePaymentLinkRequest linkRequest = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amountVnd)
                .description(shortDescription)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .item(PaymentLinkItem.builder().name(shortDescription).quantity(1).price(amountVnd).build())
                .build();

        try {
            CreatePaymentLinkResponse resp = payOS.paymentRequests().create(linkRequest);
            log.info("Created PayOS listing fee link for property {} payment {} orderCode {} checkoutUrl {}",
                    propertyId, payment.getId(), orderCode, resp.getCheckoutUrl());
            return resp;
        } catch (PayOSException e) {
            log.error("Failed to create PayOS listing fee link: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create PayOS listing fee link", e);
        }
    }

    private BigDecimal calculateOutstandingServiceFee(Property property) {
        BigDecimal serviceFee = Optional.ofNullable(property.getServiceFeeAmount()).orElse(BigDecimal.ZERO);
        BigDecimal collected = Optional.ofNullable(property.getServiceFeeCollectedAmount()).orElse(BigDecimal.ZERO);
        BigDecimal outstanding = serviceFee.subtract(collected);
        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return outstanding;
    }

    private void authorizeContractPaymentRequest(Contract contract) {
        User currentUser = userService.getUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication is required to create a contract payment");
        }

        RoleEnum role = currentUser.getRole();
        if (role == RoleEnum.ADMIN) {
            return;
        }

        UUID currentUserId = currentUser.getId();

        if (contract.getCustomer() != null && currentUserId.equals(contract.getCustomer().getId())) {
            return;
        }

        if (contract.getAgent() != null && currentUserId.equals(contract.getAgent().getId())) {
            return;
        }

        Property property = contract.getProperty();
        if (property != null && property.getOwner() != null) {
            UUID ownerId = property.getOwner().getId();
            if (ownerId == null && property.getOwner().getUser() != null) {
                ownerId = property.getOwner().getUser().getId();
            }
            if (ownerId != null && ownerId.equals(currentUserId)) {
                return;
            }
        }

        throw new AccessDeniedException("You do not have permission to create a PayOS checkout for this contract");
    }

    private Optional<CreatePaymentLinkResponse> reusePendingContractPaymentLink(
            Contract contract,
            PaymentTypeEnum paymentType,
            Integer installmentNumber,
            BigDecimal normalizedAmount
    ) {
        Optional<Payment> candidateOpt = paymentRepository
            .findFirstByContract_IdAndPaymentTypeOrderByCreatedAtDesc(contract.getId(), paymentType);

        if (candidateOpt.isEmpty()) {
            return Optional.empty();
        }

        Payment candidate = candidateOpt.get();
        if (candidate.getStatus() != PaymentStatusEnum.PENDING) {
            return Optional.empty();
        }

        if (!matchesInstallment(candidate, installmentNumber)) {
            return Optional.empty();
        }

        if (candidate.getAmount() == null || candidate.getAmount().compareTo(normalizedAmount) != 0) {
            candidate.setStatus(PaymentStatusEnum.FAILED);
            paymentRepository.save(candidate);
            return Optional.empty();
        }

        if (candidate.getPayosOrderCode() == null) {
            return Optional.empty();
        }

        Optional<PaymentLink> remoteOpt = fetchPaymentLink(candidate.getPayosOrderCode());
        PaymentLink remote = remoteOpt.orElse(null);

        if (remote != null && remote.getStatus() == PaymentLinkStatus.PAID) {
            candidate.setStatus(PaymentStatusEnum.SUCCESS);
            paymentRepository.save(candidate);
            throw new IllegalStateException("This contract payment has already been settled");
        }

        if (remote == null) {
            return Optional.empty();
        }

        CreatePaymentLinkResponse response = buildResponseFromPayOS(candidate.getPayosOrderCode(), candidate.getAmount(), buildContractPaymentDescription(candidate.getPaymentType(), contract), remote);
        return Optional.of(response);
    }

    private Optional<CreatePaymentLinkResponse> reusePendingPropertyServicePaymentLink(
            Property property,
            BigDecimal normalizedAmount,
            String shortDescription,
            String notes
    ) {
        Optional<Payment> candidateOpt = paymentRepository
                .findFirstByProperty_IdAndPaymentTypeOrderByCreatedAtDesc(property.getId(), PaymentTypeEnum.SERVICE_FEE);

        if (candidateOpt.isEmpty()) {
            return Optional.empty();
        }

        Payment candidate = candidateOpt.get();

        if (candidate.getStatus() == PaymentStatusEnum.SUCCESS) {
            if (normalizedAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Service fee already settled for this property");
            }
            log.info("Property {} has additional outstanding service fee after prior settlement; creating a new payment entry", property.getId());
            return Optional.empty();
        }

        if (candidate.getStatus() != PaymentStatusEnum.PENDING) {
            return Optional.empty();
        }

        if (candidate.getAmount() == null || candidate.getAmount().compareTo(normalizedAmount) != 0) {
            return Optional.empty();
        }

        if (candidate.getPayosOrderCode() == null) {
            return Optional.empty();
        }

        Optional<PaymentLink> remoteOpt = fetchPaymentLink(candidate.getPayosOrderCode());
        if (remoteOpt.isEmpty()) {
            return Optional.empty();
        }

        PaymentLink remote = remoteOpt.get();
        if (remote.getStatus() == PaymentLinkStatus.PAID) {
            candidate.setStatus(PaymentStatusEnum.SUCCESS);
            candidate.setPaidDate(LocalDate.now());
            paymentRepository.save(candidate);
            handleSuccessfulPayment(candidate);
            if (normalizedAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Service fee already settled for this property");
            }
            log.info("Property {} has outstanding service fee after prior settlement; issuing fresh payment link", property.getId());
            return Optional.empty();
        }

        PaymentLinkStatus remoteStatus = remote.getStatus();
        if (remoteStatus != PaymentLinkStatus.PENDING && remoteStatus != PaymentLinkStatus.PROCESSING) {
            candidate.setStatus(PaymentStatusEnum.FAILED);
            paymentRepository.save(candidate);
            return Optional.empty();
        }

        boolean notesUpdated = notes != null && !notes.isBlank() && !notes.equals(candidate.getNotes());
        if (notesUpdated) {
            candidate.setNotes(notes);
            paymentRepository.save(candidate);
        }

        return Optional.of(buildResponseFromPayOS(candidate.getPayosOrderCode(), candidate.getAmount(), shortDescription, remote));
    }

    private CreatePaymentLinkResponse buildResponseFromPayOS(Long orderCode, BigDecimal amount, String description, PaymentLink remoteLink) {
        PaymentLinkStatus status = remoteLink != null ? remoteLink.getStatus() : PaymentLinkStatus.PENDING;
        String paymentLinkId = remoteLink != null ? remoteLink.getId() : null;
        String checkoutUrl = paymentLinkId != null ? PAYOS_CHECKOUT_BASE_URL + paymentLinkId : null;

        long amountVnd = amount.setScale(0, RoundingMode.HALF_UP).longValueExact();

        CreatePaymentLinkResponse response = new CreatePaymentLinkResponse();
        response.setAmount(amountVnd);
        response.setDescription(description);
        response.setOrderCode(orderCode);
        response.setPaymentLinkId(paymentLinkId);
        response.setStatus(status);
        response.setCheckoutUrl(checkoutUrl);
        return response;
    }

    private boolean matchesInstallment(Payment payment, Integer installmentNumber) {
        if (installmentNumber == null) {
            return payment.getInstallmentNumber() == null;
        }
        return installmentNumber.equals(payment.getInstallmentNumber());
    }

    private String buildContractPaymentDescription(PaymentTypeEnum paymentType, Contract contract) {
        String base = paymentType.name() + " " + contract.getContractNumber();
        return base.length() > 25 ? base.substring(0, 25) : base;
    }

    private String buildPropertyListingDescription(String description, Property property) {
        String itemName = String.format("Service fee - %s", property.getTitle());
        String descriptor = (description != null && !description.isBlank()) ? description : itemName;
        return descriptor.length() > 25 ? descriptor.substring(0, 25) : descriptor;
    }

    private String buildCancellationRefundDescription(Contract contract) {
        String base = String.format("Refund %s", contract.getContractNumber());
        return base.length() > 25 ? base.substring(0, 25) : base;
    }

    private Optional<PaymentLink> fetchPaymentLink(Long orderCode) {
        try {
            return Optional.ofNullable(payOS.paymentRequests().get(orderCode));
        } catch (Exception ex) {
            log.warn("Unable to fetch PayOS link for orderCode {}: {}", orderCode, ex.getMessage());
            return Optional.empty();
        }
    }

    // Only property owners or admins can generate service fee payment links.
    private void authorizePropertyServiceFeePaymentRequest(Property property) {
        User currentUser = userService.getUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Authentication is required to generate a service fee payment");
        }

        if (currentUser.getRole() == RoleEnum.ADMIN) {
            return;
        }

        UUID ownerUserId = Optional.ofNullable(property.getOwner())
            .map(owner -> owner.getId() != null ? owner.getId() : (owner.getUser() != null ? owner.getUser().getId() : null))
                    .orElse(null);

        if (ownerUserId == null || !ownerUserId.equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to create a service fee payment for this property");
        }
    }

//    @Override
//    @Transactional
//    public CreatePaymentLinkResponse createCancellationPenaltyPaymentLink(UUID contractId, UUID penaltyPaymentId) {
//        if (penaltyPaymentId == null) {
//            throw new IllegalArgumentException("Penalty payment id is required");
//        }
//
//        Contract contract = contractRepository.findById(contractId)
//                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));
//
//        Payment penaltyPayment = paymentRepository.findById(penaltyPaymentId)
//                .orElseThrow(() -> new IllegalArgumentException("Penalty payment not found: " + penaltyPaymentId));
//
//        if (penaltyPayment.getPaymentType() != PaymentTypeEnum.PENALTY) {
//            throw new IllegalArgumentException("Payment " + penaltyPaymentId + " is not a penalty payment");
//        }
//        if (penaltyPayment.getStatus() == PaymentStatusEnum.SUCCESS) {
//            log.info("Penalty payment {} already settled for contract {}", penaltyPaymentId, contractId);
//            return null;
//        }
//
//        BigDecimal amount = penaltyPayment.getAmount();
//        if (amount == null || amount.signum() <= 0) {
//            throw new IllegalStateException("Penalty amount must be positive for contract " + contractId);
//        }
//
//        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);
//
//        long orderCode = penaltyPayment.getPayosOrderCode() != null
//                ? penaltyPayment.getPayosOrderCode()
//                : generateOrderCode();
//
//        penaltyPayment.setPayosOrderCode(orderCode);
//        penaltyPayment.setStatus(PaymentStatusEnum.PENDING);
//        penaltyPayment.setPaymentMethod("PAYOS");
//        penaltyPayment.setNotes(String.format("Cancellation penalty for %s", contract.getContractNumber()));
//        penaltyPayment.setAmount(normalizedAmount);
//        paymentRepository.save(penaltyPayment);
//
//        String shortDescription = String.format("Penalty %s", contract.getContractNumber());
//        if (shortDescription.length() > 25) {
//            shortDescription = shortDescription.substring(0, 25);
//        }
//
//        long amountVnd = normalizedAmount.setScale(0, RoundingMode.HALF_UP).longValueExact();
//
//        CreatePaymentLinkRequest linkRequest = CreatePaymentLinkRequest.builder()
//                .orderCode(orderCode)
//                .amount(amountVnd)
//                .description(shortDescription)
//                .returnUrl(defaultReturnUrl)
//                .cancelUrl(defaultCancelUrl)
//                .item(PaymentLinkItem.builder().name(shortDescription).quantity(1).price(amountVnd).build())
//                .build();
//
//        try {
//            CreatePaymentLinkResponse resp = payOS.paymentRequests().create(linkRequest);
//            log.info("Created PayOS penalty link for contract {} payment {} orderCode {} checkoutUrl {}",
//                    contractId, penaltyPayment.getId(), orderCode, resp.getCheckoutUrl());
//            return resp;
//        } catch (PayOSException e) {
//            log.error("Failed to create penalty payment link for contract {}: {}", contractId, e.getMessage(), e);
//            throw new RuntimeException("Failed to create PayOS penalty payment link", e);
//        }
//    }

    @Override
    @Transactional
    public CreatePaymentLinkResponse createCancellationRefundCollectionLink(UUID contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

//        if (contract.getStatus() != ContractStatusEnum.CANCELLATION_PENDING) {
//            throw new IllegalStateException("Contract " + contractId + " is not awaiting cancellation settlement");
//        }

        Payment refundSettlement = paymentRepository
                .findFirstByContract_IdAndPaymentTypeOrderByCreatedAtDesc(contractId, PaymentTypeEnum.REFUND)
                .orElseThrow(() -> new IllegalStateException("No pending cancellation refund settlement found for contract " + contractId));

        if (refundSettlement.getStatus() == PaymentStatusEnum.SUCCESS) {
            throw new IllegalStateException("Cancellation refund settlement already completed for contract " + contractId);
        }

        BigDecimal amount = refundSettlement.getAmount();
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalStateException("Refund settlement amount must be positive for contract " + contractId);
        }

        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);
        String description = buildCancellationRefundDescription(contract);

        if (refundSettlement.getPayosOrderCode() != null) {
            Optional<PaymentLink> remoteLink = fetchPaymentLink(refundSettlement.getPayosOrderCode());
            if (remoteLink.isPresent()) {
                PaymentLink link = remoteLink.get();
                if (link.getStatus() == PaymentLinkStatus.PAID) {
                    throw new IllegalStateException("Cancellation refund settlement already paid for contract " + contractId);
                }
                return buildResponseFromPayOS(refundSettlement.getPayosOrderCode(), normalizedAmount, description, link);
            }
        }

        long orderCode = refundSettlement.getPayosOrderCode() != null
                ? refundSettlement.getPayosOrderCode()
                : generateOrderCode();

        refundSettlement.setPayosOrderCode(orderCode);
        refundSettlement.setStatus(PaymentStatusEnum.PENDING);
        refundSettlement.setPaymentMethod("PAYOS");
        refundSettlement.setDueDate(LocalDate.now());
        if (refundSettlement.getNotes() == null || refundSettlement.getNotes().isBlank()) {
            refundSettlement.setNotes(description);
        }
        paymentRepository.save(refundSettlement);

        long amountVnd = normalizedAmount.setScale(0, RoundingMode.HALF_UP).longValueExact();

        CreatePaymentLinkRequest linkRequest = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amountVnd)
                .description(description)
                .returnUrl(defaultReturnUrl)
                .cancelUrl(defaultCancelUrl)
                .item(PaymentLinkItem.builder().name(description).quantity(1).price(amountVnd).build())
                .build();

        try {
            CreatePaymentLinkResponse resp = payOS.paymentRequests().create(linkRequest);
            log.info("Created PayOS cancellation refund link for contract {} payment {} orderCode {} checkoutUrl {}",
                    contractId, refundSettlement.getId(), orderCode, resp.getCheckoutUrl());
            return resp;
        } catch (PayOSException e) {
            log.error("Failed to create cancellation refund link for contract {}: {}", contractId, e.getMessage(), e);
            throw new RuntimeException("Failed to create PayOS cancellation refund link", e);
        }
    }

    private BigDecimal resolveContractPaymentAmount(Contract contract, PaymentTypeEnum paymentType, Integer installmentNumber) {
        PaymentTypeEnum effectiveType = paymentType != null ? paymentType : PaymentTypeEnum.FULL_PAY;
        BigDecimal amount;
        switch (effectiveType) {
            case DEPOSIT:
                amount = contract.getDepositAmount();
                break;
            case ADVANCE:
                amount = contract.getAdvancePaymentAmount();
                break;
            case FULL_PAY:
            case MONEY_SALE:
            case MONEY_RENTAL:
                amount = Optional.ofNullable(contract.getRemainingAmount())
                        .orElse(contract.getTotalContractAmount());
                break;
            case INSTALLMENT:
            case MONTHLY:
                if (contract.getInstallmentAmount() != null) {
                    amount = BigDecimal.valueOf(contract.getInstallmentAmount());
                } else if (contract.getProgressMilestone() != null) {
                    amount = contract.getProgressMilestone();
                } else {
                    amount = contract.getRemainingAmount();
                }
                break;
            case PENALTY:
            case REFUND:
            case SERVICE_FEE:
            case SALARY:
            default:
                amount = contract.getRemainingAmount();
                break;
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalStateException("Contract " + contract.getId() + " does not have a configured amount for payment type " + effectiveType);
        }

        return amount;
    }

    private long generateOrderCode() {
        long orderCode = System.currentTimeMillis() / 1000L;
        for (int i = 0; i < 5; i++) {
            Optional<Payment> existing = paymentRepository.findByPayosOrderCode(orderCode);
            if (existing.isEmpty()) {
                return orderCode;
            }
            orderCode++;
        }
        // Fallback: keep incrementing until unique (should be rare)
        while (paymentRepository.findByPayosOrderCode(orderCode).isPresent()) {
            orderCode++;
        }
        return orderCode;
    }

    private void handleSuccessfulPayment(Payment payment) {
        switch (payment.getPaymentType()) {
            case SERVICE_FEE -> {
                log.info("Service fee payment {} completed", payment.getId());
                activatePropertyListing(payment);
            }
            case MONEY_SALE, MONEY_RENTAL -> distributeContractPayment(payment);
            case PENALTY -> {
                log.info("Penalty payment {} completed; finalizing contract cancellation", payment.getId());
//                contractService.finalizeCancellationAfterPenalty(payment);
            }
            case REFUND -> {
                log.info("Cancellation refund payment {} completed; finalizing contract settlement", payment.getId());
//                contractService.finalizeCancellationAfterRefundCollection(payment);
            }
            case SALARY -> log.info("Salary payment {} registered as SUCCESS", payment.getId());
            default -> log.info("Payment {} completed with type {}", payment.getId(), payment.getPaymentType());
        }
    }

    private void activatePropertyListing(Payment payment) {
        if (payment.getProperty() == null) {
            log.warn("Service fee payment {} does not reference a property", payment.getId());
            return;
        }
        Property property = payment.getProperty();
        PropertyStatusEnum currentStatus = property.getStatus();

        BigDecimal paymentAmount = Optional.ofNullable(payment.getAmount()).orElse(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal collected = Optional.ofNullable(property.getServiceFeeCollectedAmount()).orElse(BigDecimal.ZERO);
        BigDecimal updatedCollected = collected.add(paymentAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal serviceFee = Optional.ofNullable(property.getServiceFeeAmount()).orElse(BigDecimal.ZERO);

        if (updatedCollected.compareTo(serviceFee) > 0) {
            updatedCollected = serviceFee;
        }

        property.setServiceFeeCollectedAmount(updatedCollected);

        boolean outstanding = serviceFee.compareTo(updatedCollected) > 0;

        if (!outstanding) {
            if (currentStatus != PropertyStatusEnum.AVAILABLE) {
                if (property.getApprovedAt() == null) {
                    property.setApprovedAt(LocalDateTime.now());
                }
                property.setStatus(PropertyStatusEnum.AVAILABLE);
            }
        } else if (currentStatus == PropertyStatusEnum.AVAILABLE) {
            property.setStatus(PropertyStatusEnum.APPROVED);
        } else if (currentStatus != PropertyStatusEnum.APPROVED) {
            log.warn("Property {} is in status {} during service fee settlement; retaining status", property.getId(), currentStatus);
        }

        propertyRepository.save(property);
    }

    private void distributeContractPayment(Payment payment) {
        Contract contract = payment.getContract();
        if (contract == null) {
            log.warn("Payment {} is not linked to a contract; skipping payout.", payment.getId());
            return;
        }

        Property property = contract.getProperty();
        BigDecimal commissionRate = property.getCommissionRate();
        BigDecimal serviceFee = Optional.ofNullable(property.getServiceFeeAmount()).orElse(BigDecimal.ZERO);

        BigDecimal commission = payment.getAmount()
                .multiply(commissionRate != null ? commissionRate : BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal netToOwner = payment.getAmount().subtract(commission).subtract(serviceFee);
        if (netToOwner.signum() < 0) {
            netToOwner = BigDecimal.ZERO;
        }

        log.info("Distributing payment {}. amount={}, commission={}, serviceFee={}, netToOwner={}",
                payment.getId(), payment.getAmount(), commission, serviceFee, netToOwner);

    }
}
