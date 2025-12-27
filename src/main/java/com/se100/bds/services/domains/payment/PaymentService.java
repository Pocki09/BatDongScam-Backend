package com.se100.bds.services.domains.payment;

import com.se100.bds.dtos.requests.payment.UpdatePaymentStatusRequest;
import com.se100.bds.dtos.responses.payment.PaymentDetailResponse;
import com.se100.bds.dtos.responses.payment.PaymentListItem;
import com.se100.bds.models.entities.property.Property;
import com.se100.bds.utils.Constants;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PaymentService {
    /**
     * Get paginated list of payments with filters (for accountant)
     */
    Page<PaymentListItem> getPayments(
            Pageable pageable,
            List<Constants.PaymentTypeEnum> paymentTypes,
            List<Constants.PaymentStatusEnum> statuses,
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
    );

    // get payments of property
    @Transactional(readOnly = true)
    Page<PaymentListItem> getPaymentsOfProperty(
            Pageable pageable,
            @NotNull UUID propertyId
    );

    /**
     * Get payment detail by ID
     */
    PaymentDetailResponse getPaymentById(UUID paymentId);

    /**
     * Update payment status (accountant marks as paid externally)
     */
    PaymentDetailResponse updatePaymentStatus(UUID paymentId, UpdatePaymentStatusRequest request);

    /**
     * Create service fee payment (for property listing)
     */
    PaymentDetailResponse createServiceFeePayment(Property property);

    /**
     * Process a Payway webhook event (raw request body JSON string).
     * Signature verification is handled at the controller layer.
     */
    void handlePaywayWebhook(String rawBody);
}
