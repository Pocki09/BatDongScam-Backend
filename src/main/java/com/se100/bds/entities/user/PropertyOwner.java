package com.se100.bds.entities.user;

import com.se100.bds.entities.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "property_owners")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyOwner extends AbstractBaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "property_owner_id", referencedColumnName = "user_id")
    private User user;

    @Column(name = "identification_number", nullable = false, unique = true)
    private String identificationNumber;

    @Column(name = "for_rent", nullable = false)
    private int forRent;

    @Column(name = "for_sale", nullable = false)
    private int forSale;

    @Column(name = "renting", nullable = false)
    private int renting;

    @Column(name = "sold", nullable = false)
    private int sold;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.se100.bds.entities.property.Property> properties = new ArrayList<>();
}
