package com.se100.bds.dtos.requests.payos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CreatePayoutRequest {
    private String referenceId;

    @NotNull
    @DecimalMin(value = "2000")
    private BigDecimal amount;

    @NotBlank
    private String description;

    private UUID saleAgentId;

    private List<String> categories;
}
