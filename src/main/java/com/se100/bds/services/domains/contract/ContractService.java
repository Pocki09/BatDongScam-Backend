package com.se100.bds.services.domains.contract;

import com.se100.bds.dtos.requests.contract.CancelContractRequest;
import com.se100.bds.dtos.requests.contract.CreateContractRequest;
import com.se100.bds.dtos.requests.contract.CreateDepositContractRequest;
import com.se100.bds.dtos.requests.contract.UpdateContractRequest;
import com.se100.bds.dtos.responses.contract.ContractDetailResponse;
import com.se100.bds.dtos.responses.contract.ContractListItem;
import com.se100.bds.dtos.responses.contract.CreateContractResponse;
import com.se100.bds.utils.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ContractService {

    CreateContractResponse createContract(CreateContractRequest request);
    CreateContractResponse createDepositContract(CreateDepositContractRequest request);

//
//    /**
//     * Get contract by ID
//     */
//    ContractDetailResponse getContractById(UUID contractId);
//
//    /**
//     * Get paginated list of contracts with filters
//     */
//    Page<ContractListItem> getContracts(
//            Pageable pageable,
//            List<Constants.ContractTypeEnum> contractTypes,
//            List<Constants.ContractStatusEnum> statuses,
//            UUID customerId,
//            UUID agentId,
//            UUID propertyId,
//            LocalDate startDateFrom,
//            LocalDate startDateTo,
//            LocalDate endDateFrom,
//            LocalDate endDateTo,
//            String search
//    );
//
//    /**
//     * Update contract details
//     */
//    ContractDetailResponse updateContract(UUID contractId, UpdateContractRequest request);
//
//    /**
//     * Sign a contract (transition from DRAFT/PENDING_SIGNING to ACTIVE)
//     */
//    ContractDetailResponse signContract(UUID contractId);
//
//    /**
//     * Complete a contract (transition from ACTIVE to COMPLETED)
//     */
//    ContractDetailResponse completeContract(UUID contractId);
//
//    /**
//     * Cancel a contract with penalty calculation
//     */
//    ContractDetailResponse cancelContract(UUID contractId, CancelContractRequest request);
//
//    /**
//     * Calculate cancellation penalty for a contract
//     */
//    java.math.BigDecimal calculateCancellationPenalty(UUID contractId);
//
//    /**
//     * Get contracts for current customer
//     */
//    Page<ContractListItem> getMyContracts(Pageable pageable, List<Constants.ContractStatusEnum> statuses);
//
//    /**
//     * Get contracts for current agent
//     */
//    Page<ContractListItem> getMyAgentContracts(Pageable pageable, List<Constants.ContractStatusEnum> statuses);
//
//    /**
//     * Rate a completed contract
//     */
//    ContractDetailResponse rateContract(UUID contractId, Short rating, String comment);
}
