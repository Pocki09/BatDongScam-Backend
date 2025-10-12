package com.se100.bds.entities.customer;

import com.se100.bds.entities.AbstractBaseEntity;
import com.se100.bds.entities.location.Ward;
import com.se100.bds.entities.user.Customer;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer_preferred_wards")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPreferredWard extends AbstractBaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;
}
