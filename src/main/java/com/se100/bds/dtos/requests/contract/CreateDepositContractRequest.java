package com.se100.bds.dtos.requests.contract;

import com.se100.bds.utils.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDepositContractRequest {
    @NotNull(message = "Property ID is required")
    @Schema(description = "The property ID for this deposit contract")
    private UUID propertyId;

    @Schema(description = "The agent ID handling this contract. Only takes effect (and is required) " +
            "when user is NOT a Sale Agent, otherwise, the agent ID will be set to the current user's ID.")
    private UUID agentId;

    @NotNull(message = "Customer ID is required")
    @Schema(description = "The customer ID for this contract")
    private UUID customerId;

    @NotNull(message = "Deposit amount is required")
    @Schema(description = "Deposit amount", example = "100000000")
    private Double depositAmount;


}
