package com.se100.bds.services.domains.property.impl;

import com.se100.bds.dtos.requests.property.CreatePropertyTypeRequest;
import com.se100.bds.dtos.requests.property.UpdatePropertyTypeRequest;
import com.se100.bds.dtos.responses.property.PropertyDetails;
import com.se100.bds.dtos.responses.property.PropertyTypeResponse;
import com.se100.bds.exceptions.NotFoundException;
import com.se100.bds.models.entities.property.Property;
import com.se100.bds.models.entities.property.PropertyType;
import com.se100.bds.models.entities.user.SaleAgent;
import com.se100.bds.models.entities.user.User;
import com.se100.bds.mappers.PropertyMapper;
import com.se100.bds.repositories.domains.property.PropertyRepository;
import com.se100.bds.repositories.domains.property.PropertyTypeRepository;
import com.se100.bds.repositories.dtos.MediaProjection;
import com.se100.bds.repositories.dtos.PropertyCardProtection;
import com.se100.bds.repositories.dtos.PropertyDetailsProjection;
import com.se100.bds.services.domains.property.PropertyService;
import com.se100.bds.services.domains.ranking.RankingService;
import com.se100.bds.services.domains.search.SearchService;
import com.se100.bds.services.domains.user.UserService;
import com.se100.bds.services.dtos.results.PropertyCard;
import com.se100.bds.services.fileupload.CloudinaryService;
import com.se100.bds.utils.Constants;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final RankingService rankingService;
    private final CloudinaryService cloudinaryService;

    @Override
    public Page<Property> getAll(Pageable pageable) {
        return propertyRepository.findAll(pageable);
    }

    @Override
    public Page<PropertyCard> getAllCardsWithFilters(List<UUID> cityIds, List<UUID> districtIds, List<UUID> wardIds,
                                                     List<UUID> propertyTypeIds, UUID ownerId, String ownerName,
                                                     List<Constants.ContributionTierEnum> ownerTier,
                                                     UUID agentId, String agentName, List<Constants.PerformanceTierEnum> agentTier,
                                                     BigDecimal minPrice, BigDecimal maxPrice, BigDecimal minArea, BigDecimal maxArea,
                                                     Integer rooms, Integer bathrooms, Integer bedrooms, Integer floors,
                                                     Constants.OrientationEnum houseOrientation, Constants.OrientationEnum balconyOrientation,
                                                     List<Constants.TransactionTypeEnum> transactionType,
                                                     List<Constants.PropertyStatusEnum> statuses, boolean topK,
                                                     Pageable pageable) {

        User currentUser = null;
        try {
            currentUser = userService.getUser();
            searchService.addSearchList(currentUser.getId(), cityIds, districtIds, wardIds, propertyTypeIds);
        } catch (Exception ignored) {
        }

        List<UUID> propertyIds = null;
        if (topK) {
            // Lấy tháng và năm hiện tại
            int currentYear = java.time.LocalDateTime.now().getYear();
            int currentMonth = java.time.LocalDateTime.now().getMonthValue();

            propertyIds = searchService.getMostSearchedPropertyIds(1000, currentYear, currentMonth);
            log.info("Found {} most searched properties", propertyIds.size());
        }

        List<String> transactionTypeStrings = null;
        if (transactionType != null && !transactionType.isEmpty()) {
            transactionTypeStrings = transactionType.stream()
                    .map(Enum::name)
                    .toList();
        }

        List<UUID> ownerIds;
        if (ownerId != null) {
            ownerIds = List.of(ownerId);
        } else {
            String searchName = ownerName != null ? ownerName : "";
            List<User> owners = userService.getAllByName(searchName);

            if (ownerTier != null && !ownerTier.isEmpty()) {
                int currentMonth = LocalDateTime.now().getMonthValue();
                int currentYear = LocalDateTime.now().getYear();

                ownerIds = owners.stream()
                        .map(User::getId)
                        .filter(id -> {
                            String tier = rankingService.getTier(id, Constants.RoleEnum.PROPERTY_OWNER, currentMonth, currentYear);
                            return tier != null && ownerTier.stream().anyMatch(filter -> filter.name().equals(tier));
                        })
                        .toList();
            } else {
                ownerIds = owners.stream()
                        .map(User::getId)
                        .toList();
            }
        }

        List<UUID> agentIds;
        if (agentId != null) {
            agentIds = List.of(agentId);
        }  else {
            String searchName = agentName != null ? agentName : "";
            List<User> agents = userService.getAllByName(searchName);
            if (agentTier != null && !agentTier.isEmpty()) {
                int currentMonth = LocalDateTime.now().getMonthValue();
                int currentYear = LocalDateTime.now().getYear();

                agentIds = agents.stream()
                        .map(User::getId)
                        .filter(id -> {
                            String tier = rankingService.getTier(id, Constants.RoleEnum.SALESAGENT, currentMonth, currentYear);
                            return tier != null && agentTier.stream().anyMatch(filter -> filter.name().equals(tier));
                        })
                        .toList();
            } else  {
                agentIds = agents.stream()
                        .map(User::getId)
                        .toList();
            }
        }

        List<String> statusStrings = null;
        if (statuses != null && !statuses.isEmpty()) {
            statusStrings = statuses.stream()
                    .map(Enum::name)
                    .toList();
        }

        Page<PropertyCardProtection> cardProtections = propertyRepository.findAllPropertyCardsWithFilter(
                pageable,
                propertyIds,
                cityIds,
                districtIds,
                wardIds,
                propertyTypeIds,
                ownerIds,
                agentIds,
                minPrice,
                maxPrice,
                minArea,
                maxArea,
                rooms,
                bathrooms,
                bedrooms,
                floors,
                houseOrientation.getValue(),
                balconyOrientation.getValue(),
                transactionTypeStrings,
                statusStrings,
                currentUser != null ? currentUser.getId() : null
        );

        Page<PropertyCard> propertyCardsPage = propertyMapper.mapToPage(cardProtections, PropertyCard.class);

        for (PropertyCard propertyCard : propertyCardsPage) {
            if (propertyCard.getOwnerId() != null) {
                propertyCard.setOwnerTier(rankingService.getCurrentTier(
                        propertyCard.getOwnerId(),
                        Constants.RoleEnum.PROPERTY_OWNER
                ));
            }
            if (propertyCard.getAgentId() != null) {
                propertyCard.setAgentTier(rankingService.getCurrentTier(
                        propertyCard.getAgentId(),
                        Constants.RoleEnum.SALESAGENT
                ));
            }
        }

        return propertyCardsPage;
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

        // Get document list
        List<com.se100.bds.repositories.dtos.DocumentProjection> documentProjections = propertyRepository.findDocumentsByPropertyId(propertyId);

        // Use mapper to convert projection to DTO
        PropertyDetails propertyDetails = propertyMapper.toPropertyDetails(projection, mediaProjections, documentProjections);

        // Set tier for owner
        if (propertyDetails.getOwner() != null && propertyDetails.getOwner().getId() != null) {
            String ownerTier = rankingService.getCurrentTier(
                    propertyDetails.getOwner().getId(),
                    Constants.RoleEnum.PROPERTY_OWNER
            );
            propertyDetails.getOwner().setTier(ownerTier);
        }

        // Set tier for agent
        if (propertyDetails.getAssignedAgent() != null && propertyDetails.getAssignedAgent().getId() != null) {
            String agentTier = rankingService.getCurrentTier(
                    propertyDetails.getAssignedAgent().getId(),
                    Constants.RoleEnum.SALESAGENT
            );
            propertyDetails.getAssignedAgent().setTier(agentTier);
        }

        return propertyDetails;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Property> getAllByUserIdAndStatus(UUID ownerId, UUID customerId, UUID salesAgentId, List<Constants.PropertyStatusEnum> statuses) {
        if (customerId == null) {
            if (salesAgentId == null) {
                return statuses == null || statuses.isEmpty() ? propertyRepository.findAllByOwner_Id(ownerId) : propertyRepository.findAllByOwner_IdAndStatusIn(ownerId, statuses);
            }
            else if (ownerId == null) {
                return statuses == null || statuses.isEmpty() ? propertyRepository.findAllByAssignedAgent_Id(salesAgentId) : propertyRepository.findAllByAssignedAgent_IdAndStatusIn(salesAgentId, statuses);
            }
            return statuses == null || statuses.isEmpty() ? propertyRepository.findAllByOwner_IdAndAssignedAgent_Id(ownerId, salesAgentId) : propertyRepository.findAllByOwner_IdAndAssignedAgent_IdAndStatusIn(ownerId, salesAgentId, statuses);
        } else {
            return propertyRepository.findAllByCustomer_IdAndStatusIn(customerId);
        }
    }

    @Override
    @Transactional
    public void assignAgentToProperty(UUID agentId, UUID propertyId) {
        SaleAgent salesAgent = userService.findSaleAgentById(agentId);
        Property assignedProperty = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new NotFoundException("Property not found with id: " + propertyId));
        assignedProperty.setAssignedAgent(salesAgent);
    }

    @Override
    @Transactional
    public PropertyTypeResponse createPropertyType(CreatePropertyTypeRequest request) throws IOException {
        // Upload avatar if provided
        String avatarUrl = null;
        if (request.getAvatar() != null) {
            avatarUrl = cloudinaryService.uploadFile(request.getAvatar(), "property-types");
        }

        // Create new PropertyType
        PropertyType propertyType = PropertyType.builder()
                .typeName(request.getTypeName())
                .avatarUrl(avatarUrl)
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        PropertyType savedPropertyType = propertyTypeRepository.save(propertyType);
        log.info("Created new property type with id: {}", savedPropertyType.getId());

        return propertyMapper.mapTo(savedPropertyType, PropertyTypeResponse.class);
    }

    @Override
    @Transactional
    public PropertyTypeResponse updatePropertyType(UpdatePropertyTypeRequest request) throws IOException {
        PropertyType propertyType = propertyTypeRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Property type not found with id: " + request.getId()));

        // Upload new avatar if provided
        String newAvatarUrl = null;
        if (request.getAvatar() != null) {
            newAvatarUrl = cloudinaryService.uploadFile(request.getAvatar(), "property-types");

            // Delete old avatar if exists
            if (propertyType.getAvatarUrl() != null) {
                try {
                    cloudinaryService.deleteFile(propertyType.getAvatarUrl());
                } catch (IOException e) {
                    log.error("Failed to delete old avatar from Cloudinary: {}", e.getMessage());
                }
            }
        }

        // Update fields only if they are not null
        if (request.getTypeName() != null) {
            propertyType.setTypeName(request.getTypeName());
        }
        if (newAvatarUrl != null) {
            propertyType.setAvatarUrl(newAvatarUrl);
        }
        if (request.getDescription() != null) {
            propertyType.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            propertyType.setIsActive(request.getIsActive());
        }

        PropertyType updatedPropertyType = propertyTypeRepository.save(propertyType);
        log.info("Updated property type with id: {}", updatedPropertyType.getId());

        return propertyMapper.mapTo(updatedPropertyType, PropertyTypeResponse.class);
    }

    @Override
    @Transactional
    public void deletePropertyType(UUID id) throws IOException {
        PropertyType propertyType = propertyTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property type not found with id: " + id));

        // Delete avatar from Cloudinary if exists
        if (propertyType.getAvatarUrl() != null) {
            try {
                cloudinaryService.deleteFile(propertyType.getAvatarUrl());
            } catch (IOException e) {
                log.error("Failed to delete avatar from Cloudinary: {}", e.getMessage());
            }
        }

        propertyTypeRepository.delete(propertyType);
        log.info("Deleted property type with id: {}", id);
    }
}