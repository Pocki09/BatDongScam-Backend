package com.se100.bds.controllers;

import com.se100.bds.controllers.base.AbstractBaseController;
import com.se100.bds.dtos.responses.PageResponse;
import com.se100.bds.dtos.responses.SingleResponse;
import com.se100.bds.dtos.responses.error.ErrorResponse;
import com.se100.bds.entities.location.City;
import com.se100.bds.services.domains.location.LocationService;
import com.se100.bds.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/locations")
@Tag(name = "005. Public Locations", description = "Public Location API")
@Slf4j
public class PublicLocationController extends AbstractBaseController {
    private final LocationService locationService;

    @GetMapping("/cities/top")
    @Operation(
            summary = "Get top K most searched/popular cities",
            description = "Retrieve a paginated list of the most popular cities based on search frequency",
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
    public ResponseEntity<PageResponse<City>> getTopCities(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int limit,

            @Parameter(description = "Sort direction: asc or desc")
            @RequestParam(defaultValue = "desc") String sortType,

            @Parameter(description = "Field to sort by")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Number of top cities to retrieve (K)", example = "10")
            @RequestParam(defaultValue = "10") int topK
    ) {
        log.info("Getting top {} cities - page: {}, limit: {}", topK, page, limit);

        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<City> topCities = locationService.topKCities(pageable, topK);

        return responseFactory.successPage(topCities, "Top cities retrieved successfully");
    }

    @GetMapping("/children")
    @Operation(
            summary = "Get child locations by parent ID",
            description = "Retrieve all districts of a city, wards of a district, or cities if no parent. Specify the search type to determine what child locations to return.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation - returns a map of UUID to name",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid search type",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<Map<UUID, String>>> getChildLocations(
            @Parameter(
                    description = "Parent location ID (null to get all cities)",
                    required = false
            )
            @RequestParam(required = false) UUID parentId,

            @Parameter(
                    description = "Type of child locations to retrieve: CITY (gets all cities), DISTRICT (gets districts of a city), WARD (gets wards of a district)",
                    required = true,
                    example = "DISTRICT"
            )
            @RequestParam Constants.SearchTypeEnum searchType
    ) {
        log.info("Getting child locations - parentId: {}, searchType: {}", parentId, searchType);

        Map<UUID, String> childLocations = locationService.findAllByParents(parentId, searchType);

        String message = parentId == null
                ? String.format("All %ss retrieved successfully", searchType.name().toLowerCase())
                : String.format("Child %ss retrieved successfully", searchType.name().toLowerCase());

        return responseFactory.successSingle(childLocations, message);
    }
}
