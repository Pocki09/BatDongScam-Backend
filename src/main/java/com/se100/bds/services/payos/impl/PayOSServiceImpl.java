package com.se100.bds.services.payos.impl;

import com.se100.bds.dtos.requests.payos.CreateContractPaymentRequest;
import com.se100.bds.models.entities.AbstractBaseEntity;
import com.se100.bds.models.entities.contract.Contract;
import com.se100.bds.models.entities.contract.Payment;
import com.se100.bds.models.entities.property.Property;
import com.se100.bds.models.entities.user.PropertyOwner;
import com.se100.bds.repositories.domains.contract.ContractRepository;
import com.se100.bds.repositories.domains.contract.PaymentRepository;
import com.se100.bds.repositories.domains.property.PropertyRepository;
import com.se100.bds.services.payos.PayOSService;
import com.se100.bds.services.payos.PayOSPayoutService;
import com.se100.bds.utils.Constants.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PayOSServiceImpl implements PayOSService {

    private final PayOS payOS;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final PropertyRepository propertyRepository;
    private final PayOSPayoutService payoutService;

    public PayOSServiceImpl(
            @Qualifier("payOSPaymentClient") final PayOS payOS,
            final ContractRepository contractRepository,
            final PaymentRepository paymentRepository,
            final PropertyRepository propertyRepository,
            final PayOSPayoutService payoutService
    ) {
        this.payOS = payOS;
        this.contractRepository = contractRepository;
        this.paymentRepository = paymentRepository;
        this.propertyRepository = propertyRepository;
        this.payoutService = payoutService;
    }

    @Override
    @Transactional
    public CreatePaymentLinkResponse createContractPaymentLink(UUID contractId, CreateContractPaymentRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

        BigDecimal amount = request.getAmount();
        PaymentTypeEnum paymentType = request.getPaymentType();
        Integer installmentNumber = request.getInstallmentNumber();
        String description = request.getDescription();
        String returnUrl = request.getReturnUrl();
        String cancelUrl = request.getCancelUrl();

        long orderCode = generateOrderCode();

        UUID payeeUserId = Optional.ofNullable(contract.getProperty())
                .map(Property::getOwner)
                .map(PropertyOwner::getUser)
                .map(AbstractBaseEntity::getId)
                .orElse(null);

        Payment payment = Payment.builder()
            .contract(contract)
            .paymentType(paymentType)
            .amount(amount)
            .dueDate(LocalDate.now())
            .installmentNumber(installmentNumber)
            .paymentMethod("PAYOS")
            .status(PaymentStatusEnum.PENDING)
            .notes(description)
            .payosOrderCode(orderCode)
            .payeeUserId(payeeUserId)
            .build();

        paymentRepository.save(payment);

        long amountVnd = amount.setScale(0, RoundingMode.HALF_UP).longValueExact();

        PaymentLinkItem item = PaymentLinkItem.builder()
            .name(description != null && !description.isBlank() ? description : "Contract payment")
            .quantity(1)
            .price(amountVnd)
            .build();

        CreatePaymentLinkRequest linkRequest = CreatePaymentLinkRequest.builder()
            .orderCode(orderCode)
            .amount(amountVnd)
            .description(description)
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
    public String confirmWebhook(String webhookPublicBaseUrl) {
        String webhookUrl = webhookPublicBaseUrl.endsWith("/")
                ? webhookPublicBaseUrl + "webhooks/payos"
                : webhookPublicBaseUrl + "/webhooks/payos";
        try {
            String result = payOS.webhooks().confirm(webhookUrl).getWebhookUrl();
            log.info("Confirmed PayOS webhook URL: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Failed to confirm webhook URL {}: {}", webhookUrl, e.getMessage(), e);
            throw new RuntimeException("Failed to confirm PayOS webhook URL", e);
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
    public CreatePaymentLinkResponse createPropertyListingPaymentLink(UUID propertyId, BigDecimal amountOverride, String description, String returnUrl, String cancelUrl) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));

        BigDecimal amount = amountOverride != null ? amountOverride : property.getServiceFeeAmount();
        if (amount == null) {
            throw new IllegalArgumentException("Service fee amount is not configured for property: " + propertyId);
        }

        long orderCode = generateOrderCode();

        Payment payment = Payment.builder()
            .property(property)
            .paymentType(PaymentTypeEnum.SERVICE_FEE)
            .amount(amount)
            .dueDate(LocalDate.now())
            .paymentMethod("PAYOS")
            .status(PaymentStatusEnum.PENDING)
            .notes(description)
            .payosOrderCode(orderCode)
            .build();
            paymentRepository.save(payment);

        long amountVnd = amount.setScale(0, RoundingMode.HALF_UP).longValueExact();
        String itemName = String.format("Listing fee - %s", property.getTitle());

        CreatePaymentLinkRequest linkRequest = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amountVnd)
                .description(description != null ? description : itemName)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .item(PaymentLinkItem.builder().name(itemName).quantity(1).price(amountVnd).build())
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
            case SALARY -> log.info("Salary payment {} registered as SUCCESS", payment.getId());
            default -> log.info("Payment {} completed with type {}", payment.getId(), payment.getPaymentType());
        }
    }

    private void activatePropertyListing(Payment payment) {
        if (payment.getProperty() == null) {
            log.warn("Service fee payment {} does not reference a property", payment.getId());
            return;
        }
        payment.getProperty().setStatus(PropertyStatusEnum.AVAILABLE);
        propertyRepository.save(payment.getProperty());
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

        payoutService.payoutToOwner(contract, payment, netToOwner);
        if (commission.signum() > 0) {
            payoutService.payoutToSaleAgent(contract, payment, commission);
        }
    }
}
