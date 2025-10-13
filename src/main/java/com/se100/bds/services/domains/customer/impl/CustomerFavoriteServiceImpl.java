package com.se100.bds.services.domains.customer.impl;

import com.se100.bds.entities.AbstractCustomerPreferenceEntity;
import com.se100.bds.repositories.domains.customer.*;
import com.se100.bds.services.domains.customer.CustomerFavoriteService;
import com.se100.bds.services.domains.customer.CustomerPreferenceEntityFactory;
import com.se100.bds.services.domains.user.UserService;
import com.se100.bds.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerFavoriteServiceImpl implements CustomerFavoriteService {
    private final CustomerFavoritePropertyRepository customerFavoritePropertyRepository;
    private final CustomerPreferredCityRepository customerPreferredCityRepository;
    private final CustomerPreferredDistrictRepository customerPreferredDistrictRepository;
    private final CustomerPreferredPropertyTypeRepository customerPreferredPropertyTypeRepository;
    private final CustomerPreferredWardRepository customerPreferredWardRepository;
    private final UserService userService;

    private Map<Constants.LikeTypeEnum, BaseCustomerPreferenceRepository<? extends AbstractCustomerPreferenceEntity>> repositoryMap;

    private void initRepositoryMap() {
        if (repositoryMap == null) {
            repositoryMap = Map.of(
                    Constants.LikeTypeEnum.PROPERTY, customerFavoritePropertyRepository,
                    Constants.LikeTypeEnum.CITY, customerPreferredCityRepository,
                    Constants.LikeTypeEnum.DISTRICT, customerPreferredDistrictRepository,
                    Constants.LikeTypeEnum.WARD, customerPreferredWardRepository,
                    Constants.LikeTypeEnum.PROPERTY_TYPE, customerPreferredPropertyTypeRepository
            );
        }
    }

    private BaseCustomerPreferenceRepository<? extends AbstractCustomerPreferenceEntity> getRepository(Constants.LikeTypeEnum likeType) {
        initRepositoryMap();
        return repositoryMap.get(likeType);
    }

    @Override
    public boolean like(UUID id, Constants.LikeTypeEnum likeType) {
        UUID customerId = userService.getUserId();
        BaseCustomerPreferenceRepository<? extends AbstractCustomerPreferenceEntity> repository = getRepository(likeType);
        if (repository == null) {
            log.error("Invalid like type: {}", likeType);
            return false;
        }

        try {
            if (isLike(id, customerId, likeType)) {
                repository.deleteByCustomerIdAndRefId(customerId, id);
                log.info("Removed {} preference {} for customer {}", likeType, id, customerId);
                return false;
            } else {
                AbstractCustomerPreferenceEntity entity = CustomerPreferenceEntityFactory.createEntity(
                        likeType, customerId, id);
                saveEntity(repository, entity);
                log.info("Added {} preference {} for customer {}", likeType, id, customerId);
                return true;
            }
        } catch (Exception e) {
            log.error("Error processing like operation for type {} and id {}: {}", likeType, id, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isLike(UUID refId, UUID customerId, Constants.LikeTypeEnum likeType) {
        BaseCustomerPreferenceRepository<? extends AbstractCustomerPreferenceEntity> repository = getRepository(likeType);
        if (repository == null) {
            log.error("Invalid like type: {}", likeType);
            return false;
        }

        try {
            return repository.existsByCustomerIdAndRefId(customerId, refId);
        } catch (Exception e) {
            log.error("Error checking like status for type {} and id {}: {}", likeType, refId, e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractCustomerPreferenceEntity> void saveEntity(
            BaseCustomerPreferenceRepository<? extends AbstractCustomerPreferenceEntity> repository,
            AbstractCustomerPreferenceEntity entity) {
        ((BaseCustomerPreferenceRepository<T>) repository).save((T) entity);
    }
}
