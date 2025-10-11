package com.se100.bds.entities.customer;

import com.se100.bds.entities.AbstractBaseEntity;
import com.se100.bds.entities.location.District;
import com.se100.bds.entities.user.Customer;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CustomerPreferredDistrict")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPreferredDistrict extends AbstractBaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;
}
