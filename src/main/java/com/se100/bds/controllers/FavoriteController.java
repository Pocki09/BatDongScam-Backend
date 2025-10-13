package com.se100.bds.controllers;

import com.se100.bds.controllers.base.AbstractBaseController;
import com.se100.bds.dtos.responses.SingleResponse;
import com.se100.bds.dtos.responses.error.ErrorResponse;
import com.se100.bds.services.domains.customer.CustomerFavoriteService;
import com.se100.bds.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.se100.bds.utils.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/favorites")
@Tag(name = "004. Favorite", description = "Favorite API")
@Slf4j
public class FavoriteController extends AbstractBaseController {
    private final CustomerFavoriteService customerFavoriteService;

    @PostMapping("/like")
    @Operation(
            summary = "Toggle like/unlike for a resource",
            description = "Add or remove a favorite/preference for properties, cities, districts, wards, or property types. Returns true if liked, false if unliked.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation - returns true if liked, false if unliked",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid like type or ID",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - user must be logged in",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<Boolean>> like(
            @Parameter(description = "ID of the resource to like/unlike", required = true)
            @RequestParam UUID id,

            @Parameter(
                    description = "Type of resource: PROPERTY, CITY, DISTRICT, WARD, or PROPERTY_TYPE",
                    required = true,
                    example = "PROPERTY"
            )
            @RequestParam Constants.LikeTypeEnum likeType
    ) {
        log.info("Like request received - ID: {}, Type: {}", id, likeType);

        boolean isLiked = customerFavoriteService.like(id, likeType);

        String message = isLiked
                ? String.format("%s added to favorites", likeType.name().toLowerCase())
                : String.format("%s removed from favorites", likeType.name().toLowerCase());

        return responseFactory.successSingle(isLiked, message);
    }
}
