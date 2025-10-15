package com.se100.bds.models.entities.review;

import com.se100.bds.models.entities.AbstractBaseEntity;
import com.se100.bds.models.entities.appointment.Appointment;
import com.se100.bds.models.entities.contract.Contract;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviews")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "review_id", nullable = false)),
})
public class Review extends AbstractBaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @Column(name = "rating", nullable = false)
    private Short rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
}
