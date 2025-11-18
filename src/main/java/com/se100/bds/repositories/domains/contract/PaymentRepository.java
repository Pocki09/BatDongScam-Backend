package com.se100.bds.repositories.domains.contract;

import com.se100.bds.models.entities.contract.Payment;
import com.se100.bds.utils.Constants.PaymentStatusEnum;
import com.se100.bds.utils.Constants.PaymentTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {
	Optional<Payment> findByPayosOrderCode(Long payosOrderCode);
	List<Payment> findAllByContract_IdAndStatus(UUID contractId, PaymentStatusEnum status);
	Optional<Payment> findFirstByContract_IdAndPaymentTypeAndStatus(UUID contractId, PaymentTypeEnum paymentType, PaymentStatusEnum status);
	Optional<Payment> findFirstByContract_IdAndPaymentTypeOrderByCreatedAtDesc(UUID contractId, PaymentTypeEnum paymentType);
	Optional<Payment> findFirstByProperty_IdAndPaymentTypeOrderByCreatedAtDesc(UUID propertyId, PaymentTypeEnum paymentType);
}

