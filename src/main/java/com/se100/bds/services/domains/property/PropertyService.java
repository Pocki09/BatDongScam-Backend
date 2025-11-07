package com.se100.bds.services.domains.property;

import com.se100.bds.dtos.requests.property.CreatePropertyTypeRequest;
import com.se100.bds.dtos.requests.property.UpdatePropertyTypeRequest;
import com.se100.bds.dtos.responses.property.PropertyDetails;
import com.se100.bds.dtos.responses.property.PropertyTypeResponse;
import com.se100.bds.models.entities.property.Property;
import com.se100.bds.models.entities.property.PropertyType;
import com.se100.bds.services.dtos.results.PropertyCard;
import com.se100.bds.utils.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PropertyService {
    Page<Property> getAll(Pageable pageable);
    Page<PropertyCard> getAllCardsWithFilters(List<UUID> cityIds, List<UUID> districtIds, List<UUID> wardIds,
                                              List<UUID> propertyTypeIds, UUID ownerId, String ownerName,
                                              List<Constants.ContributionTierEnum> ownerTier,
                                              UUID agentId, String agentName,
                                              List<Constants.PerformanceTierEnum> agentTier,
                                              BigDecimal minPrice, BigDecimal maxPrice, BigDecimal minArea, BigDecimal maxArea,
                                              Integer rooms, Integer bathrooms, Integer bedrooms, Integer floors, Constants.OrientationEnum houseOrientation, Constants.OrientationEnum balconyOrientation,
                                              List<Constants.TransactionTypeEnum> transactionType, List<Constants.PropertyStatusEnum> statuses, boolean topK,
                                              Pageable pageable);
    Page<PropertyType> getAllTypes(Pageable pageable);
    PropertyDetails getPropertyDetailsById(UUID propertyId);
    List<Property> getAllByUserIdAndStatus(UUID ownerId, UUID customerId, UUID salesAgentId, List<Constants.PropertyStatusEnum> statuses);
    void assignAgentToProperty(UUID agentId, UUID propertyId);

    // PropertyType CRUD operations
    PropertyTypeResponse createPropertyType(CreatePropertyTypeRequest request) throws IOException;
    PropertyTypeResponse updatePropertyType(UpdatePropertyTypeRequest request) throws IOException;
    void deletePropertyType(UUID id) throws IOException;
}
