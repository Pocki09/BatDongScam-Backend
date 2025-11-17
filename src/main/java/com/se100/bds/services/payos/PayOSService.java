package com.se100.bds.services.payos;

import com.se100.bds.dtos.requests.payos.CreateContractPaymentRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.util.UUID;

public interface PayOSService {
    CreatePaymentLinkResponse createContractPaymentLink(UUID contractId, CreateContractPaymentRequest request);
    void handlePaymentWebhook(String rawBody);
    CreatePaymentLinkResponse createPropertyServicePaymentLink(UUID propertyId, String description, String returnUrl, String cancelUrl);
    CreatePaymentLinkResponse createCancellationRefundCollectionLink(UUID contractId);
}