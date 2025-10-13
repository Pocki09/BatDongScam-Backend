package com.se100.bds.services.domains.customer;

import com.se100.bds.entities.AbstractCustomerPreferenceEntity;
import com.se100.bds.entities.customer.*;
import com.se100.bds.utils.Constants;

import java.util.UUID;
import java.util.function.BiFunction;

public class CustomerPreferenceEntityFactory {

    public static AbstractCustomerPreferenceEntity createEntity(
            Constants.LikeTypeEnum likeType,
            UUID customerId,
            UUID refId) {

        BiFunction<UUID, UUID, AbstractCustomerPreferenceEntity> constructor = switch (likeType) {
            case PROPERTY -> CustomerFavoriteProperty::new;
            case CITY -> CustomerPreferredCity::new;
            case DISTRICT -> CustomerPreferredDistrict::new;
            case WARD -> CustomerPreferredWard::new;
            case PROPERTY_TYPE -> CustomerPreferredPropertyType::new;
        };

        return constructor.apply(customerId, refId);
    }
}

