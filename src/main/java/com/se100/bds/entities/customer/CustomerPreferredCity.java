package com.se100.bds.entities.customer;

import com.se100.bds.entities.AbstractCustomerPreferenceEntity;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "customer_preferred_cities")
@Getter
@Setter
@NoArgsConstructor
public class CustomerPreferredCity extends AbstractCustomerPreferenceEntity {
    public CustomerPreferredCity(UUID customerId, UUID refId) {
        super(customerId, refId);
    }
}