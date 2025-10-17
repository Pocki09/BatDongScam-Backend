package com.se100.bds.controllers;

import com.se100.bds.controllers.base.AbstractBaseController;
import com.se100.bds.dtos.responses.PageResponse;
import com.se100.bds.dtos.responses.SingleResponse;
import com.se100.bds.dtos.responses.error.ErrorResponse;
import com.se100.bds.dtos.responses.property.PropertyDetails;
import com.se100.bds.dtos.responses.property.SimplePropertyCard;
import com.se100.bds.mappers.PropertyMapper;
import com.se100.bds.models.entities.property.Property;
import com.se100.bds.services.domains.property.PropertyService;
import com.se100.bds.services.dtos.results.PropertyCard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.se100.bds.utils.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/properties")
@Tag(name = "003. Public Properties", description = "Public Property Listing API")
@Slf4j
public class PublicPropertyController extends AbstractBaseController {
    private final PropertyMapper propertyMapper;
    private final PropertyService propertyService;

    @GetMapping("/cards")
    @Operation(
            summary = "Get all property cards with filters and pagination",
            description = "Retrieve a paginated list of property cards with optional filters for location, price, area, and property features",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid parameters",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<PageResponse<SimplePropertyCard>> getAllCardsWithFilters(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "15") int limit,

            @Parameter(description = "Sort direction: asc or desc")
            @RequestParam(defaultValue = "desc") String sortType,

            @Parameter(description = "Field to sort by")
            @RequestParam(required = false) String sortBy,

            @Parameter(description = "List of city IDs to filter by")
            @RequestParam(required = false) List<UUID> cityIds,

            @Parameter(description = "List of district IDs to filter by")
            @RequestParam(required = false) List<UUID> districtIds,

            @Parameter(description = "List of ward IDs to filter by")
            @RequestParam(required = false) List<UUID> wardIds,

            @Parameter(description = "List of property type IDs to filter by")
            @RequestParam(required = false) List<UUID> propertyTypeIds,

            @Parameter(description = "Property owner ID")
            @RequestParam(required = false) UUID ownerId,

            @Parameter(description = "Get top K property? This sorted by most popular property")
            @RequestParam(required = true, defaultValue = "false") Boolean topK,

            @Parameter(description = "Minimum price")
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(description = "Maximum price")
            @RequestParam(required = false) BigDecimal maxPrice,

            @Parameter(description = "Minimum total area (square meters)")
            @RequestParam(required = false) BigDecimal totalArea,

            @Parameter(description = "Number of rooms")
            @RequestParam(required = false) Integer rooms,

            @Parameter(description = "Number of bathrooms")
            @RequestParam(required = false) Integer bathrooms,

            @Parameter(description = "Number of bedrooms")
            @RequestParam(required = false) Integer bedrooms,

            @Parameter(description = "Number of floors")
            @RequestParam(required = false) Integer floors,

            @Parameter(description = "House orientation (e.g., EAST, WEST, NORTH, SOUTH)")
            @RequestParam(required = false) String houseOrientation,

            @Parameter(description = "Balcony orientation (e.g., EAST, WEST, NORTH, SOUTH)")
            @RequestParam(required = false) String balconyOrientation,

            @Parameter(description = "Transaction type (e.g., SALE, RENT)")
            @RequestParam(required = false) String transactionType,

            @Parameter(description = "Property status (e.g., AVAILABLE, SOLD, RENTED, PENDING, APPROVED)")
            @RequestParam(required = false) String status
    ) {
        if (!topK)
            sortBy = null;
        Pageable pageable = createPageable(page, limit, sortType, sortBy);

        Page<PropertyCard> propertyCards = propertyService.getAllCardsWithFilters(
                cityIds,
                districtIds,
                wardIds,
                propertyTypeIds,
                ownerId,
                minPrice,
                maxPrice,
                totalArea,
                rooms,
                bathrooms,
                bedrooms,
                floors,
                houseOrientation,
                balconyOrientation,
                transactionType,
                status,
                topK,
                pageable
        );

        Page<SimplePropertyCard> simplePropertyCards = propertyMapper.mapToPage(propertyCards, SimplePropertyCard.class);

        return responseFactory.successPage(simplePropertyCards, "Property cards retrieved successfully");
    }

    @GetMapping("/{propertyId}")
    @Operation(
            summary = "Get property details by ID",
            description = "Retrieve detailed information about a specific property including owner, agent, location, and media",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Property not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<PropertyDetails>> getPropertyDetails(
            @Parameter(description = "Property ID", required = true)
            @PathVariable UUID propertyId
    ) {
        log.info("Getting property details for ID: {}", propertyId);

        PropertyDetails propertyDetails = propertyService.getPropertyDetailsById(propertyId);

        return responseFactory.successSingle(propertyDetails, "Property details retrieved successfully");
    }
}
