package com.se100.bds.services.payment;

import com.se100.bds.dtos.requests.payos.CreateContractPaymentRequest;

import java.util.UUID;

public interface PaymentService {
    CreatePaymentLinkResponse createContractPaymentLink(UUID contractId, CreateContractPaymentRequest request);
    void handlePaymentWebhook(String rawBody);
    CreatePaymentLinkResponse createPropertyServicePaymentLink(UUID propertyId, String description, String returnUrl, String cancelUrl);
    CreatePaymentLinkResponse createCancellationRefundCollectionLink(UUID contractId);
}