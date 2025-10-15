package com.se100.bds.repositories.domains.appointment;

import com.se100.bds.models.entities.appointment.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID>, JpaSpecificationExecutor<Appointment> {
}
