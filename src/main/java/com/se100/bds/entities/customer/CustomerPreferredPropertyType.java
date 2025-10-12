package com.se100.bds.entities.customer;

import com.se100.bds.entities.AbstractBaseEntity;
import com.se100.bds.entities.property.PropertyType;
import com.se100.bds.entities.user.Customer;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer_preferred_property_types")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPreferredPropertyType extends AbstractBaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_type_id", nullable = false)
    private PropertyType propertyType;
}
