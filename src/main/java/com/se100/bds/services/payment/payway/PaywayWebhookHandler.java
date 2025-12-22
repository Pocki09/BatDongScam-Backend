package com.se100.bds.services.payment.payway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se100.bds.models.entities.contract.Payment;
import com.se100.bds.repositories.domains.contract.PaymentRepository;
import com.se100.bds.services.payment.payway.dto.PaywayWebhookEvent;
import com.se100.bds.services.payment.payway.dto.PaywayWebhookPaymentObject;
import com.se100.bds.services.payment.payway.dto.PaywayWebhookPayoutObject;
import com.se100.bds.utils.Constants.PaymentStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaywayWebhookHandler {

    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository;

    @Transactional
    public void handlePaymentEvent(String rawBody) {
        try {
            PaywayWebhookEvent<PaywayWebhookPaymentObject> event = objectMapper.readValue(
                    rawBody.getBytes(StandardCharsets.UTF_8),
                    objectMapper.getTypeFactory().constructParametricType(PaywayWebhookEvent.class, PaywayWebhookPaymentObject.class)
            );

            if (event == null || event.getData() == null || event.getData().getObject() == null) {
                log.warn("Payway webhook: missing data.object");
                return;
            }

            String type = event.getType();
            PaywayWebhookPaymentObject obj = event.getData().getObject();

            if (type == null || obj.getId() == null) {
                log.warn("Payway webhook: missing type or payment id");
                return;
            }

            Payment payment = paymentRepository.findByPaywayPaymentId(obj.getId()).orElse(null);
            if (payment == null) {
                log.warn("Payway webhook: unknown payway payment id {}", obj.getId());
                return;
            }

            PaymentStatusEnum newStatus = mapPaymentEventToInternalStatus(type);
            if (newStatus == null) {
                log.info("Payway webhook: ignoring unsupported event type {}", type);
                return;
            }

            // Idempotency: ignore if already terminal
            if (payment.getStatus() == PaymentStatusEnum.SUCCESS) {
                return;
            }

            payment.setStatus(newStatus);
            paymentRepository.save(payment);

            // NOTE: PaymentServiceImpl has private handleSuccessfulPayment() etc; we don't call into those yet.
            // We'll hook business side-effects in a follow-up when you confirm desired behavior.
            log.info("Payway webhook: updated payment {} to {} via {}", payment.getId(), newStatus, type);

        } catch (Exception e) {
            // Best-effort: don't throw to Payway, just log.
            log.error("Payway webhook: failed to process payment event", e);
        }
    }

    /**
     * Best-effort payout webhook handling.
     * <p>
     * We currently don't have a domain Payout aggregate/repository in this codebase,
     * so we just parse + log for now.
     */
    public void handlePayoutEvent(String rawBody) {
        try {
            PaywayWebhookEvent<PaywayWebhookPayoutObject> event = objectMapper.readValue(
                    rawBody.getBytes(StandardCharsets.UTF_8),
                    objectMapper.getTypeFactory().constructParametricType(PaywayWebhookEvent.class, PaywayWebhookPayoutObject.class)
            );

            if (event == null || event.getData() == null || event.getData().getObject() == null) {
                log.warn("Payway webhook (payout): missing data.object");
                return;
            }

            String type = event.getType();
            PaywayWebhookPayoutObject obj = event.getData().getObject();
            String error = event.getData().getError();

            if (type == null || obj.getId() == null) {
                log.warn("Payway webhook (payout): missing type or payout id");
                return;
            }

            // Until we have a payout entity, we just log the incoming callback.
            if (error != null && !error.isBlank()) {
                log.warn("Payway webhook (payout): {} for payout {}, error={}", type, obj.getId(), error);
            } else {
                log.info("Payway webhook (payout): {} for payout {} (status={})", type, obj.getId(), obj.getStatus());
            }

        } catch (Exception e) {
            log.error("Payway webhook: failed to process payout event", e);
        }
    }

    private static @NotNull PaymentStatusEnum mapPaymentEventToInternalStatus(String eventType) {
        return switch (eventType) {
            case "payment.succeeded" -> PaymentStatusEnum.SUCCESS;
            // New docs: payment.canceled; keep payment.failed for backwards compat.
            case "payment.canceled", "payment.failed" -> PaymentStatusEnum.FAILED;
            default -> throw new IllegalArgumentException("Unsupported event type: " + eventType);
        };
    }
}
