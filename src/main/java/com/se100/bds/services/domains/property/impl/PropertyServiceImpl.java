package com.se100.bds.services.domains.property.impl;

import com.se100.bds.dtos.responses.property.PropertyDetails;
import com.se100.bds.models.entities.property.Property;
import com.se100.bds.models.entities.property.PropertyType;
import com.se100.bds.models.entities.user.User;
import com.se100.bds.mappers.PropertyMapper;
import com.se100.bds.repositories.domains.property.PropertyRepository;
import com.se100.bds.repositories.domains.property.PropertyTypeRepository;
import com.se100.bds.repositories.dtos.MediaProjection;
import com.se100.bds.repositories.dtos.PropertyCardProtection;
import com.se100.bds.repositories.dtos.PropertyDetailsProjection;
import com.se100.bds.services.domains.property.PropertyService;
import com.se100.bds.services.domains.search.SearchService;
import com.se100.bds.services.domains.user.UserService;
import com.se100.bds.services.dtos.results.PropertyCard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {
    private final PropertyRepository propertyRepository;
    private final PropertyTypeRepository propertyTypeRepository;
    private final PropertyMapper propertyMapper;
    private final UserService userService;
    private final SearchService searchService;

    @Override
    public Page<Property> getAll(Pageable pageable) {
        return propertyRepository.findAll(pageable);
    }

    @Override
    public Page<PropertyCard> getAllCardsWithFilters(List<UUID> cityIds, List<UUID> districtIds, List<UUID> wardIds,
                                                     List<UUID> propertyTypeIds, UUID ownerId,
                                                     BigDecimal minPrice, BigDecimal maxPrice, BigDecimal totalArea,
                                                     Integer rooms, Integer bathrooms, Integer bedrooms, Integer floors,
                                                     String houseOrientation, String balconyOrientation, String transactionType,
                                                     String status, boolean topK,
                                                     Pageable pageable) {

        User currentUser = null;
        try {
            currentUser = userService.getUser();
            searchService.addSearchList(currentUser.getId(), cityIds, districtIds, wardIds, propertyTypeIds);
        } catch (Exception ignored) {
        }

        List<UUID> propertyIds = null;
        if (topK) {
            // Get most searched property IDs sorted by search frequency
            // Use a large limit to get enough results for filtering
            propertyIds = searchService.getMostSearchedPropertyIds(1000);
            log.info("Found {} most searched properties", propertyIds.size());
        }

        Page<PropertyCardProtection> cardProtections = propertyRepository.findAllPropertyCardsWithFilter(
                pageable,
                propertyIds,
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
                currentUser != null ? currentUser.getId() : null
        );


        return propertyMapper.mapToPage(cardProtections, PropertyCard.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyType> getAllTypes(Pageable pageable) {
        return propertyTypeRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyDetails getPropertyDetailsById(UUID propertyId) {
        PropertyDetailsProjection projection = propertyRepository.findPropertyDetailsById(propertyId);
        if (projection == null) {
            throw new RuntimeException("Property not found with id: " + propertyId);
        }

        // Get media list
        List<MediaProjection> mediaProjections = propertyRepository.findMediaByPropertyId(propertyId);

        // Use mapper to convert projection to DTO
        return propertyMapper.toPropertyDetails(projection, mediaProjections);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Property> getAllByUserId(UUID ownerId, UUID customerId, UUID salesAgentId) {
        if (customerId == null) {
            if (salesAgentId == null) {
                return propertyRepository.findAllByOwner_Id(ownerId);
            }
            else if (ownerId == null) {
                return propertyRepository.findAllByAssignedAgent_Id(salesAgentId);
            }
            return propertyRepository.findAllByOwner_IdAndAssignedAgent_Id(ownerId, salesAgentId);
        } else {
            return propertyRepository.findAllByCustomer_Id(customerId);
        }
    }
}