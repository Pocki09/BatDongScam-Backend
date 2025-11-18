package com.se100.bds.controllers;


import com.se100.bds.controllers.base.AbstractBaseController;
import com.se100.bds.dtos.responses.SingleResponse;
import com.se100.bds.dtos.responses.error.ErrorResponse;
import com.se100.bds.services.payos.PayOSService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webhooks/payos")
@Tag(name = "100. PayOS Webhooks", description = "PayOS Webhook API")
@Slf4j
public class PayOSWebhookController extends AbstractBaseController {

	private final PayOSService payOSService;

    @PostMapping
    @Operation(
	    summary = "Handle PayOS payment webhook callback",
	    responses = {
		    @ApiResponse(
			    responseCode = "200",
			    description = "Webhook processed",
			    content = @Content(
				    mediaType = MediaType.APPLICATION_JSON_VALUE,
				    schema = @Schema(implementation = SingleResponse.class)
			    )
		    ),
		    @ApiResponse(
			    responseCode = "400",
			    description = "Invalid payload",
			    content = @Content(
				    mediaType = MediaType.APPLICATION_JSON_VALUE,
				    schema = @Schema(implementation = ErrorResponse.class)
			    )
		    )
	    }
    )
    public ResponseEntity<SingleResponse<Void>> handle(@RequestBody String rawBody) {
		payOSService.handlePaymentWebhook(rawBody);
		return responseFactory.successSingle(null, "Webhook handled successfully");
	}
}
