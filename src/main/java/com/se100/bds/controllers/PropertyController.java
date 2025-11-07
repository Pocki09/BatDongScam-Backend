package com.se100.bds.controllers;

import com.se100.bds.controllers.base.AbstractBaseController;
import com.se100.bds.dtos.requests.property.CreatePropertyTypeRequest;
import com.se100.bds.dtos.requests.property.UpdatePropertyTypeRequest;
import com.se100.bds.dtos.responses.SingleResponse;
import com.se100.bds.dtos.responses.SuccessResponse;
import com.se100.bds.dtos.responses.error.DetailedErrorResponse;
import com.se100.bds.dtos.responses.error.ErrorResponse;
import com.se100.bds.dtos.responses.property.PropertyTypeResponse;
import com.se100.bds.mappers.PropertyMapper;
import com.se100.bds.services.domains.property.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

import static com.se100.bds.utils.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/properties")
@Tag(name = "006. Properties", description = "Property Listing API")
@Slf4j
public class PropertyController extends AbstractBaseController {
    private final PropertyMapper propertyMapper;
    private final PropertyService propertyService;

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{propertyId}/assign-agent/{agentId}")
    @Operation(
            summary = "Assign a sales agent to a property",
            description = "Admin assigns a sales agent to manage a specific property",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Agent assigned successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - User not authenticated",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - User is not an admin",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Property or Agent not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<Void>> assignAgentToProperty(
            @Parameter(description = "Sales agent ID", required = true)
            @PathVariable UUID agentId,
            @Parameter(description = "Property ID", required = true)
            @PathVariable UUID propertyId
    ) {
        propertyService.assignAgentToProperty(agentId, propertyId);
        return responseFactory.successSingle(null, "Agent assigned to property successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/types", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create a new property type",
            description = "Admin creates a new property type with optional avatar image",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Property type created successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - User not authenticated",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - User is not an admin",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "Validation failed",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = DetailedErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<PropertyTypeResponse>> createPropertyType(
            @Parameter(description = "Request body to create property type (multipart/form-data)", required = true)
            @Valid @ModelAttribute CreatePropertyTypeRequest request,
            BindingResult bindingResult
    ) throws BindException, IOException {
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        log.info("Creating new property type - typeName: {}", request.getTypeName());
        PropertyTypeResponse response = propertyService.createPropertyType(request);
        return responseFactory.successSingle(response, "Property type created successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/types", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Update an existing property type",
            description = "Admin updates property type details including typeName, avatar, description, and active status",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Property type updated successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - User not authenticated",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - User is not an admin",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Property type not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "Validation failed",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = DetailedErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<PropertyTypeResponse>> updatePropertyType(
            @Parameter(description = "Request body to update property type (multipart/form-data)", required = true)
            @Valid @ModelAttribute UpdatePropertyTypeRequest request,
            BindingResult bindingResult
    ) throws BindException, IOException {
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        log.info("Updating property type - id: {}", request.getId());
        PropertyTypeResponse response = propertyService.updatePropertyType(request);
        return responseFactory.successSingle(response, "Property type updated successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/types/{id}")
    @Operation(
            summary = "Delete a property type by ID",
            description = "Admin deletes a property type. This will also delete the associated avatar image from Cloudinary.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Property type deleted successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - User not authenticated",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - User is not an admin",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Property type not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<SuccessResponse>> deletePropertyType(
            @Parameter(description = "Property type ID to delete", required = true)
            @PathVariable UUID id
    ) throws IOException {
        log.info("Deleting property type - id: {}", id);
        propertyService.deletePropertyType(id);
        return responseFactory.successSingle(null, "Property type deleted successfully");
    }
}
