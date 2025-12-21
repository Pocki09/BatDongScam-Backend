package com.se100.bds.services.payment.payway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaywayCreatePayoutRequest {

    private Integer amount;

    private String currency;

    private String destination;

    private String description;

    private Map<String, Object> metadata;

    @JsonProperty("webhook_url")
    private String webhookUrl;
}

