package com.se100.bds.controllers;

import com.se100.bds.controllers.base.AbstractBaseController;
import com.se100.bds.dtos.responses.PageResponse;
import com.se100.bds.dtos.responses.SingleResponse;
import com.se100.bds.dtos.responses.appointment.ViewingCardDto;
import com.se100.bds.dtos.responses.appointment.ViewingDetails;
import com.se100.bds.dtos.responses.appointment.ViewingDetailsAdmin;
import com.se100.bds.dtos.responses.appointment.ViewingListItemDto;
import com.se100.bds.services.domains.appointment.AppointmentService;
import com.se100.bds.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.se100.bds.utils.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/appointment")
@Tag(name = "008. Appointment/Viewing", description = "Appointment API")
@Slf4j
public class AppointmentController extends AbstractBaseController {
    private final AppointmentService appointmentService;

    @GetMapping("/viewing-cards")
    @Operation(
            summary = "Get my viewing cards",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<PageResponse<ViewingCardDto>> getMyViewingCards(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Sort direction: asc or desc")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Field to sort by")
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Constants.AppointmentStatusEnum statusEnum,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<ViewingCardDto> viewingCardDtos = appointmentService.myViewingCards(pageable, statusEnum, day, month, year);
        return responseFactory.successPage(viewingCardDtos, "My viewings retrieved successfully");
    }

    @GetMapping("/viewing-details/{id}")
    @Operation(
            summary = "Customer Get viewing details by appointment ID",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<SingleResponse<ViewingDetails>> getViewingDetails(
            @Parameter(description = "Appointment ID")
            @PathVariable UUID id) {
        ViewingDetails viewingDetails = appointmentService.getViewingDetails(id);
        return responseFactory.successSingle(viewingDetails, "Viewing details retrieved successfully");
    }

    @GetMapping("/admin/viewing-list")
    @Operation(
            summary = "Admin Get viewing list with filters",
            description = "Get paginated list of appointments with comprehensive filtering options",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<PageResponse<ViewingListItemDto>> getViewingListItems(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Sort direction: asc or desc")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Field to sort by")
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "Property name to filter by")
            @RequestParam(required = false) String propertyName,
            @Parameter(description = "Property type IDs to filter by")
            @RequestParam(required = false) List<UUID> propertyTypeIds,
            @Parameter(description = "Transaction types to filter by")
            @RequestParam(required = false) List<Constants.TransactionTypeEnum> transactionTypeEnums,
            @Parameter(description = "Agent name to filter by")
            @RequestParam(required = false) String agentName,
            @Parameter(description = "Agent performance tiers to filter by")
            @RequestParam(required = false) List<Constants.PerformanceTierEnum> agentTiers,
            @Parameter(description = "Customer name to filter by")
            @RequestParam(required = false) String customerName,
            @Parameter(description = "Customer tiers to filter by")
            @RequestParam(required = false) List<Constants.CustomerTierEnum> customerTiers,
            @Parameter(description = "Request date from (ISO format)")
            @RequestParam(required = false) LocalDateTime requestDateFrom,
            @Parameter(description = "Request date to (ISO format)")
            @RequestParam(required = false) LocalDateTime requestDateTo,
            @Parameter(description = "Minimum rating")
            @RequestParam(required = false) Short minRating,
            @Parameter(description = "Maximum rating")
            @RequestParam(required = false) Short maxRating,
            @Parameter(description = "City IDs to filter by")
            @RequestParam(required = false) List<UUID> cityIds,
            @Parameter(description = "District IDs to filter by")
            @RequestParam(required = false) List<UUID> districtIds,
            @Parameter(description = "Ward IDs to filter by")
            @RequestParam(required = false) List<UUID> wardIds) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<ViewingListItemDto> viewingListItems = appointmentService.getViewingListItems(
                pageable,
                propertyName, propertyTypeIds,
                transactionTypeEnums,
                agentName, agentTiers,
                customerName, customerTiers,
                requestDateFrom, requestDateTo,
                minRating, maxRating,
                cityIds, districtIds, wardIds
        );
        return responseFactory.successPage(viewingListItems, "Viewing list retrieved successfully");
    }

    @GetMapping("/admin/viewing-details/{id}")
    @Operation(
            summary = "Admin Get viewing details by appointment ID",
            description = "Get detailed appointment information including property, customer, owner, and agent details",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<SingleResponse<ViewingDetailsAdmin>> getViewingDetailsAdmin(
            @Parameter(description = "Appointment ID")
            @PathVariable UUID id) {
        ViewingDetailsAdmin viewingDetails = appointmentService.getViewingDetailsAdmin(id);
        return responseFactory.successSingle(viewingDetails, "Viewing details retrieved successfully");
    }
}
