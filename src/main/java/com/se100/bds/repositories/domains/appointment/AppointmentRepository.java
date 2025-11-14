package com.se100.bds.repositories.domains.appointment;

import com.se100.bds.models.entities.appointment.Appointment;
import com.se100.bds.utils.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID>, JpaSpecificationExecutor<Appointment> {
    Page<Appointment> findAllByStatus(Constants.AppointmentStatusEnum status, Pageable pageable);

    @EntityGraph(attributePaths = {"property", "property.ward", "property.ward.district", "property.ward.district.city", "property.mediaList"})
    List<Appointment> findAllByCustomer_Id(UUID customerId);

    @EntityGraph(attributePaths = {"property", "property.ward", "property.ward.district", "property.ward.district.city", "property.mediaList"})
    List<Appointment> findAllByStatusAndCustomer_Id(Constants.AppointmentStatusEnum status, UUID customerId);
}
