package com.se100.bds.entities.search;

import com.se100.bds.entities.AbstractBaseEntity;
import com.se100.bds.entities.location.City;
import com.se100.bds.entities.location.District;
import com.se100.bds.entities.location.Ward;
import com.se100.bds.entities.property.PropertyType;
import com.se100.bds.entities.user.Customer;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SearchLog")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "search_id", nullable = false)),
})
public class SearchLog extends AbstractBaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_type_id")
    private PropertyType propertyType;
}
