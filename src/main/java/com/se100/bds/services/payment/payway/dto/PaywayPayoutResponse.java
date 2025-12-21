package com.se100.bds.services.payment.payway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaywayPayoutResponse {

    private String id;

    private Integer amount;

    private String currency;

    /** created | paid | failed */
    private String status;

    private String destination;

    private String description;

    private Map<String, Object> metadata;

    @JsonProperty("webhook_url")
    private String webhookUrl;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
}

