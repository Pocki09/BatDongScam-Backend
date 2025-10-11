package com.se100.bds.entities.customer;

import com.se100.bds.entities.AbstractBaseEntity;
import com.se100.bds.entities.property.Property;
import com.se100.bds.entities.user.Customer;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CustomerFavoriteProperty")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFavoriteProperty extends AbstractBaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;
}
