package com.se100.bds.models.entities.appointment;

import com.se100.bds.models.entities.AbstractBaseEntity;
import com.se100.bds.models.entities.property.Property;
import com.se100.bds.models.entities.user.Customer;
import com.se100.bds.models.entities.user.SaleAgent;
import com.se100.bds.models.entities.review.Review;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "appointment_id", nullable = false)),
})
public class Appointment extends AbstractBaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private SaleAgent agent;

    @Column(name = "requested_date", nullable = false)
    private LocalDateTime requestedDate;

    @Column(name = "confirmed_date")
    private LocalDateTime confirmedDate;

    @Column(name = "status")
    private String status;

    @Column(name = "customer_requirements", columnDefinition = "TEXT")
    private String customerRequirements;

    @Column(name = "agent_notes", columnDefinition = "TEXT")
    private String agentNotes;

    @Column(name = "viewing_outcome", columnDefinition = "TEXT")
    private String viewingOutcome;

    @Column(name = "customer_interest_level")
    private String customerInterestLevel;

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Review review;
}
