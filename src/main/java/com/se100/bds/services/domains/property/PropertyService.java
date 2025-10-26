package com.se100.bds.services.domains.property;

import com.se100.bds.dtos.responses.property.PropertyDetails;
import com.se100.bds.models.entities.property.Property;
import com.se100.bds.models.entities.property.PropertyType;
import com.se100.bds.services.dtos.results.PropertyCard;
import com.se100.bds.utils.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PropertyService {
    Page<Property> getAll(Pageable pageable);
    Page<PropertyCard> getAllCardsWithFilters(List<UUID> cityIds, List<UUID> districtIds, List<UUID> wardIds,
                                              List<UUID> propertyTypeIds, UUID ownerId, String ownerName,
                                              List<Constants.ContributionTierEnum> ownerTier,
                                              BigDecimal minPrice, BigDecimal maxPrice, BigDecimal minArea, BigDecimal maxArea,
                                              Integer rooms, Integer bathrooms, Integer bedrooms, Integer floors, String houseOrientation, String balconyOrientation,
                                              List<Constants.TransactionTypeEnum> transactionType, List<Constants.PropertyStatusEnum> statuses, boolean topK,
                                              Pageable pageable);
    Page<PropertyType> getAllTypes(Pageable pageable);
    PropertyDetails getPropertyDetailsById(UUID propertyId);
    List<Property> getAllByUserId(UUID ownerId, UUID customerId, UUID salesAgentId);
}
