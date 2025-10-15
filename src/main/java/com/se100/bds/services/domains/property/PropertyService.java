package com.se100.bds.services.domains.property;

import com.se100.bds.models.entities.property.Property;
import com.se100.bds.services.dtos.results.PropertyCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PropertyService {
    Page<Property> getAll(Pageable pageable);
    Page<PropertyCard> getAllCardsWithFilters(List<UUID> cityIds, List<UUID> districtIds, List<UUID> wardIds,
                                              List<UUID> propertyTypeIds, BigDecimal minPrice, BigDecimal maxPrice, BigDecimal totalArea,
                                              Integer rooms, Integer bathrooms, Integer bedrooms, Integer floors, String houseOrientation, String balconyOrientation,
                                              String transactionType, int topK,
                                              Pageable pageable);
}
