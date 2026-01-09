package com.se100.bds.controllers;

import com.se100.bds.controllers.base.AbstractBaseController;
import com.se100.bds.dtos.requests.contract.CancelContractRequest;
import com.se100.bds.dtos.requests.contract.CreateContractRequest;
import com.se100.bds.dtos.requests.contract.CreateDepositContractRequest;
import com.se100.bds.dtos.requests.contract.UpdateContractRequest;
import com.se100.bds.dtos.responses.PageResponse;
import com.se100.bds.dtos.responses.SingleResponse;
import com.se100.bds.dtos.responses.contract.ContractDetailResponse;
import com.se100.bds.dtos.responses.contract.ContractListItem;
import com.se100.bds.dtos.responses.contract.CreateContractResponse;
import com.se100.bds.services.domains.contract.ContractService;
import com.se100.bds.utils.Constants.ContractStatusEnum;
import com.se100.bds.utils.Constants.ContractTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.se100.bds.utils.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/contracts")
@Tag(name = "014. Contract Management", description = "Contract CRUD operations, signing, cancellation, and penalties")
@Slf4j
public class ContractController extends AbstractBaseController {

    private final ContractService contractService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALESAGENT')")
    @Operation(
            summary = "Create a new contract",
            description = "Creates a new contract in DRAFT status. Only admin and sales agents can create contracts.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contract created successfully",
                    content = @Content(schema = @Schema(implementation = SingleResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Property, customer, or agent not found")
    })
    public ResponseEntity<SingleResponse<CreateContractResponse>> createContract(
            @Valid @RequestBody CreateContractRequest request
    ) {
        var response = contractService.createContract(request);
        return responseFactory.successSingle(response, "Contract created successfully");
    }

    /// Create a deposit contract
    @PostMapping("/deposit")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALESAGENT')")
    @Operation(
            summary = "Create a new deposit contract",
            description = "Creates a new deposit contract in DRAFT status. Only admin and sales agents can create deposit contracts.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Deposit contract created successfully",
                    content = @Content(schema = @Schema(implementation = SingleResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Property, customer, or agent not found")
    })
    public ResponseEntity<SingleResponse<CreateContractResponse>> createDepositContract(
            @Valid @RequestBody CreateDepositContractRequest request
    ) {
        var response = contractService.createDepositContract(request);
        return responseFactory.successSingle(response, "Deposit contract created successfully");
    }

    ///

//    @GetMapping("/{contractId}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'SALESAGENT', 'CUSTOMER')")
//    @Operation(
//            summary = "Get contract by ID",
//            description = "Returns detailed contract information including payments, property, customer, and agent info",
//            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Contract retrieved successfully",
//                    content = @Content(schema = @Schema(implementation = SingleResponse.class))
//            ),
//            @ApiResponse(responseCode = "401", description = "Unauthorized"),
//            @ApiResponse(responseCode = "404", description = "Contract not found")
//    })
//    public ResponseEntity<SingleResponse<ContractDetailResponse>> getContractById(
//            @Parameter(description = "Contract ID", required = true)
//            @PathVariable UUID contractId
//    ) {
//        ContractDetailResponse response = contractService.getContractById(contractId);
//        return responseFactory.successSingle(response, "Contract retrieved successfully");
//    }
//
//    @GetMapping
//    @PreAuthorize("hasAnyRole('ADMIN', 'SALESAGENT')")
//    @Operation(
//            summary = "Get paginated list of contracts",
//            description = "Query contracts with various filters. For admin and sales agents.",
//            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Contracts retrieved successfully",
//                    content = @Content(schema = @Schema(implementation = PageResponse.class))
//            ),
//            @ApiResponse(responseCode = "401", description = "Unauthorized")
//    })
//    public ResponseEntity<PageResponse<ContractListItem>> getContracts(
//            @Parameter(description = "Page number (0-based)")
//            @RequestParam(defaultValue = "0") int page,
//
//            @Parameter(description = "Page size")
//            @RequestParam(defaultValue = "20") int size,
//
//            @Parameter(description = "Sort by field")
//            @RequestParam(defaultValue = "createdAt") String sortBy,
//
//            @Parameter(description = "Sort direction")
//            @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection,
//
//            @Parameter(description = "Filter by contract types")
//            @RequestParam(required = false) List<ContractTypeEnum> contractTypes,
//
//            @Parameter(description = "Filter by statuses")
//            @RequestParam(required = false) List<ContractStatusEnum> statuses,
//
//            @Parameter(description = "Filter by customer ID")
//            @RequestParam(required = false) UUID customerId,
//
//            @Parameter(description = "Filter by agent ID")
//            @RequestParam(required = false) UUID agentId,
//
//            @Parameter(description = "Filter by property ID")
//            @RequestParam(required = false) UUID propertyId,
//
//            @Parameter(description = "Filter by start date from")
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
//
//            @Parameter(description = "Filter by start date to")
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateTo,
//
//            @Parameter(description = "Filter by end date from")
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateFrom,
//
//            @Parameter(description = "Filter by end date to")
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateTo,
//
//            @Parameter(description = "Search by contract number or property title")
//            @RequestParam(required = false) String search
//    ) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
//
//        Page<ContractListItem> contracts = contractService.getContracts(
//                pageable, contractTypes, statuses, customerId, agentId, propertyId,
//                startDateFrom, startDateTo, endDateFrom, endDateTo, search
//        );
//
//        return responseFactory.successPage(contracts, "Contracts retrieved successfully");
//    }
//
//    @PutMapping("/{contractId}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'SALESAGENT')")
//    @Operation(
//            summary = "Update contract",
//            description = "Update contract details. Only allowed for DRAFT or PENDING_SIGNING status.",
//            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Contract updated successfully",
//                    content = @Content(schema = @Schema(implementation = SingleResponse.class))
//            ),
//            @ApiResponse(responseCode = "400", description = "Invalid request or status"),
//            @ApiResponse(responseCode = "401", description = "Unauthorized"),
//            @ApiResponse(responseCode = "404", description = "Contract not found")
//    })
//    public ResponseEntity<SingleResponse<ContractDetailResponse>> updateContract(
//            @Parameter(description = "Contract ID", required = true)
//            @PathVariable UUID contractId,
//
//            @Valid @RequestBody UpdateContractRequest request
//    ) {
//        ContractDetailResponse response = contractService.updateContract(contractId, request);
//        return responseFactory.successSingle(response, "Contract updated successfully");
//    }
//
//    @PostMapping("/{contractId}/sign")
//    @PreAuthorize("hasAnyRole('ADMIN', 'SALESAGENT')")
//    @Operation(
//            summary = "Sign a contract",
//            description = "Transitions contract from DRAFT/PENDING_SIGNING to ACTIVE status",
//            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Contract signed successfully",
//                    content = @Content(schema = @Schema(implementation = SingleResponse.class))
//            ),
//            @ApiResponse(responseCode = "400", description = "Contract cannot be signed in current status"),
//            @ApiResponse(responseCode = "401", description = "Unauthorized"),
//            @ApiResponse(responseCode = "404", description = "Contract not found")
//    })
//    public ResponseEntity<SingleResponse<ContractDetailResponse>> signContract(
//            @Parameter(description = "Contract ID", required = true)
//            @PathVariable UUID contractId
//    ) {
//        ContractDetailResponse response = contractService.signContract(contractId);
//        return responseFactory.successSingle(response, "Contract signed successfully");
//    }
//
//    @PostMapping("/{contractId}/cancel")
//    @PreAuthorize("hasAnyRole('ADMIN', 'SALESAGENT', 'CUSTOMER')")
//    @Operation(
//            summary = "Cancel a contract",
//            description = "Cancels a contract and calculates penalty. Admin can waive penalty.",
//            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Contract cancelled successfully",
//                    content = @Content(schema = @Schema(implementation = SingleResponse.class))
//            ),
//            @ApiResponse(responseCode = "400", description = "Contract cannot be cancelled (already cancelled or completed)"),
//            @ApiResponse(responseCode = "401", description = "Unauthorized"),
//            @ApiResponse(responseCode = "404", description = "Contract not found")
//    })
//    public ResponseEntity<SingleResponse<ContractDetailResponse>> cancelContract(
//            @Parameter(description = "Contract ID", required = true)
//            @PathVariable UUID contractId,
//
//            @Valid @RequestBody CancelContractRequest request
//    ) {
//        ContractDetailResponse response = contractService.cancelContract(contractId, request);
//        return responseFactory.successSingle(response, "Contract cancelled successfully");
//    }
//
//    @GetMapping("/my")
//    @PreAuthorize("hasRole('CUSTOMER')")
//    @Operation(
//            summary = "Get my contracts",
//            description = "Get contracts for the currently logged in customer",
//            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Contracts retrieved successfully",
//                    content = @Content(schema = @Schema(implementation = PageResponse.class))
//            ),
//            @ApiResponse(responseCode = "401", description = "Unauthorized")
//    })
//    public ResponseEntity<PageResponse<ContractListItem>> getMyContracts(
//            @Parameter(description = "Page number (0-based)")
//            @RequestParam(defaultValue = "0") int page,
//
//            @Parameter(description = "Page size")
//            @RequestParam(defaultValue = "20") int size,
//
//            @Parameter(description = "Filter by statuses")
//            @RequestParam(required = false) List<ContractStatusEnum> statuses
//    ) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
//        Page<ContractListItem> contracts = contractService.getMyContracts(pageable, statuses);
//        return responseFactory.successPage(contracts, "My contracts retrieved successfully");
//    }
//
//    @GetMapping("/agent/my")
//    @PreAuthorize("hasRole('SALESAGENT')")
//    @Operation(
//            summary = "Get my agent contracts",
//            description = "Get contracts assigned to the currently logged in agent",
//            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Contracts retrieved successfully",
//                    content = @Content(schema = @Schema(implementation = PageResponse.class))
//            ),
//            @ApiResponse(responseCode = "401", description = "Unauthorized")
//    })
//    public ResponseEntity<PageResponse<ContractListItem>> getMyAgentContracts(
//            @Parameter(description = "Page number (0-based)")
//            @RequestParam(defaultValue = "0") int page,
//
//            @Parameter(description = "Page size")
//            @RequestParam(defaultValue = "20") int size,
//
//            @Parameter(description = "Filter by statuses")
//            @RequestParam(required = false) List<ContractStatusEnum> statuses
//    ) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
//        Page<ContractListItem> contracts = contractService.getMyAgentContracts(pageable, statuses);
//        return responseFactory.successPage(contracts, "Agent contracts retrieved successfully");
//    }
//
//    @PostMapping("/{contractId}/rate")
//    @PreAuthorize("hasRole('CUSTOMER')")
//    @Operation(
//            summary = "Rate a completed contract",
//            description = "Rate a completed contract with a rating (1-5) and optional comment",
//            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Contract rated successfully",
//                    content = @Content(schema = @Schema(implementation = SingleResponse.class))
//            ),
//            @ApiResponse(responseCode = "400", description = "Contract is not completed or invalid rating"),
//            @ApiResponse(responseCode = "401", description = "Unauthorized"),
//            @ApiResponse(responseCode = "404", description = "Contract not found")
//    })
//    public ResponseEntity<SingleResponse<ContractDetailResponse>> rateContract(
//            @Parameter(description = "Contract ID", required = true)
//            @PathVariable UUID contractId,
//
//            @Parameter(description = "Rating (1-5)", required = true)
//            @RequestParam Short rating,
//
//            @Parameter(description = "Comment")
//            @RequestParam(required = false) String comment
//    ) {
//        ContractDetailResponse response = contractService.rateContract(contractId, rating, comment);
//        return responseFactory.successSingle(response, "Contract rated successfully");
//    }
}
