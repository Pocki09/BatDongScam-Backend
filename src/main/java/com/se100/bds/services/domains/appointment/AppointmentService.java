package com.se100.bds.services.domains.appointment;

import com.se100.bds.dtos.responses.appointment.ViewingCardDto;
import com.se100.bds.dtos.responses.appointment.ViewingDetails;
import com.se100.bds.dtos.responses.appointment.ViewingDetailsAdmin;
import com.se100.bds.dtos.responses.appointment.ViewingListItemDto;
import com.se100.bds.utils.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentService {
    // USER
    Page<ViewingCardDto> myViewingCards(
            Pageable pageable,
            Constants.AppointmentStatusEnum statusEnum,
            Integer day, Integer month, Integer year
    );
    ViewingDetails getViewingDetails(UUID id);

    // ADMIN
    Page<ViewingListItemDto> getViewingListItems(
            Pageable pageable,
            String propertyName, List<UUID> propertyTypeIds,
            List<Constants.TransactionTypeEnum> transactionTypeEnums,
            String agentName, List<Constants.PerformanceTierEnum> agentTiers,
            String customerName, List<Constants.CustomerTierEnum> customerTiers,
            LocalDateTime requestDateFrom, LocalDateTime requestDateTo,
            Short minRating, Short maxRating,
            List<UUID> cityIds, List<UUID> districtIds, List<UUID> wardIds
    );
    ViewingDetailsAdmin getViewingDetailsAdmin(UUID id);
}
