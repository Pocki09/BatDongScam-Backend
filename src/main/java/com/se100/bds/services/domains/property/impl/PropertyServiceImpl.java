package com.se100.bds.services.domains.property.impl;

import com.se100.bds.entities.property.Property;
import com.se100.bds.entities.user.User;
import com.se100.bds.mappers.PropertyMapper;
import com.se100.bds.repositories.domains.customer.CustomerFavoritePropertyRepository;
import com.se100.bds.repositories.domains.property.PropertyRepository;
import com.se100.bds.repositories.dtos.PropertyCardProtection;
import com.se100.bds.services.domains.customer.CustomerFavoriteService;
import com.se100.bds.services.domains.property.PropertyService;
import com.se100.bds.services.domains.user.UserService;
import com.se100.bds.services.dtos.results.PropertyCard;
import com.se100.bds.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {
    private final PropertyRepository propertyRepository;
    private final CustomerFavoriteService customerFavoriteService;
    private final PropertyMapper propertyMapper;
    private final UserService userService;

    @Override
    public Page<Property> getAll(Pageable pageable) {
        return propertyRepository.findAll(pageable);
    }

    @Override
    public Page<PropertyCard> getAllCardsWithFilters(List<UUID> cityIds, List<UUID> districtIds, List<UUID> wardIds, List<UUID> propertyTypeIds, BigDecimal minPrice, BigDecimal maxPrice, BigDecimal totalArea, Integer rooms, Integer bathrooms, Integer bedrooms, Integer floors, String houseOrientation, String balconyOrientation, String transactionType, Pageable pageable) {

        User currentUser = userService.getUser();

        Page<PropertyCardProtection> cardProtections = propertyRepository.findAllPropertyCardsWithFilter(
                pageable,
                cityIds,
                districtIds,
                wardIds,
                propertyTypeIds,
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
                currentUser != null ? currentUser.getId() : null
        );

        if (currentUser == null) {
            return propertyMapper.mapToPage(cardProtections, PropertyCard.class);
        }

        for (PropertyCardProtection card : cardProtections) {
            if (customerFavoriteService.isLike(card.getId(), currentUser.getId(), Constants.LikeTypeEnum.PROPERTY)) {
                card.setFavorite(true);
            }
        }

        return propertyMapper.mapToPage(cardProtections, PropertyCard.class);
    }
}