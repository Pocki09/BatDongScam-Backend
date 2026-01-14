package com.se100.bds.services.domains.payment.webhook.impl;

import com.se100.bds.models.entities.contract.Payment;
import com.se100.bds.models.entities.contract.RentalContract;
import com.se100.bds.services.domains.contract.RentalContractService;
import com.se100.bds.services.domains.payment.webhook.PaymentGatewayWebhookEvent;
import com.se100.bds.services.domains.payment.webhook.PaymentSucceededSideEffectHandler;
import com.se100.bds.utils.Constants.ContractStatusEnum;
import com.se100.bds.utils.Constants.PaymentTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Side effects for SECURITY_DEPOSIT and MONTHLY payments on rental contracts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RentalSecurityDepositPaymentSucceededHandler implements PaymentSucceededSideEffectHandler {

    private final RentalContractService rentalContractService;

    @Override
    public boolean supports(Payment payment) {
        if (payment == null || !(payment.getContract() instanceof RentalContract)) {
            return false;
        }
        PaymentTypeEnum type = payment.getPaymentType();
        return type == PaymentTypeEnum.SECURITY_DEPOSIT || type == PaymentTypeEnum.MONTHLY;
    }

    @Override
    public void handle(Payment payment, PaymentGatewayWebhookEvent event) {
        if (payment.getContract() == null) {
            log.warn("Rental payment {} has no contract associated", payment.getId());
            return;
        }

        RentalContract rentalContract = (RentalContract) payment.getContract();

        log.info("Rental payment succeeded: type={}, paymentId={}, contractId={}, gatewayEventId={}",
                payment.getPaymentType(),
                payment.getId(),
                rentalContract.getId(),
                event != null ? event.getExternalEventId() : null);

        if (payment.getPaymentType() == PaymentTypeEnum.SECURITY_DEPOSIT) {
            // Update security deposit status to HELD
            rentalContractService.onSecurityDepositPaymentCompleted(rentalContract.getId());
        } else if (payment.getPaymentType() == PaymentTypeEnum.MONTHLY) {
            // Check if this is the first month payment (contract in PENDING_PAYMENT state)
            if (rentalContract.getStatus() == ContractStatusEnum.PENDING_PAYMENT) {
                rentalContractService.onFirstMonthRentPaymentCompleted(rentalContract.getId());
            } else {
                // Subsequent monthly payment
                rentalContractService.onMonthlyRentPaymentCompleted(rentalContract.getId(), payment.getId());
            }
        }
    }
}
