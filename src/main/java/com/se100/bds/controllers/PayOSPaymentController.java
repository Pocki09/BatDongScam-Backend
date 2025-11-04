package com.se100.bds.controllers;

import com.se100.bds.controllers.base.AbstractBaseController;
import com.se100.bds.dtos.requests.payos.CreateContractPaymentRequest;
import com.se100.bds.dtos.responses.SingleResponse;
import com.se100.bds.dtos.responses.error.ErrorResponse;
import com.se100.bds.services.payos.PayOSService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Tag(name = "40. Payments", description = "Payment APIs via PayOS")
public class PayOSPaymentController extends AbstractBaseController {
    private final PayOSService payOSService;

    @PostMapping("/contracts/{contractId}/checkout")
    @Operation(
        summary = "Create PayOS checkout link for a contract payment",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Checkout link generated",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SingleResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Bad request",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)
                )
            )
        }
    )
    public ResponseEntity<SingleResponse<CreatePaymentLinkResponse>> createContractCheckout(
            @PathVariable UUID contractId,
            @Valid @RequestBody CreateContractPaymentRequest body
    ) {
        CreatePaymentLinkResponse resp = payOSService.createContractPaymentLink(contractId, body);
        return responseFactory.successSingle(resp, "Created PayOS contract checkout link successfully");
    }

    @PostMapping("/properties/{propertyId}/listing-fee")
    @Operation(
        summary = "Create PayOS checkout link for property listing fee",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Checkout link generated",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SingleResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Bad request",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)
                )
            )
        }
    )
    public ResponseEntity<SingleResponse<CreatePaymentLinkResponse>> createPropertyListingFee(
        @PathVariable UUID propertyId,
        @RequestParam(required = false) BigDecimal amount,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) String returnUrl,
        @RequestParam(required = false) String cancelUrl
    ) {
        CreatePaymentLinkResponse response = payOSService.createPropertyListingPaymentLink(
            propertyId,
            amount,
            description,
            returnUrl,
            cancelUrl
        );
        return responseFactory.successSingle(response, "Created PayOS listing fee checkout link successfully");
    }
}
