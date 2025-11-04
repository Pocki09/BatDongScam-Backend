package com.se100.bds.controllers;

import com.se100.bds.controllers.base.AbstractBaseController;
import com.se100.bds.dtos.requests.payos.CreatePayoutRequest;
import com.se100.bds.dtos.responses.SingleResponse;
import com.se100.bds.dtos.responses.error.ErrorResponse;
import com.se100.bds.services.payos.impl.PayOSPayoutServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.model.v1.payouts.Payout;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payouts")
@Tag(name = "41. Payouts", description = "Manual payout APIs via PayOS")
public class PayOSPayoutController extends AbstractBaseController {

    private final PayOSPayoutServiceImpl payoutService;

    @PostMapping("/employees")
    @Operation(
        summary = "Trigger a manual PayOS payout for an employee",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Payout created successfully",
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
    public ResponseEntity<SingleResponse<Payout>> createEmployeePayout(@Valid @RequestBody CreatePayoutRequest request) {
        if (request.getSaleAgentId() == null) {
            return responseFactory.failedSingle(null, "Sale agent id is required for payout");
        }

        String referenceId = StringUtils.hasText(request.getReferenceId())
                ? request.getReferenceId()
                : String.format("salary_%s_%d", request.getSaleAgentId(), System.currentTimeMillis() / 1000);

        try {
            Payout payout = payoutService.createSalaryPayoutForAgent(
                    request.getSaleAgentId(),
                    referenceId,
                    request.getAmount(),
                    request.getDescription(),
                    request.getCategories()
            );
            return responseFactory.successSingle(payout, "Created PayOS employee payout successfully");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return responseFactory.failedSingle(null, ex.getMessage());
        }
    }
}
