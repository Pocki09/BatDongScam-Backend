package com.se100.bds.services.domains.payment.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se100.bds.dtos.requests.payment.CreateBonusPaymentRequest;
import com.se100.bds.dtos.requests.payment.CreateSalaryPaymentRequest;
import com.se100.bds.dtos.requests.payment.UpdatePaymentStatusRequest;
import com.se100.bds.dtos.responses.payment.PaymentDetailResponse;
import com.se100.bds.dtos.responses.payment.PaymentListItem;
import com.se100.bds.exceptions.NotFoundException;
import com.se100.bds.models.entities.contract.Contract;
import com.se100.bds.models.entities.contract.Payment;
import com.se100.bds.models.entities.property.Property;
import com.se100.bds.models.entities.user.SaleAgent;
import com.se100.bds.models.entities.user.User;
import com.se100.bds.repositories.domains.contract.PaymentRepository;
import com.se100.bds.repositories.domains.user.SaleAgentRepository;
import com.se100.bds.services.payment.payway.PaywayWebhookHandler;
import com.se100.bds.services.payment.payway.dto.PaywayWebhookEvent;
import com.se100.bds.services.domains.payment.PaymentService;
import com.se100.bds.utils.Constants.PaymentStatusEnum;
import com.se100.bds.utils.Constants.PaymentTypeEnum;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final SaleAgentRepository saleAgentRepository;
    private final PaywayWebhookHandler paywayWebhookHandler;
    private final ObjectMapper objectMapper;

    private static final String COMPANY_NAME = "Company";
    private static final String COMPANY_ROLE = "COMPANY";
    private static final String PAYOS_METHOD = "PAYOS";
    private static final String OWNER_PAYOUT_METHOD = "OWNER_PAYOUT";
    private static final String COMPANY_PAYOUT_METHOD = "COMPANY_PAYOUT";

    private static final EnumSet<PaymentTypeEnum> CUSTOMER_TO_COMPANY_TYPES = EnumSet.of(
            PaymentTypeEnum.DEPOSIT,
            PaymentTypeEnum.ADVANCE,
            PaymentTypeEnum.INSTALLMENT,
            PaymentTypeEnum.FULL_PAY,
            PaymentTypeEnum.MONTHLY,
            PaymentTypeEnum.MONEY_SALE,
            PaymentTypeEnum.MONEY_RENTAL
    );

    private record PartyInfo(UUID id, String firstName, String lastName, String role, String phone) {
        String displayName() {
            String first = firstName != null ? firstName.trim() : "";
            String last = lastName != null ? lastName.trim() : "";
            String combined = (first + " " + last).trim();
            if (!combined.isEmpty()) {
                return combined;
            }
            return role;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentListItem> getPayments(
            Pageable pageable,
            List<PaymentTypeEnum> paymentTypes,
            List<PaymentStatusEnum> statuses,
            UUID payerId,
            UUID payeeId,
            UUID contractId,
            UUID propertyId,
            UUID agentId,
            LocalDate dueDateFrom,
            LocalDate dueDateTo,
            LocalDate paidDateFrom,
            LocalDate paidDateTo,
            Boolean overdue
    ) {
        Specification<Payment> spec = buildPaymentSpecification(
                paymentTypes, statuses, payerId, payeeId, contractId, propertyId, agentId,
                dueDateFrom, dueDateTo, paidDateFrom, paidDateTo, overdue
        );

        Page<Payment> payments = paymentRepository.findAll(spec, pageable);
        return payments.map(this::mapToListItem);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentById(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));
        return mapToDetailResponse(payment);
    }

    @Override
    @Transactional
    public PaymentDetailResponse updatePaymentStatus(UUID paymentId, UpdatePaymentStatusRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));

        PaymentStatusEnum oldStatus = payment.getStatus();
        payment.setStatus(request.getStatus());

        if (request.getNotes() != null) {
            payment.setNotes(request.getNotes());
        }
        if (request.getTransactionReference() != null) {
            payment.setTransactionReference(request.getTransactionReference());
        }

        // Set paid date if marking as success
        if (request.getStatus() == PaymentStatusEnum.SUCCESS || request.getStatus() == PaymentStatusEnum.SYSTEM_SUCCESS) {
            if (payment.getPaidDate() == null) {
                payment.setPaidDate(LocalDate.now());
            }
        }

        Payment saved = paymentRepository.save(payment);
        log.info("Updated payment {} status from {} to {}", paymentId, oldStatus, request.getStatus());

        return mapToDetailResponse(saved);
    }

    @Override
    @Transactional
    public PaymentDetailResponse createSalaryPayment(CreateSalaryPaymentRequest request) {
        // TODO: Replace manual salary input with calculated values driven by financial performance metrics.
        SaleAgent agent = saleAgentRepository.findById(request.getAgentId())
                .orElseThrow(() -> new NotFoundException("Sales agent not found: " + request.getAgentId()));

        LocalDate dueDate = request.getDueDate() != null ? request.getDueDate() : LocalDate.now();

        Payment payment = Payment.builder()
            .saleAgent(agent)
            .paymentType(PaymentTypeEnum.SALARY)
            .amount(request.getAmount())
            .dueDate(dueDate)
            .paidDate(LocalDate.now())
            .paymentMethod(COMPANY_PAYOUT_METHOD)
            .status(PaymentStatusEnum.SYSTEM_SUCCESS)
            .notes(request.getNotes())
            .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Created salary payment {} for agent {}", saved.getId(), request.getAgentId());

        return mapToDetailResponse(saved);
    }

    @Override
    @Transactional
    public PaymentDetailResponse createBonusPayment(CreateBonusPaymentRequest request) {
        // TODO: Drive bonus amount from agreed KPI formulas instead of raw request payloads.
        SaleAgent agent = saleAgentRepository.findById(request.getAgentId())
                .orElseThrow(() -> new NotFoundException("Sales agent not found: " + request.getAgentId()));

        Payment payment = Payment.builder()
            .saleAgent(agent)
            .paymentType(PaymentTypeEnum.BONUS)
            .amount(request.getAmount())
            .dueDate(LocalDate.now())
            .paidDate(LocalDate.now())
            .paymentMethod(COMPANY_PAYOUT_METHOD)
            .status(PaymentStatusEnum.SYSTEM_SUCCESS)
            .notes(request.getNotes())
            .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Created bonus payment {} for agent {}", saved.getId(), request.getAgentId());

        return mapToDetailResponse(saved);
    }

    @Override
    public void handlePaywayWebhook(String rawBody) {
        // Route by event.type, since Payway sends multiple event shapes.
        try {
            PaywayWebhookEvent<?> envelope = objectMapper.readValue(
                    rawBody,
                    objectMapper.getTypeFactory().constructType(PaywayWebhookEvent.class)
            );

            String type = envelope != null ? envelope.getType() : null;
            if (type != null && type.startsWith("payout.")) {
                paywayWebhookHandler.handlePayoutEvent(rawBody);
                return;
            }
        } catch (Exception e) {
            // Best-effort: if we can't parse the envelope, fall back to payment handler.
            log.warn("Payway webhook: unable to parse envelope, falling back to payment handler", e);
        }

        paywayWebhookHandler.handlePaymentEvent(rawBody);
    }

    private Specification<Payment> buildPaymentSpecification(
            List<PaymentTypeEnum> paymentTypes,
            List<PaymentStatusEnum> statuses,
            UUID payerId,
            UUID payeeId,
            UUID contractId,
            UUID propertyId,
            UUID agentId,
            LocalDate dueDateFrom,
            LocalDate dueDateTo,
            LocalDate paidDateFrom,
            LocalDate paidDateTo,
            Boolean overdue
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (paymentTypes != null && !paymentTypes.isEmpty()) {
                predicates.add(root.get("paymentType").in(paymentTypes));
            }
            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }
            if (contractId != null) {
                predicates.add(cb.equal(root.get("contract").get("id"), contractId));
            }
            if (propertyId != null) {
                predicates.add(root.get("property").get("id").in(propertyId));
            }
            if (agentId != null) {
                predicates.add(cb.equal(root.get("saleAgent").get("id"), agentId));
            }
            if (dueDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), dueDateFrom));
            }
            if (dueDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), dueDateTo));
            }
            if (paidDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("paidDate"), paidDateFrom));
            }
            if (paidDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("paidDate"), paidDateTo));
            }
            if (Boolean.TRUE.equals(overdue)) {
                predicates.add(cb.lessThan(root.get("dueDate"), LocalDate.now()));
                predicates.add(cb.isNull(root.get("paidDate")));
                predicates.add(root.get("status").in(PaymentStatusEnum.PENDING, PaymentStatusEnum.SYSTEM_PENDING));
            }

            // Payer filter - derived from contract customer or property owner
            if (payerId != null) {
                Predicate contractCustomer = cb.equal(root.get("contract").get("customer").get("id"), payerId);
                Predicate propertyOwner = cb.equal(root.get("property").get("owner").get("id"), payerId);
                predicates.add(cb.or(contractCustomer, propertyOwner));
            }

            // Payee filter - typically agent for salary/bonus, or could be owner for refunds
            if (payeeId != null) {
                Predicate agentPayee = cb.equal(root.get("saleAgent").get("id"), payeeId);
                Predicate ownerPayee = cb.equal(root.get("property").get("owner").get("id"), payeeId);
                predicates.add(cb.or(agentPayee, ownerPayee));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private PaymentListItem mapToListItem(Payment payment) {
        PaymentListItem.PaymentListItemBuilder builder = PaymentListItem.builder()
                .id(payment.getId())
                .createdAt(payment.getCreatedAt())
                .paymentType(payment.getPaymentType() != null ? payment.getPaymentType().name() : null)
                .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                .amount(payment.getAmount())
                .dueDate(payment.getDueDate())
                .paidDate(payment.getPaidDate());

        // Derive payer/payee based on payment type and relations
        derivePayerPayee(payment, builder);

        // Contract context
        Contract contract = payment.getContract();
        if (contract != null) {
            builder.contractId(contract.getId())
                   .contractNumber(contract.getContractNumber());
        }

        // Property context
        Property property = payment.getProperty();
        if (property == null && contract != null) {
            property = contract.getProperty();
        }
        if (property != null) {
            builder.propertyId(property.getId())
                   .propertyTitle(property.getTitle());
        }

        return builder.build();
    }

    private void derivePayerPayee(Payment payment, PaymentListItem.PaymentListItemBuilder builder) {
        PartyInfo payer = resolvePayer(payment);
        PartyInfo payee = resolvePayee(payment);

        applyPartyInfoToList(builder, payer, true);
        applyPartyInfoToList(builder, payee, false);
    }

    private PaymentDetailResponse mapToDetailResponse(Payment payment) {
        PaymentDetailResponse.PaymentDetailResponseBuilder builder = PaymentDetailResponse.builder()
                .id(payment.getId())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .paymentType(payment.getPaymentType() != null ? payment.getPaymentType().name() : null)
                .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                .amount(payment.getAmount())
                .penaltyAmount(payment.getPenaltyAmount())
                .dueDate(payment.getDueDate())
                .paidDate(payment.getPaidDate())
                .installmentNumber(payment.getInstallmentNumber())
                .paymentMethod(payment.getPaymentMethod())
                .transactionReference(payment.getTransactionReference())
                .notes(payment.getNotes());

        // Calculate overdue days
        if (payment.getDueDate() != null && payment.getPaidDate() == null 
                && payment.getStatus() != PaymentStatusEnum.SUCCESS 
                && payment.getStatus() != PaymentStatusEnum.SYSTEM_SUCCESS) {
            long overdue = ChronoUnit.DAYS.between(payment.getDueDate(), LocalDate.now());
            if (overdue > 0) {
                builder.overdueDays(overdue);
                builder.penaltyApplied(payment.getPenaltyAmount() != null && payment.getPenaltyAmount().signum() > 0);
            } else {
                builder.overdueDays(0L);
                builder.penaltyApplied(false);
            }
        } else {
            builder.overdueDays(0L);
            builder.penaltyApplied(payment.getPenaltyAmount() != null && payment.getPenaltyAmount().signum() > 0);
        }

        // Contract context
        Contract contract = payment.getContract();
        if (contract != null) {
            builder.contractId(contract.getId())
                   .contractNumber(contract.getContractNumber())
                   .contractType(contract.getContractType() != null ? contract.getContractType().name() : null)
                   .contractStatus(contract.getStatus() != null ? contract.getStatus().name() : null);
        }

        // Property context
        Property property = payment.getProperty();
        if (property == null && contract != null) {
            property = contract.getProperty();
        }
        if (property != null) {
            builder.propertyId(property.getId())
                   .propertyTitle(property.getTitle())
                   .propertyAddress(property.getFullAddress());
        }

        // Agent context (salary/bonus)
        SaleAgent agent = payment.getSaleAgent();
        if (agent != null) {
            builder.agentId(agent.getId())
                   .agentEmployeeCode(agent.getEmployeeCode());
            if (agent.getUser() != null) {
                builder.agentFirstName(agent.getUser().getFirstName())
                       .agentLastName(agent.getUser().getLastName());
            }
        }

        PartyInfo payer = resolvePayer(payment);
        PartyInfo payee = resolvePayee(payment);
        applyPartyInfoToDetail(builder, payer, true);
        applyPartyInfoToDetail(builder, payee, false);

        return builder.build();
    }

    private PartyInfo resolvePayer(Payment payment) {
        PaymentTypeEnum type = payment.getPaymentType();
        Contract contract = payment.getContract();
        Property property = resolvePropertyContext(payment);

        if (type == PaymentTypeEnum.SERVICE_FEE) {
            PartyInfo owner = buildOwnerParty(property);
            return owner != null ? owner : buildCompanyParty();
        }

        if (type == PaymentTypeEnum.SALARY || type == PaymentTypeEnum.BONUS) {
            return buildCompanyParty();
        }

        if (type == PaymentTypeEnum.REFUND) {
            if (isOwnerRefundCollection(payment)) {
                PartyInfo owner = buildOwnerParty(property);
                return owner != null ? owner : buildCompanyParty();
            }
            return buildCompanyParty();
        }

        if (isOwnerPayout(payment)) {
            return buildCompanyParty();
        }

        PartyInfo customer = buildCustomerParty(contract);
        return customer != null ? customer : buildCompanyParty();
    }

    private PartyInfo resolvePayee(Payment payment) {
        PaymentTypeEnum type = payment.getPaymentType();
        Contract contract = payment.getContract();
        Property property = resolvePropertyContext(payment);

        if (type == PaymentTypeEnum.SALARY || type == PaymentTypeEnum.BONUS) {
            PartyInfo agentParty = buildAgentParty(payment.getSaleAgent());
            return agentParty != null ? agentParty : buildCompanyParty();
        }

        if (type == PaymentTypeEnum.SERVICE_FEE) {
            return buildCompanyParty();
        }

        if (type == PaymentTypeEnum.REFUND) {
            if (isOwnerRefundCollection(payment)) {
                return buildCompanyParty();
            }
            PartyInfo customer = buildCustomerParty(contract);
            return customer != null ? customer : buildCompanyParty();
        }

        if (isOwnerPayout(payment)) {
            PartyInfo owner = buildOwnerParty(property);
            return owner != null ? owner : buildCompanyParty();
        }

        return buildCompanyParty();
    }

    private PartyInfo buildCompanyParty() {
        return new PartyInfo(null, COMPANY_NAME, null, COMPANY_ROLE, null);
    }

    private PartyInfo buildCustomerParty(Contract contract) {
        if (contract == null || contract.getCustomer() == null || contract.getCustomer().getUser() == null) {
            return null;
        }
        User user = contract.getCustomer().getUser();
        return new PartyInfo(user.getId(), user.getFirstName(), user.getLastName(), "CUSTOMER", user.getPhoneNumber());
    }

    private PartyInfo buildOwnerParty(Property property) {
        if (property == null || property.getOwner() == null || property.getOwner().getUser() == null) {
            return null;
        }
        User owner = property.getOwner().getUser();
        return new PartyInfo(owner.getId(), owner.getFirstName(), owner.getLastName(), "PROPERTY_OWNER", owner.getPhoneNumber());
    }

    private PartyInfo buildAgentParty(SaleAgent agent) {
        if (agent == null || agent.getUser() == null) {
            return null;
        }
        User user = agent.getUser();
        return new PartyInfo(user.getId(), user.getFirstName(), user.getLastName(), "SALESAGENT", user.getPhoneNumber());
    }

    private boolean isOwnerRefundCollection(Payment payment) {
        return payment.getPaymentType() == PaymentTypeEnum.REFUND
                && payment.getPaymentMethod() != null
                && payment.getPaymentMethod().equalsIgnoreCase(PAYOS_METHOD);
    }

    private boolean isOwnerPayout(Payment payment) {
        PaymentTypeEnum type = payment.getPaymentType();
        String method = payment.getPaymentMethod();
        return CUSTOMER_TO_COMPANY_TYPES.contains(type)
                && method != null
                && (method.equalsIgnoreCase(OWNER_PAYOUT_METHOD) || method.equalsIgnoreCase(COMPANY_PAYOUT_METHOD));
    }

    private Property resolvePropertyContext(Payment payment) {
        if (payment.getProperty() != null) {
            return payment.getProperty();
        }
        if (payment.getContract() != null) {
            return payment.getContract().getProperty();
        }
        return null;
    }

    private void applyPartyInfoToList(PaymentListItem.PaymentListItemBuilder builder, PartyInfo info, boolean payer) {
        if (info == null) {
            return;
        }
        if (payer) {
            builder.payerId(info.id())
                   .payerName(info.displayName())
                   .payerRole(info.role());
        } else {
            builder.payeeId(info.id())
                   .payeeName(info.displayName())
                   .payeeRole(info.role());
        }
    }

    private void applyPartyInfoToDetail(PaymentDetailResponse.PaymentDetailResponseBuilder builder, PartyInfo info, boolean payer) {
        if (info == null) {
            return;
        }
        if (payer) {
            builder.payerId(info.id())
                   .payerFirstName(info.firstName())
                   .payerLastName(info.lastName())
                   .payerRole(info.role())
                   .payerPhone(info.phone());
        } else {
            builder.payeeId(info.id())
                   .payeeFirstName(info.firstName())
                   .payeeLastName(info.lastName())
                   .payeeRole(info.role())
                   .payeePhone(info.phone());
        }
    }
}
