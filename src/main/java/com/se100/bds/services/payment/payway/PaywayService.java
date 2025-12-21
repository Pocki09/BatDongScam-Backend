package com.se100.bds.services.payment.payway;

import com.se100.bds.services.payment.PaymentGatewayService;
import com.se100.bds.services.payment.dto.CreatePaymentSessionRequest;
import com.se100.bds.services.payment.dto.CreatePaymentSessionResponse;
import com.se100.bds.services.payment.dto.CreatePayoutSessionRequest;
import com.se100.bds.services.payment.dto.CreatePayoutSessionResponse;
import com.se100.bds.services.payment.dto.PayoutSessionResponse;
import com.se100.bds.services.payment.payway.dto.PaywayCreatePaymentRequest;
import com.se100.bds.services.payment.payway.dto.PaywayCreatePayoutRequest;
import com.se100.bds.services.payment.payway.dto.PaywayPaymentResponse;
import com.se100.bds.services.payment.payway.dto.PaywayPayoutResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Service
@RequiredArgsConstructor
public class PaywayService implements PaymentGatewayService {
    private static final String WEBHOOK_ROUTE = "/payway";

    @Value("${payway.service-url:http://localhost:3000}")
    private String serviceUrl;

    @Value("${payway.api-key}")
    private String apiKey;

    @Value("${payway.verify-key}")
    private String verifyKey;

    @Value("${payway.webhook-base-url}")
    private String webhookBaseUrl;

    @Value("${payway.return-url}")
    private String returnUrl;

    private volatile RestClient restClient;

    private RestClient client() {
        // Lazy-init so we don't need extra @Configuration right now.
        RestClient c = restClient;
        if (c != null) return c;

        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        RestClient created = RestClient.builder()
                .baseUrl(serviceUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        restClient = created;
        return created;
    }

    @Override
    public CreatePaymentSessionResponse createPaymentSession(CreatePaymentSessionRequest request, String idempotencyKey) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.getAmount() == null || request.getAmount() < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        if (!StringUtils.hasText(request.getCurrency())) {
            throw new IllegalArgumentException("currency is required");
        }

        if (!StringUtils.hasText(request.getReturnUrl())) {
            request.setReturnUrl(returnUrl);
        }
        if (!StringUtils.hasText(request.getWebhookUrl()) && StringUtils.hasText(webhookBaseUrl)) {
            request.setWebhookUrl(normalizeBaseUrl(webhookBaseUrl) + WEBHOOK_ROUTE);
        }

        PaywayCreatePaymentRequest paywayRequest = PaywayCreatePaymentRequest.builder()
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .description(request.getDescription())
                .metadata(request.getMetadata())
                .returnUrl(request.getReturnUrl())
                .webhookUrl(request.getWebhookUrl())
                .build();

        RestClient.RequestBodySpec spec = client()
                .post()
                .uri("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);

        if (StringUtils.hasText(idempotencyKey)) {
            spec = spec.header("Idempotency-Key", idempotencyKey);
        }

        PaywayPaymentResponse paywayResp = spec
                .body(paywayRequest)
                .retrieve()
                .body(PaywayPaymentResponse.class);

        if (paywayResp == null) {
            throw new IllegalStateException("Payway returned empty response");
        }

        return mapPayment(paywayResp);
    }

    @Override
    public CreatePaymentSessionResponse getPaymentSession(String paymentId) {
        if (!StringUtils.hasText(paymentId)) {
            throw new IllegalArgumentException("paymentId is required");
        }

        PaywayPaymentResponse paywayResp = client()
                .get()
                .uri("/api/payments/{id}", paymentId)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .retrieve()
                .body(PaywayPaymentResponse.class);

        if (paywayResp == null) {
            throw new IllegalStateException("Payway returned empty response");
        }

        return mapPayment(paywayResp);
    }

    @Override
    public CreatePayoutSessionResponse createPayoutSession(CreatePayoutSessionRequest request, String idempotencyKey) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.getAmount() == null || request.getAmount() < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        if (!StringUtils.hasText(request.getCurrency())) {
            throw new IllegalArgumentException("currency is required");
        }

        if (!StringUtils.hasText(request.getWebhookUrl()) && StringUtils.hasText(webhookBaseUrl)) {
            request.setWebhookUrl(normalizeBaseUrl(webhookBaseUrl) + WEBHOOK_ROUTE);
        }

        PaywayCreatePayoutRequest paywayRequest = PaywayCreatePayoutRequest.builder()
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .destination(request.getDestination())
                .description(request.getDescription())
                .metadata(request.getMetadata())
                .webhookUrl(request.getWebhookUrl())
                .build();

        RestClient.RequestBodySpec spec = client()
                .post()
                .uri("/api/payouts")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);

        if (StringUtils.hasText(idempotencyKey)) {
            spec = spec.header("Idempotency-Key", idempotencyKey);
        }

        PaywayPayoutResponse paywayResp = spec
                .body(paywayRequest)
                .retrieve()
                .body(PaywayPayoutResponse.class);

        if (paywayResp == null) {
            throw new IllegalStateException("Payway returned empty response");
        }

        return mapPayoutCreate(paywayResp);
    }

    @Override
    public PayoutSessionResponse getPayoutSession(String payoutId) {
        if (!StringUtils.hasText(payoutId)) {
            throw new IllegalArgumentException("payoutId is required");
        }

        PaywayPayoutResponse paywayResp = client()
                .get()
                .uri("/api/payouts/{id}", payoutId)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .retrieve()
                .body(PaywayPayoutResponse.class);

        if (paywayResp == null) {
            throw new IllegalStateException("Payway returned empty response");
        }

        return mapPayout(paywayResp);
    }

    private static CreatePaymentSessionResponse mapPayment(PaywayPaymentResponse paywayResp) {
        return CreatePaymentSessionResponse.builder()
                .id(paywayResp.getId())
                .amount(paywayResp.getAmount())
                .currency(paywayResp.getCurrency())
                .status(paywayResp.getStatus())
                .description(paywayResp.getDescription())
                .metadata(paywayResp.getMetadata())
                .returnUrl(paywayResp.getReturnUrl())
                .webhookUrl(paywayResp.getWebhookUrl())
                .checkoutUrl(paywayResp.getCheckoutUrl())
                .createdAt(paywayResp.getCreatedAt())
                .updatedAt(paywayResp.getUpdatedAt())
                .build();
    }

    private static CreatePayoutSessionResponse mapPayoutCreate(PaywayPayoutResponse paywayResp) {
        return CreatePayoutSessionResponse.builder()
                .id(paywayResp.getId())
                .amount(paywayResp.getAmount())
                .currency(paywayResp.getCurrency())
                .status(paywayResp.getStatus())
                .destination(paywayResp.getDestination())
                .description(paywayResp.getDescription())
                .metadata(paywayResp.getMetadata())
                .webhookUrl(paywayResp.getWebhookUrl())
                .createdAt(paywayResp.getCreatedAt())
                .updatedAt(paywayResp.getUpdatedAt())
                .build();
    }

    private static PayoutSessionResponse mapPayout(PaywayPayoutResponse paywayResp) {
        return PayoutSessionResponse.builder()
                .id(paywayResp.getId())
                .amount(paywayResp.getAmount())
                .currency(paywayResp.getCurrency())
                .status(paywayResp.getStatus())
                .destination(paywayResp.getDestination())
                .description(paywayResp.getDescription())
                .metadata(paywayResp.getMetadata())
                .webhookUrl(paywayResp.getWebhookUrl())
                .createdAt(paywayResp.getCreatedAt())
                .updatedAt(paywayResp.getUpdatedAt())
                .build();
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) return baseUrl;
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    public String getVerifyKey() {
        return verifyKey;
    }
}
