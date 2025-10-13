package com.se100.bds.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public abstract class AbstractCustomerPreferenceEntity extends AbstractMongoBaseEntity {
    @Field("customer_id")
    private UUID customerId;

    @Field("ref_id")
    private UUID refId;

    // Constructor chung cho tất cả các entity
    public AbstractCustomerPreferenceEntity(UUID customerId, UUID refId) {
        super();
        this.customerId = customerId;
        this.refId = refId;
    }
}

