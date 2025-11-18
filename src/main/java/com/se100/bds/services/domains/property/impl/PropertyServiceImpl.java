package com.se100.bds.services.domains.property.impl;

import com.se100.bds.dtos.responses.property.SimplePropertyCard;
import com.se100.bds.dtos.requests.property.CreatePropertyRequest;
import com.se100.bds.dtos.requests.property.CreatePropertyTypeRequest;
import com.se100.bds.dtos.requests.property.UpdatePropertyRequest;
import com.se100.bds.dtos.requests.property.UpdatePropertyStatusRequest;
import com.se100.bds.dtos.requests.property.UpdatePropertyTypeRequest;
import com.se100.bds.dtos.responses.property.PropertyDetails;
import com.se100.bds.dtos.responses.property.PropertyTypeResponse;
import com.se100.bds.exceptions.NotFoundException;
import com.se100.bds.mappers.PropertyMapper;
import com.se100.bds.models.entities.location.Ward;
import com.se100.bds.models.entities.property.Media;
import com.se100.bds.models.entities.property.Property;
import com.se100.bds.models.entities.property.PropertyType;
import com.se100.bds.models.entities.user.PropertyOwner;
import com.se100.bds.models.entities.user.SaleAgent;
import com.se100.bds.models.entities.user.User;
import com.se100.bds.repositories.domains.location.WardRepository;
import com.se100.bds.repositories.domains.property.PropertyRepository;
import com.se100.bds.repositories.domains.property.PropertyTypeRepository;
import com.se100.bds.repositories.domains.user.PropertyOwnerRepository;
import com.se100.bds.repositories.dtos.DocumentProjection;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private static final EnumSet<Constants.PropertyStatusEnum> ADMIN_STATUS_UPDATABLE = EnumSet.of(
            Constants.PropertyStatusEnum.PENDING,
            Constants.PropertyStatusEnum.APPROVED,
            Constants.PropertyStatusEnum.REJECTED,
            Constants.PropertyStatusEnum.AVAILABLE,
            Constants.PropertyStatusEnum.DELETED
    );

    private static final EnumSet<Constants.PropertyStatusEnum> OWNER_STATUS_UPDATABLE = EnumSet.of(
            Constants.PropertyStatusEnum.RENTED,
            Constants.PropertyStatusEnum.SOLD,
            Constants.PropertyStatusEnum.UNAVAILABLE
    );

    private final PropertyRepository propertyRepository;
    private final PropertyTypeRepository propertyTypeRepository;
    private final PropertyOwnerRepository propertyOwnerRepository;
    private final WardRepository wardRepository;
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
            int currentYear = LocalDateTime.now().getYear();
            int currentMonth = LocalDateTime.now().getMonthValue();

            propertyIds = searchService.getMostSearchedPropertyIds(1000, currentYear, currentMonth);
            log.info("Found {} most searched properties", propertyIds.size());
        }

        List<String> transactionTypeStrings = (transactionType != null && !transactionType.isEmpty())
                ? transactionType.stream().map(Enum::name).toList()
                : null;

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
        } else {
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
            } else {
                agentIds = agents.stream()
                        .map(User::getId)
                        .toList();
            }
        }

        List<String> statusStrings = (statuses != null && !statuses.isEmpty())
                ? statuses.stream().map(Enum::name).toList()
                : null;

        String houseOrientationValue = houseOrientation != null ? houseOrientation.getValue() : null;
        String balconyOrientationValue = balconyOrientation != null ? balconyOrientation.getValue() : null;

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
                houseOrientationValue,
                balconyOrientationValue,
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
        List<DocumentProjection> documentProjections = propertyRepository.findDocumentsByPropertyId(propertyId);

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
    public PropertyDetails createProperty(CreatePropertyRequest request, MultipartFile[] mediaFiles) {
        User currentUser = userService.getUser();
        boolean isAdmin = isAdmin(currentUser);
        boolean isOwner = isPropertyOwner(currentUser);

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You do not have permission to create properties");
        }

        PropertyOwner owner = resolveOwnerForCreate(isAdmin, request.getOwnerId(), currentUser.getId());
        PropertyType propertyType = propertyTypeRepository.findById(request.getPropertyTypeId())
                .orElseThrow(() -> new NotFoundException("Property type not found with id: " + request.getPropertyTypeId()));
        Ward ward = wardRepository.findById(request.getWardId())
                .orElseThrow(() -> new NotFoundException("Ward not found with id: " + request.getWardId()));

        BigDecimal commissionRate = Constants.DEFAULT_PROPERTY_COMMISSION_RATE;
        BigDecimal serviceFeeAmount = computeServiceFee(request.getPriceAmount(), commissionRate);

        Property property = Property.builder()
            .owner(owner)
            .propertyType(propertyType)
            .ward(ward)
            .title(request.getTitle())
            .description(request.getDescription())
            .transactionType(request.getTransactionType())
            .fullAddress(request.getFullAddress())
            .area(request.getArea())
            .rooms(request.getRooms())
            .bathrooms(request.getBathrooms())
            .floors(request.getFloors())
            .bedrooms(request.getBedrooms())
            .houseOrientation(request.getHouseOrientation())
            .balconyOrientation(request.getBalconyOrientation())
            .yearBuilt(request.getYearBuilt())
            .priceAmount(request.getPriceAmount())
            .pricePerSquareMeter(resolvePricePerSquareMeter(null, request.getPriceAmount(), request.getArea()))
            .commissionRate(commissionRate)
            .serviceFeeAmount(serviceFeeAmount)
            .serviceFeeCollectedAmount(isAdmin ? serviceFeeAmount : BigDecimal.ZERO)
            .amenities(request.getAmenities())
            .status(isAdmin ? Constants.PropertyStatusEnum.AVAILABLE : Constants.PropertyStatusEnum.PENDING)
            .viewCount(0)
            .approvedAt(isAdmin ? LocalDateTime.now() : null)
            .assignedAgent(null)
            .build();

        ensureMediaCollection(property);
        Property persisted = propertyRepository.save(property);

        addMediaFiles(persisted, mediaFiles);

        Property saved = propertyRepository.save(persisted);
        if (isAdmin) {
            log.info("Admin created property {} for owner {}", saved.getId(), owner.getId());
        } else {
            log.info("Owner {} created property {}", owner.getId(), saved.getId());
        }
        return propertyMapper.mapTo(saved, PropertyDetails.class);
    }

    @Override
    @Transactional
    public PropertyDetails updateProperty(UUID propertyId, UpdatePropertyRequest request, MultipartFile[] mediaFiles) {
        User currentUser = userService.getUser();
        boolean isAdmin = isAdmin(currentUser);
        boolean isOwner = isPropertyOwner(currentUser);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new NotFoundException("Property not found with id: " + propertyId));

        if (!isAdmin && (!isOwner || !property.getOwner().getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("You do not have permission to modify this property");
        }

        if (property.getStatus() == Constants.PropertyStatusEnum.DELETED) {
            throw new IllegalStateException("Cannot update a deleted property");
        }

        boolean priceChanged = hasPriceChanged(property.getPriceAmount(), request.getPriceAmount());
        boolean priceIncreased = isPriceIncreased(property.getPriceAmount(), request.getPriceAmount());

        PropertyOwner owner = resolveOwnerForUpdate(isAdmin, request.getOwnerId(), property.getOwner());
        PropertyType propertyType = propertyTypeRepository.findById(request.getPropertyTypeId())
                .orElseThrow(() -> new NotFoundException("Property type not found with id: " + request.getPropertyTypeId()));
        Ward ward = wardRepository.findById(request.getWardId())
                .orElseThrow(() -> new NotFoundException("Ward not found with id: " + request.getWardId()));

        BigDecimal commissionRate = Constants.DEFAULT_PROPERTY_COMMISSION_RATE;
        BigDecimal serviceFeeAmount = computeServiceFee(request.getPriceAmount(), commissionRate);

        applyPropertyChanges(property, request, owner, propertyType, ward, commissionRate, serviceFeeAmount);

        if (isAdmin) {
            property.setServiceFeeCollectedAmount(property.getServiceFeeAmount());
            property.setStatus(Constants.PropertyStatusEnum.AVAILABLE);
            if (property.getApprovedAt() == null) {
                property.setApprovedAt(LocalDateTime.now());
            }
        } else {
            reconcileStatusAfterOwnerUpdate(property, priceChanged, priceIncreased);
        }

        ensureMediaCollection(property);
        removeMediaFiles(property, request.getMediaIdsToRemove());
        addMediaFiles(property, mediaFiles);

        Property saved = propertyRepository.save(property);
        return propertyMapper.mapTo(saved, PropertyDetails.class);
    }

    @Override
    @Transactional
    public PropertyDetails updatePropertyStatus(UUID propertyId, UpdatePropertyStatusRequest request) {
        User currentUser = userService.getUser();
        boolean isAdmin = isAdmin(currentUser);
        boolean isOwner = isPropertyOwner(currentUser);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new NotFoundException("Property not found with id: " + propertyId));

        if (property.getStatus() == Constants.PropertyStatusEnum.DELETED) {
            throw new IllegalStateException("Cannot update a deleted property");
        }

        Constants.PropertyStatusEnum targetStatus = request.getStatus();

        if (isAdmin) {
            if (!ADMIN_STATUS_UPDATABLE.contains(targetStatus)) {
                throw new IllegalArgumentException("Unsupported status update: " + targetStatus);
            }

            if (targetStatus == Constants.PropertyStatusEnum.AVAILABLE && hasOutstandingServiceFee(property)) {
                throw new IllegalStateException("Cannot mark property as AVAILABLE while service fee payment is outstanding");
            }

            switch (targetStatus) {
                case APPROVED -> property.setApprovedAt(LocalDateTime.now());
                case PENDING, REJECTED -> {
                    property.setApprovedAt(null);
                    property.setAssignedAgent(null);
                }
                case AVAILABLE -> {
                    if (property.getApprovedAt() == null) {
                        property.setApprovedAt(LocalDateTime.now());
                    }
                }
                default -> {
                }
            }

            property.setStatus(targetStatus);
            Property saved = propertyRepository.save(property);
            log.info("Admin updated property {} status to {}", saved.getId(), targetStatus);
            return propertyMapper.mapTo(saved, PropertyDetails.class);
        }

        if (isOwner && property.getOwner().getId().equals(currentUser.getId())) {
            if (!OWNER_STATUS_UPDATABLE.contains(targetStatus)) {
                throw new IllegalArgumentException("Property owners can only set status to RENTED, SOLD, or UNAVAILABLE");
            }

            property.setStatus(targetStatus);
            Property saved = propertyRepository.save(property);
            log.info("Owner {} updated property {} status to {}", currentUser.getId(), saved.getId(), targetStatus);
            return propertyMapper.mapTo(saved, PropertyDetails.class);
        }

        throw new AccessDeniedException("You do not have permission to modify this property");
    }

    @Override
    @Transactional
    public void deleteProperty(UUID propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new NotFoundException("Property not found with id: " + propertyId));

        if (property.getStatus() == Constants.PropertyStatusEnum.DELETED) {
            return;
        }

        property.setStatus(Constants.PropertyStatusEnum.DELETED);
        property.setApprovedAt(null);
        property.setAssignedAgent(null);
        propertyRepository.save(property);
        log.info("Soft deleted property {}", propertyId);
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

    @Override
    public int countByAssignedAgentId(UUID agentId) {
        Long count = propertyRepository.countByAssignedAgent_Id(agentId);
        return count != null ? count.intValue() : 0;
    }

    @Override
    public boolean assignAgent(UUID agentId, UUID propertyId) {
        // Find the property
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found with id: " + propertyId));

        // If agentId is null, remove current agent
        if (agentId == null) {
            if (property.getAssignedAgent() != null) {
                property.setAssignedAgent(null);
                propertyRepository.save(property);
                log.info("Removed agent from property: {}", propertyId);
                return true;
            }
            return false; // No agent was assigned
        }

        // Find the new agent
        User agentUser = userService.findById(agentId);
        if (agentUser == null || agentUser.getSaleAgent() == null) {
            throw new IllegalArgumentException("User is not a sales agent");
        }

        // Remove old agent if exists and assign new agent
        if (property.getAssignedAgent() != null) {
            log.info("Replacing agent {} with {} for property: {}",
                    property.getAssignedAgent().getId(), agentId, propertyId);
        }

        property.setAssignedAgent(agentUser.getSaleAgent());
        propertyRepository.save(property);
        log.info("Assigned agent {} to property: {}", agentId, propertyId);

        return true;
    }

    @Override
    public Page<SimplePropertyCard> myAssignedProperties(
            Pageable pageable,
            String propertyOwnerName) {
        Page<Property> properties;
        if (propertyOwnerName != null && !propertyOwnerName.isEmpty()) {
            List<User> propOwners = userService.findAllByNameAndRole(
                    propertyOwnerName, Constants.RoleEnum.PROPERTY_OWNER
            );
            List<UUID> propOwnerIds = new ArrayList<>();
            if (propOwners != null && !propOwners.isEmpty())
                propOwnerIds = propOwners.stream().map(User::getId).toList();
            properties = propertyRepository.findAllByOwner_IdInAndAssignedAgent_Id(
                    propOwnerIds, userService.getUserId(), pageable
            );
        } else
            properties = propertyRepository.findAllByAssignedAgent_Id(
                    userService.getUserId(), pageable
            );

        Page<SimplePropertyCard> propertyCards = propertyMapper.mapToPage(properties, SimplePropertyCard.class);

        // Enrich with owner and agent tiers
        propertyCards.forEach(card -> {
            if (card.getOwnerId() != null) {
                card.setOwnerTier(rankingService.getCurrentTier(
                        card.getOwnerId(),
                        Constants.RoleEnum.PROPERTY_OWNER
                ));
            }
            if (card.getAgentId() != null) {
                card.setAgentTier(rankingService.getCurrentTier(
                        card.getAgentId(),
                        Constants.RoleEnum.SALESAGENT
                ));
            }
        });

        return propertyCards;
    }

    private boolean hasPriceChanged(BigDecimal currentPrice, BigDecimal newPrice) {
        return bigDecimalChanged(currentPrice, newPrice);
    }

    private boolean isPriceIncreased(BigDecimal currentPrice, BigDecimal newPrice) {
        if (currentPrice == null || newPrice == null) {
            return false;
        }
        return newPrice.compareTo(currentPrice) > 0;
    }

    private boolean bigDecimalChanged(BigDecimal current, BigDecimal incoming) {
        if (current == null && incoming == null) {
            return false;
        }
        if (current == null || incoming == null) {
            return true;
        }
        return current.compareTo(incoming) != 0;
    }

    private void synchronizeServiceFeeCollection(Property property) {
        BigDecimal serviceFeeAmount = property.getServiceFeeAmount();
        BigDecimal collected = property.getServiceFeeCollectedAmount();

        if (serviceFeeAmount == null) {
            property.setServiceFeeCollectedAmount(BigDecimal.ZERO);
            return;
        }

        if (collected == null || collected.compareTo(BigDecimal.ZERO) < 0) {
            property.setServiceFeeCollectedAmount(BigDecimal.ZERO);
            return;
        }

        if (collected.compareTo(serviceFeeAmount) > 0) {
            property.setServiceFeeCollectedAmount(serviceFeeAmount);
        }
    }

    private boolean hasOutstandingServiceFee(Property property) {
        BigDecimal serviceFeeAmount = property.getServiceFeeAmount();
        if (serviceFeeAmount == null || serviceFeeAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        BigDecimal collected = property.getServiceFeeCollectedAmount();
        if (collected == null) {
            return true;
        }

        return serviceFeeAmount.compareTo(collected) > 0;
    }

    private void applyPropertyChanges(Property property,
                                      CreatePropertyRequest request,
                                      PropertyOwner owner,
                                      PropertyType propertyType,
                                      Ward ward,
                                      BigDecimal commissionRate,
                                      BigDecimal serviceFeeAmount) {
        property.setOwner(owner);
        property.setPropertyType(propertyType);
        property.setWard(ward);
        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setTransactionType(request.getTransactionType());
        property.setFullAddress(request.getFullAddress());
        property.setArea(request.getArea());
        property.setRooms(request.getRooms());
        property.setBathrooms(request.getBathrooms());
        property.setFloors(request.getFloors());
        property.setBedrooms(request.getBedrooms());
        property.setHouseOrientation(request.getHouseOrientation());
        property.setBalconyOrientation(request.getBalconyOrientation());
        property.setYearBuilt(request.getYearBuilt());
        property.setPriceAmount(request.getPriceAmount());
        property.setPricePerSquareMeter(resolvePricePerSquareMeter(null, request.getPriceAmount(), request.getArea()));
        property.setCommissionRate(commissionRate);
        property.setServiceFeeAmount(serviceFeeAmount);
        synchronizeServiceFeeCollection(property);
        property.setAmenities(request.getAmenities());
    }

    private void reconcileStatusAfterOwnerUpdate(Property property,
                                                 boolean priceChanged,
                                                 boolean priceIncreased) {
        if (!priceChanged) {
            return;
        }

        reconcileStatusAfterPricingChange(property, priceChanged, priceIncreased);
    }

    private void reconcileStatusAfterPricingChange(Property property, boolean priceChanged, boolean priceIncreased) {
        if (!priceChanged) {
            return;
        }

        if (priceIncreased) {
            if (hasOutstandingServiceFee(property)) {
                Constants.PropertyStatusEnum currentStatus = property.getStatus();
                if (currentStatus == Constants.PropertyStatusEnum.AVAILABLE
                        || currentStatus == Constants.PropertyStatusEnum.APPROVED) {
                    property.setStatus(Constants.PropertyStatusEnum.APPROVED);
                }
            }
            return;
        }

        if (!hasOutstandingServiceFee(property)
                && property.getStatus() == Constants.PropertyStatusEnum.APPROVED) {
            property.setStatus(Constants.PropertyStatusEnum.AVAILABLE);
        }
    }

    private BigDecimal computeServiceFee(BigDecimal priceAmount, BigDecimal commissionRate) {
        if (priceAmount == null || commissionRate == null) {
            return BigDecimal.ZERO;
        }
        return priceAmount.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
    }

    private void addMediaFiles(Property property, MultipartFile[] mediaFiles) {
        if (mediaFiles == null || mediaFiles.length == 0) {
            return;
        }

        for (MultipartFile mediaFile : mediaFiles) {
            if (mediaFile == null || mediaFile.isEmpty()) {
                continue;
            }
            try {
                String fileUrl = cloudinaryService.uploadFile(mediaFile, buildMediaFolderPath(property.getId()));
                String mimeType = mediaFile.getContentType() != null ? mediaFile.getContentType() : "application/octet-stream";
                Media media = Media.builder()
                        .property(property)
                        .mediaType(Constants.MediaTypeEnum.IMAGE)
                        .fileName(mediaFile.getOriginalFilename() != null ? mediaFile.getOriginalFilename() : mediaFile.getName())
                        .filePath(fileUrl)
                        .mimeType(mimeType)
                        .build();
                property.getMediaList().add(media);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to upload property media", e);
            }
        }
    }

    private void removeMediaFiles(Property property, List<UUID> mediaIds) {
        if (mediaIds == null || mediaIds.isEmpty() || property.getMediaList().isEmpty()) {
            return;
        }

        Iterator<Media> iterator = property.getMediaList().iterator();
        while (iterator.hasNext()) {
            Media media = iterator.next();
            if (mediaIds.contains(media.getId())) {
                try {
                    cloudinaryService.deleteFile(media.getFilePath());
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to delete property media", e);
                }
                iterator.remove();
            }
        }
    }

    private void ensureMediaCollection(Property property) {
        if (property.getMediaList() == null) {
            property.setMediaList(new ArrayList<>());
        }
    }

    private BigDecimal resolvePricePerSquareMeter(BigDecimal providedValue, BigDecimal priceAmount, BigDecimal area) {
        if (providedValue != null && providedValue.compareTo(BigDecimal.ZERO) >= 0) {
            return providedValue;
        }
        if (priceAmount != null && area != null && area.compareTo(BigDecimal.ZERO) > 0) {
            return priceAmount.divide(area, 2, RoundingMode.HALF_UP);
        }
        return null;
    }

    private String buildMediaFolderPath(UUID propertyId) {
        return "properties/" + propertyId;
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRole() == Constants.RoleEnum.ADMIN;
    }

    private boolean isPropertyOwner(User user) {
        return user != null && user.getRole() == Constants.RoleEnum.PROPERTY_OWNER;
    }

    private PropertyOwner resolveOwnerForCreate(boolean isAdmin, UUID ownerIdFromRequest, UUID currentUserId) {
        UUID resolvedOwnerId = isAdmin ? ownerIdFromRequest : currentUserId;
        if (resolvedOwnerId == null) {
            throw new IllegalArgumentException("Owner id is required");
        }
        return propertyOwnerRepository.findById(resolvedOwnerId)
                .orElseThrow(() -> new NotFoundException("Property owner not found with id: " + resolvedOwnerId));
    }

    private PropertyOwner resolveOwnerForUpdate(boolean isAdmin, UUID ownerIdFromRequest, PropertyOwner currentOwner) {
        if (!isAdmin) {
            return currentOwner;
        }
        if (ownerIdFromRequest == null || Objects.equals(ownerIdFromRequest, currentOwner.getId())) {
            return currentOwner;
        }
        return propertyOwnerRepository.findById(ownerIdFromRequest)
                .orElseThrow(() -> new NotFoundException("Property owner not found with id: " + ownerIdFromRequest));
    }
}
