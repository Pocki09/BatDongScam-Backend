package com.se100.bds.services.payos;

import com.se100.bds.dtos.requests.payos.CreateContractPaymentRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface PayOSService {
    CreatePaymentLinkResponse createContractPaymentLink(UUID contractId, CreateContractPaymentRequest request);
    String confirmWebhook(String webhookPublicBaseUrl);
    void handlePaymentWebhook(String rawBody);
    CreatePaymentLinkResponse createPropertyListingPaymentLink(UUID propertyId, BigDecimal amountOverride, String description, String returnUrl, String cancelUrl);
}