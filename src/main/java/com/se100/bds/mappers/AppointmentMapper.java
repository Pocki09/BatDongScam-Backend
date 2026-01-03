package com.se100.bds.mappers;

import com.se100.bds.dtos.responses.appointment.BookAppointmentResponse;
import com.se100.bds.dtos.responses.appointment.ViewingDetailsCustomer;
import com.se100.bds.dtos.responses.appointment.ViewingDetailsAdmin;
import com.se100.bds.dtos.responses.appointment.ViewingListItem;
import com.se100.bds.dtos.responses.user.simple.PropertyOwnerSimpleCard;
import com.se100.bds.dtos.responses.user.simple.SalesAgentSimpleCard;
import com.se100.bds.models.entities.appointment.Appointment;
import com.se100.bds.models.entities.user.PropertyOwner;
import com.se100.bds.models.entities.user.SaleAgent;
import com.se100.bds.models.entities.user.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper extends BaseMapper {
    @Autowired
    public AppointmentMapper(ModelMapper modelMapper) {
        super(modelMapper);
    }

    @Override
    protected void configureCustomMappings() {
        // Configure mapping from Appointment to ViewingDetails
        modelMapper.typeMap(Appointment.class, ViewingDetailsCustomer.class)
            .addMappings(mapper -> {
                // Map property fields to ViewingDetails (defensively handle null property)
                mapper.map(src -> src.getProperty() != null ? src.getProperty().getTitle() : null,
                    ViewingDetailsCustomer::setTitle);
                mapper.map(src -> src.getProperty() != null ? src.getProperty().getPriceAmount() : null,
                    ViewingDetailsCustomer::setPriceAmount);
                mapper.map(src -> src.getProperty() != null ? src.getProperty().getArea() : null,
                    ViewingDetailsCustomer::setArea);
                mapper.map(src -> src.getProperty() != null ? src.getProperty().getDescription() : null,
                    ViewingDetailsCustomer::setDescription);
                mapper.map(src -> src.getProperty() != null ? src.getProperty().getRooms() : null,
                    ViewingDetailsCustomer::setRooms);
                mapper.map(src -> src.getProperty() != null ? src.getProperty().getBathrooms() : null,
                    ViewingDetailsCustomer::setBathRooms);
                mapper.map(src -> src.getProperty() != null ? src.getProperty().getBedrooms() : null,
                    ViewingDetailsCustomer::setBedRooms);
                mapper.map(src -> src.getProperty() != null ? src.getProperty().getFloors() : null,
                    ViewingDetailsCustomer::setFloors);
                mapper.map(src -> src.getProperty() != null ? src.getProperty().getHouseOrientation() : null,
                    ViewingDetailsCustomer::setHouseOrientation);
                mapper.map(src -> src.getProperty() != null ? src.getProperty().getBalconyOrientation() : null,
                    ViewingDetailsCustomer::setBalconyOrientation);
                mapper.map(Appointment::getAgentNotes, ViewingDetailsCustomer::setNotes);
                });

        // Configure mapping from Appointment to ViewingListItemDto
        modelMapper.typeMap(Appointment.class, ViewingListItem.class)
                .addMappings(mapper -> {
                    // Map basic appointment fields
                    mapper.map(Appointment::getRequestedDate, ViewingListItem::setRequestedDate);
                    mapper.map(Appointment::getStatus, ViewingListItem::setStatus);

                    // Map property fields
                        mapper.map(src -> src.getProperty() != null ? src.getProperty().getTitle() : null,
                            ViewingListItem::setPropertyName);
                        mapper.map(src -> src.getProperty() != null ? src.getProperty().getPriceAmount() : null,
                            ViewingListItem::setPrice);
                        mapper.map(src -> src.getProperty() != null ? src.getProperty().getArea() : null,
                            ViewingListItem::setArea);

                    // Map location fields
                        mapper.map(src -> src.getProperty() != null && src.getProperty().getWard() != null
                                ? src.getProperty().getWard().getWardName()
                                : null,
                            ViewingListItem::setWardName);
                        mapper.map(src -> src.getProperty() != null && src.getProperty().getWard() != null
                                && src.getProperty().getWard().getDistrict() != null
                                ? src.getProperty().getWard().getDistrict().getDistrictName()
                                : null,
                            ViewingListItem::setDistrictName);
                        mapper.map(src -> src.getProperty() != null && src.getProperty().getWard() != null
                                && src.getProperty().getWard().getDistrict() != null
                                && src.getProperty().getWard().getDistrict().getCity() != null
                                ? src.getProperty().getWard().getDistrict().getCity().getCityName()
                                : null,
                            ViewingListItem::setCityName);

                    // Skip customer and agent - will be set manually in service to avoid lazy loading issues
                    mapper.skip(ViewingListItem::setCustomerName);
                    mapper.skip(ViewingListItem::setCustomerTier);
                    mapper.skip(ViewingListItem::setSalesAgentName);
                    mapper.skip(ViewingListItem::setSalesAgentTier);
                });

        modelMapper.typeMap(Appointment.class, BookAppointmentResponse.class)
                .addMappings(mapper -> {
                    mapper.map(Appointment::getId, BookAppointmentResponse::setAppointmentId);
                        mapper.map(src -> src.getProperty() != null ? src.getProperty().getId() : null,
                            BookAppointmentResponse::setPropertyId);
                        mapper.map(src -> src.getProperty() != null ? src.getProperty().getTitle() : null,
                            BookAppointmentResponse::setPropertyTitle);
                        mapper.map(src -> src.getProperty() != null ? src.getProperty().getFullAddress() : null,
                            BookAppointmentResponse::setPropertyAddress);
                    mapper.map(Appointment::getRequestedDate, BookAppointmentResponse::setRequestedDate);
                        mapper.map(src -> src.getStatus() != null ? src.getStatus().name() : null,
                            BookAppointmentResponse::setStatus);
                    mapper.map(Appointment::getCustomerRequirements, BookAppointmentResponse::setCustomerRequirements);
                    mapper.map(src -> src.getAgent() != null ? src.getAgent().getId() : null, BookAppointmentResponse::setAgentId);
                        mapper.map(src -> src.getAgent() != null && src.getAgent().getUser() != null
                                ? src.getAgent().getUser().getFullName()
                                : null,
                            BookAppointmentResponse::setAgentName);
                    mapper.map(Appointment::getCreatedAt, BookAppointmentResponse::setCreatedAt);
                });
    }

    /**
     * Build PropertyOwnerSimpleCard from PropertyOwner entity
     */
    public PropertyOwnerSimpleCard buildOwnerCard(PropertyOwner owner, String tier) {
        return PropertyOwnerSimpleCard.builder()
                .id(owner.getId())
                .firstName(owner.getUser().getFirstName())
                .lastName(owner.getUser().getLastName())
                .phoneNumber(owner.getUser().getPhoneNumber())
                .zaloContact(owner.getUser().getZaloContact())
                .email(owner.getUser().getEmail())
                .tier(tier)
                .build();
    }

    /**
     * Build SalesAgentSimpleCard from SaleAgent entity
     */
    public SalesAgentSimpleCard buildAgentCard(SaleAgent agent, String tier, double rating, int totalRates) {
        return SalesAgentSimpleCard.builder()
                .id(agent.getId())
                .firstName(agent.getUser().getFirstName())
                .lastName(agent.getUser().getLastName())
                .phoneNumber(agent.getUser().getPhoneNumber())
                .zaloContact(agent.getUser().getZaloContact())
                .tier(tier)
                .rating(rating)
                .totalRates(totalRates)
                .build();
    }

    /**
     * I FUCKING SWEAR TO GOD I HAVE NO FUCKING IDEA WHY THIS SHIT WORKS BUT THE MAPPER DOESN'T
     * THE LAST TIME I USE MAPPER IN THIS SHIT LANGUAGE
     */
    public ViewingListItem mapToViewingListItem(Appointment appointment) {
        ViewingListItem dto = new ViewingListItem();

        // Map basic appointment fields
        dto.setId(appointment.getId());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setUpdatedAt(appointment.getUpdatedAt());
        dto.setRequestedDate(appointment.getRequestedDate());
        dto.setStatus(appointment.getStatus());

        // Map property fields (safely handle null)
        if (appointment.getProperty() != null) {
            dto.setPropertyName(appointment.getProperty().getTitle());
            dto.setPrice(appointment.getProperty().getPriceAmount());
            dto.setArea(appointment.getProperty().getArea());

            // Map location fields
            if (appointment.getProperty().getWard() != null) {
                dto.setWardName(appointment.getProperty().getWard().getWardName());

                if (appointment.getProperty().getWard().getDistrict() != null) {
                    dto.setDistrictName(appointment.getProperty().getWard().getDistrict().getDistrictName());

                    if (appointment.getProperty().getWard().getDistrict().getCity() != null) {
                        dto.setCityName(appointment.getProperty().getWard().getDistrict().getCity().getCityName());
                    }
                }
            }

            // Set thumbnail
            if (appointment.getProperty().getMediaList() != null &&
                !appointment.getProperty().getMediaList().isEmpty()) {
                dto.setThumbnailUrl(appointment.getProperty().getMediaList().get(0).getFilePath());
            }
        }

        return dto;
    }

    /**
     * Enrich ViewingListItemDto with customer and agent data
     * This must be called after the base mapping to populate lazy-loaded relationships
     */
    public void enrichViewingListItem(ViewingListItem dto, Appointment appointment, String customerTier, String agentTier) {
        if (appointment.getCustomer() != null && appointment.getCustomer().getUser() != null) {
            dto.setCustomerName(appointment.getCustomer().getUser().getFullName());
        }
        dto.setCustomerTier(customerTier);

        if (appointment.getAgent() != null && appointment.getAgent().getUser() != null) {
            dto.setSalesAgentName(appointment.getAgent().getUser().getFullName());
        }
        dto.setSalesAgentTier(agentTier);
    }

    /**
     * Build PropertyCard for ViewingDetailsAdmin
     */
    public ViewingDetailsAdmin.PropertyCard buildPropertyCard(Appointment appointment) {
        var propertyCard = new ViewingDetailsAdmin.PropertyCard();
        propertyCard.setId(appointment.getProperty().getId());
        propertyCard.setTitle(appointment.getProperty().getTitle());
        propertyCard.setTransactionType(appointment.getProperty().getTransactionType());
        propertyCard.setType(appointment.getProperty().getPropertyType().getTypeName());
        propertyCard.setFullAddress(appointment.getProperty().getFullAddress());
        propertyCard.setPrice(appointment.getProperty().getPriceAmount());
        propertyCard.setArea(appointment.getProperty().getArea());

        // Set thumbnail
        if (appointment.getProperty().getMediaList() != null && !appointment.getProperty().getMediaList().isEmpty()) {
            propertyCard.setThumbnailUrl(appointment.getProperty().getMediaList().get(0).getFilePath());
        }

        return propertyCard;
    }

    /**
     * Build UserSimpleCard for ViewingDetailsAdmin
     */
    public ViewingDetailsAdmin.UserSimpleCard buildUserSimpleCard(
            User user, String tier) {
        var userCard = new ViewingDetailsAdmin.UserSimpleCard();
        userCard.setId(user.getId());
        userCard.setFullName(user.getFullName());
        userCard.setTier(tier);
        userCard.setPhoneNumber(user.getPhoneNumber());
        userCard.setEmail(user.getEmail());
        return userCard;
    }

    /**
     * Build SalesAgentSimpleCard for ViewingDetailsAdmin
     */
    public ViewingDetailsAdmin.SalesAgentSimpleCard buildSalesAgentSimpleCard(
            User agentUser, String tier, Double rating, Integer totalRates) {
        var agentCard = new ViewingDetailsAdmin.SalesAgentSimpleCard();
        agentCard.setId(agentUser.getId());
        agentCard.setFullName(agentUser.getFullName());
        agentCard.setTier(tier);
        agentCard.setPhoneNumber(agentUser.getPhoneNumber());
        agentCard.setEmail(agentUser.getEmail());
        agentCard.setRating(rating);
        agentCard.setTotalRates(totalRates);
        return agentCard;
    }

    /**
     * Build BookAppointmentResponse using configured mappings
     */
    public BookAppointmentResponse buildBookingResponse(Appointment appointment, String message) {
        BookAppointmentResponse response = modelMapper.map(appointment, BookAppointmentResponse.class);
        response.setMessage(message);
        return response;
    }
}
