package com.se100.bds.mappers;

import com.se100.bds.dtos.responses.appointment.ViewingDetails;
import com.se100.bds.dtos.responses.appointment.ViewingDetailsAdmin;
import com.se100.bds.dtos.responses.appointment.ViewingListItemDto;
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
        modelMapper.typeMap(Appointment.class, ViewingDetails.class)
                .addMappings(mapper -> {
                    // Map property fields to ViewingDetails
                    mapper.map(src -> src.getProperty().getTitle(), ViewingDetails::setTitle);
                    mapper.map(src -> src.getProperty().getPriceAmount(), ViewingDetails::setPriceAmount);
                    mapper.map(src -> src.getProperty().getArea(), ViewingDetails::setArea);
                    mapper.map(src -> src.getProperty().getDescription(), ViewingDetails::setDescription);
                    mapper.map(src -> src.getProperty().getRooms(), ViewingDetails::setRooms);
                    mapper.map(src -> src.getProperty().getBathrooms(), ViewingDetails::setBathRooms);
                    mapper.map(src -> src.getProperty().getBedrooms(), ViewingDetails::setBedRooms);
                    mapper.map(src -> src.getProperty().getFloors(), ViewingDetails::setFloors);
                    mapper.map(src -> src.getProperty().getHouseOrientation(), ViewingDetails::setHouseOrientation);
                    mapper.map(src -> src.getProperty().getBalconyOrientation(), ViewingDetails::setBalconyOrientation);
                    mapper.map(Appointment::getAgentNotes, ViewingDetails::setNotes);
                });

        // Configure mapping from Appointment to ViewingListItemDto
        modelMapper.typeMap(Appointment.class, ViewingListItemDto.class)
                .addMappings(mapper -> {
                    // Map basic appointment fields
                    mapper.map(Appointment::getRequestedDate, ViewingListItemDto::setRequestedDate);
                    mapper.map(Appointment::getStatus, ViewingListItemDto::setStatus);

                    // Map property fields
                    mapper.map(src -> src.getProperty().getTitle(), ViewingListItemDto::setPropertyName);
                    mapper.map(src -> src.getProperty().getPriceAmount(), ViewingListItemDto::setPrice);
                    mapper.map(src -> src.getProperty().getArea(), ViewingListItemDto::setArea);

                    // Map location fields
                    mapper.map(src -> src.getProperty().getWard().getWardName(), ViewingListItemDto::setWardName);
                    mapper.map(src -> src.getProperty().getWard().getDistrict().getDistrictName(), ViewingListItemDto::setDistrictName);
                    mapper.map(src -> src.getProperty().getWard().getDistrict().getCity().getCityName(), ViewingListItemDto::setCityName);

                    // Skip customer and agent - will be set manually in service to avoid lazy loading issues
                    mapper.skip(ViewingListItemDto::setCustomerName);
                    mapper.skip(ViewingListItemDto::setCustomerTier);
                    mapper.skip(ViewingListItemDto::setSalesAgentName);
                    mapper.skip(ViewingListItemDto::setSalesAgentTier);
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
     * Enrich ViewingListItemDto with customer and agent data
     * This must be called after the base mapping to populate lazy-loaded relationships
     */
    public void enrichViewingListItem(ViewingListItemDto dto, Appointment appointment, String customerTier, String agentTier) {
        if (appointment.getCustomer() != null && appointment.getCustomer().getUser() != null) {
            dto.setCustomerName(appointment.getCustomer().getUser().getFullName());
        }
        dto.setCustomerTier(customerTier);

        if (appointment.getAgent() != null && appointment.getAgent().getUser() != null) {
            dto.setSalesAgentName(appointment.getAgent().getUser().getFullName());
        }
        dto.setSalesAgentTier(agentTier);

        // Set thumbnail
        if (appointment.getProperty() != null &&
            appointment.getProperty().getMediaList() != null &&
            !appointment.getProperty().getMediaList().isEmpty()) {
            dto.setThumbnailUrl(appointment.getProperty().getMediaList().get(0).getFilePath());
        }
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
}
