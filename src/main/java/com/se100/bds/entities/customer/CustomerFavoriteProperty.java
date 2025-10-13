package com.se100.bds.entities.customer;

import com.se100.bds.entities.AbstractCustomerPreferenceEntity;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "customer_favorite_properties")
@Getter
@Setter
@NoArgsConstructor
public class CustomerFavoriteProperty extends AbstractCustomerPreferenceEntity {
    public CustomerFavoriteProperty(UUID customerId, UUID refId) {
        super(customerId, refId);
    }
}
