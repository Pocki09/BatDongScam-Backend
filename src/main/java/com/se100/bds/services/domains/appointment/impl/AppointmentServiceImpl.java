package com.se100.bds.services.domains.appointment.impl;

import com.se100.bds.dtos.responses.appointment.ViewingCardDto;
import com.se100.bds.dtos.responses.appointment.ViewingDetails;
import com.se100.bds.dtos.responses.appointment.ViewingDetailsAdmin;
import com.se100.bds.dtos.responses.appointment.ViewingListItemDto;
import com.se100.bds.dtos.responses.user.simple.PropertyOwnerSimpleCard;
import com.se100.bds.dtos.responses.user.simple.SalesAgentSimpleCard;
import com.se100.bds.mappers.AppointmentMapper;
import com.se100.bds.models.entities.AbstractBaseEntity;
import com.se100.bds.models.entities.appointment.Appointment;
import com.se100.bds.models.entities.document.IdentificationDocument;
import com.se100.bds.models.entities.property.Media;
import com.se100.bds.models.entities.user.User;
import com.se100.bds.models.schemas.ranking.IndividualSalesAgentPerformanceCareer;
import com.se100.bds.models.schemas.ranking.IndividualSalesAgentPerformanceMonth;
import com.se100.bds.repositories.domains.appointment.AppointmentRepository;
import com.se100.bds.services.domains.appointment.AppointmentService;
import com.se100.bds.services.domains.ranking.RankingService;
import com.se100.bds.services.domains.user.UserService;
import com.se100.bds.utils.Constants;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final RankingService rankingService;
    private final AppointmentMapper appointmentMapper;

    @Override
    public Page<ViewingCardDto> myViewingCards(Pageable pageable, Constants.AppointmentStatusEnum statusEnum, Integer day, Integer month, Integer year) {
        User me = userService.getUser();
        List<Appointment> appointments;
        if (statusEnum == null) {
            appointments = appointmentRepository.findAllByCustomer_Id(me.getId());
        } else {
            appointments = appointmentRepository.findAllByStatusAndCustomer_Id(statusEnum, me.getId());
        }

        List<ViewingCardDto> viewingCardDtos = appointments.stream().filter(appointment -> {
            LocalDateTime requestedTime = appointment.getRequestedDate();
            if (year != null && requestedTime.getYear() != year) {
                return false;
            }
            if (month != null && requestedTime.getMonthValue() != month) {
                return false;
            }
            if (day != null && requestedTime.getDayOfMonth() != day) {
                return false;
            }
            return true;
        })

        .map(appointment -> {

            String thumbnailUrl = appointment.getProperty().getMediaList().get(0).getFilePath();
            String districtName = appointment.getProperty().getWard().getDistrict().getDistrictName();
            String cityName = appointment.getProperty().getWard().getDistrict().getCity().getCityName();

            ViewingCardDto viewingCardDto = appointmentMapper.mapTo(appointment, ViewingCardDto.class);

            viewingCardDto.setTitle(appointment.getProperty().getTitle());
            viewingCardDto.setThumbnailUrl(thumbnailUrl);
            viewingCardDto.setDistrictName(districtName);
            viewingCardDto.setCityName(cityName);
            viewingCardDto.setPriceAmount(appointment.getProperty().getPriceAmount());
            viewingCardDto.setArea(appointment.getProperty().getArea());

            return viewingCardDto;
        }
        ).collect(Collectors.toList());

        return new PageImpl<>(viewingCardDtos, pageable, viewingCardDtos.size());
    }

    @Override
    public ViewingDetails getViewingDetails(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found with id"));

        ViewingDetails viewingDetails = appointmentMapper.mapTo(appointment, ViewingDetails.class);

        List<String> imagesList = appointment.getProperty().getMediaList().stream()
                .map(Media::getFilePath)
                .collect(Collectors.toList());
        String fullAddress = appointment.getProperty().getFullAddress();
        List<String> documentList = appointment.getProperty().getDocuments().stream()
                .map(IdentificationDocument::getFilePath)
                .collect(Collectors.toList());

        viewingDetails.setImagesList(imagesList);
        viewingDetails.setImages(imagesList.size());
        viewingDetails.setAttachedDocuments(documentList);
        viewingDetails.setFullAddress(fullAddress);

        String ownerTier = rankingService.getCurrentTier(
                appointment.getProperty().getOwner().getId(),
                Constants.RoleEnum.PROPERTY_OWNER
        );
        PropertyOwnerSimpleCard ownerCard = appointmentMapper.buildOwnerCard(
                appointment.getProperty().getOwner(),
                ownerTier
        );
        viewingDetails.setPropertyOwner(ownerCard);

        IndividualSalesAgentPerformanceMonth agentRanking = rankingService.getSaleAgentCurrentMonth(
                appointment.getAgent().getId()
        );

        IndividualSalesAgentPerformanceCareer agentRankingCareer = rankingService.getSaleAgentCareer(
                appointment.getAgent().getId()
        );

        SalesAgentSimpleCard agentCard = appointmentMapper.buildAgentCard(
                appointment.getAgent(),
                agentRanking.getPerformanceTier().getValue(),
                agentRankingCareer.getAvgRating().doubleValue(),
                agentRankingCareer.getTotalRates()
        );
        viewingDetails.setSalesAgent(agentCard);

        return viewingDetails;
    }

    @Override
    public Page<ViewingListItemDto> getViewingListItems(
            Pageable pageable,
            String propertyName, List<UUID> propertyTypeIds,
            List<Constants.TransactionTypeEnum> transactionTypeEnums,
            String agentName, List<Constants.PerformanceTierEnum> agentTiers,
            String customerName, List<Constants.CustomerTierEnum> customerTiers,
            LocalDateTime requestDateFrom, LocalDateTime requestDateTo,
            Short minRating, Short maxRating,
            List<UUID> cityIds, List<UUID> districtIds, List<UUID> wardIds) {

        List<User> agents = userService.findAllByNameAndRole(agentName, Constants.RoleEnum.SALESAGENT);
        List<UUID> agentWithMatchedTiers = new ArrayList<>();
        if (agentTiers != null && !agentTiers.isEmpty()) {
            for (User agent : agents) {
                Constants.PerformanceTierEnum agentTier = rankingService.getSaleAgentCurrentMonth(agent.getId()).getPerformanceTier();
                for (Constants.PerformanceTierEnum desiredTier: agentTiers) {
                    if (desiredTier.equals(agentTier)) {
                        agentWithMatchedTiers.add(agent.getId());
                        break;
                    }
                }
            }
        } else {
            agentWithMatchedTiers = agents.stream()
                    .map(AbstractBaseEntity::getId)
                    .collect(Collectors.toList());
        }

        List<User> customers = userService.findAllByNameAndRole(customerName, Constants.RoleEnum.CUSTOMER);
        List<UUID> customerWithMatchedTiers = new ArrayList<>();
        if (customerTiers != null && !customerTiers.isEmpty()) {
            for (User customer : customers) {
                Constants.CustomerTierEnum customerTier = rankingService.getCustomerCurrentMonth(customer.getId()).getCustomerTier();
                for (Constants.CustomerTierEnum desiredTier: customerTiers) {
                    if (desiredTier.equals(customerTier)) {
                        customerWithMatchedTiers.add(customer.getId());
                        break;
                    }
                }
            }
        } else {
            customerWithMatchedTiers = customers.stream()
                    .map(AbstractBaseEntity::getId)
                    .collect(Collectors.toList());
        }

        List<Appointment> appointments = appointmentRepository.findAllWithFilter(
                propertyName, propertyTypeIds,
                transactionTypeEnums,
                agentWithMatchedTiers,
                customerWithMatchedTiers,
                minRating, maxRating,
                cityIds, districtIds, wardIds
        );

        List<Appointment> finalAppointments = new ArrayList<>();
        for  (Appointment appointment : appointments) {
            if (requestDateFrom != null && appointment.getRequestedDate().isBefore(requestDateFrom))
                continue;
            if (requestDateTo != null && appointment.getRequestedDate().isAfter(requestDateTo))
                continue;
            finalAppointments.add(appointment);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), finalAppointments.size());
        List<Appointment> pagedAppointments = finalAppointments.subList(start, end);

        // Map appointments to ViewingListItemDto with enriched data
        List<ViewingListItemDto> viewingListItems = pagedAppointments.stream()
                .map(appointment -> {
                    // Map basic fields using the mapper
                    ViewingListItemDto dto = appointmentMapper.mapTo(appointment, ViewingListItemDto.class);

                    // Get customer tier
                    String customerTier = rankingService.getCurrentTier(
                            appointment.getCustomer().getId(),
                            Constants.RoleEnum.CUSTOMER
                    );

                    // Get sales agent tier
                    String agentTier = rankingService.getSaleAgentCurrentMonth(
                            appointment.getAgent().getId()
                    ).getPerformanceTier().getValue();

                    // Enrich with customer, agent, and thumbnail data
                    appointmentMapper.enrichViewingListItem(dto, appointment, customerTier, agentTier);

                    return dto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(viewingListItems, pageable, finalAppointments.size());
    }

    @Override
    public ViewingDetailsAdmin getViewingDetailsAdmin(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found with id"));

        ViewingDetailsAdmin viewingDetails = appointmentMapper.mapTo(appointment, ViewingDetailsAdmin.class);

        // Build property card
        ViewingDetailsAdmin.PropertyCard propertyCard = appointmentMapper.buildPropertyCard(appointment);
        viewingDetails.setPropertyCard(propertyCard);

        // Build customer card with tier
        String customerTier = rankingService.getCurrentTier(
                appointment.getCustomer().getId(),
                Constants.RoleEnum.CUSTOMER
        );
        ViewingDetailsAdmin.UserSimpleCard customerCard = appointmentMapper.buildUserSimpleCard(
                appointment.getCustomer().getUser(),
                customerTier
        );
        viewingDetails.setCustomer(customerCard);

        // Build property owner card with tier
        String ownerTier = rankingService.getCurrentTier(
                appointment.getProperty().getOwner().getId(),
                Constants.RoleEnum.PROPERTY_OWNER
        );
        ViewingDetailsAdmin.UserSimpleCard ownerCard = appointmentMapper.buildUserSimpleCard(
                appointment.getProperty().getOwner().getUser(),
                ownerTier
        );
        viewingDetails.setPropertyOwner(ownerCard);

        // Build sales agent card with tier and rating
        IndividualSalesAgentPerformanceMonth agentRanking = rankingService.getSaleAgentCurrentMonth(
                appointment.getAgent().getId()
        );
        IndividualSalesAgentPerformanceCareer agentRankingCareer = rankingService.getSaleAgentCareer(
                appointment.getAgent().getId()
        );
        ViewingDetailsAdmin.SalesAgentSimpleCard agentCard = appointmentMapper.buildSalesAgentSimpleCard(
                appointment.getAgent().getUser(),
                agentRanking.getPerformanceTier().getValue(),
                agentRankingCareer.getAvgRating().doubleValue(),
                agentRankingCareer.getTotalRates()
        );
        viewingDetails.setSalesAgent(agentCard);

        return viewingDetails;
    }
}
