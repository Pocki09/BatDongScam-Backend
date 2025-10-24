package com.se100.bds.data.domains;

import com.se100.bds.models.entities.appointment.Appointment;
import com.se100.bds.models.entities.property.Property;
import com.se100.bds.models.entities.user.Customer;
import com.se100.bds.models.entities.user.SaleAgent;
import com.se100.bds.repositories.domains.appointment.AppointmentRepository;
import com.se100.bds.repositories.domains.property.PropertyRepository;
import com.se100.bds.repositories.domains.user.CustomerRepository;
import com.se100.bds.repositories.domains.user.SaleAgentRepository;
import com.se100.bds.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
public class AppointmentDummyData {

    private final AppointmentRepository appointmentRepository;
    private final PropertyRepository propertyRepository;
    private final CustomerRepository customerRepository;
    private final SaleAgentRepository saleAgentRepository;

    private final Random random = new Random();

    public void createDummy() {
        createDummyAppointments();
    }

    private void createDummyAppointments() {
        log.info("Creating dummy appointments");

        List<Property> properties = propertyRepository.findAll();
        List<Customer> customers = customerRepository.findAll();
        List<SaleAgent> agents = saleAgentRepository.findAll();

        if (properties.isEmpty() || customers.isEmpty() || agents.isEmpty()) {
            log.warn("Cannot create appointments - missing required data");
            return;
        }

        List<Appointment> appointments = new ArrayList<>();

        // Create 300 appointments
        for (int i = 1; i <= 300; i++) {
            Property property = properties.get(random.nextInt(properties.size()));
            Customer customer = customers.get(random.nextInt(customers.size()));
            SaleAgent agent = agents.get(random.nextInt(agents.size()));

            LocalDateTime requestedDate = LocalDateTime.now()
                    .plusDays(random.nextInt(60) - 30); // Between 30 days ago and 30 days ahead

            Constants.AppointmentStatusEnum[] statuses = {
                    Constants.AppointmentStatusEnum.PENDING,
                    Constants.AppointmentStatusEnum.CONFIRMED,
                    Constants.AppointmentStatusEnum.COMPLETED,
                    Constants.AppointmentStatusEnum.CANCELLED
            };
            Constants.AppointmentStatusEnum status = statuses[random.nextInt(statuses.length)];

            LocalDateTime confirmedDate = null;
            if (status != Constants.AppointmentStatusEnum.PENDING) {
                confirmedDate = requestedDate.plusHours(random.nextInt(48));
            }

            String[] interestLevels = {"LOW", "MEDIUM", "HIGH", "VERY_HIGH"};

            Appointment appointment = Appointment.builder()
                    .property(property)
                    .customer(customer)
                    .agent(agent)
                    .requestedDate(requestedDate)
                    .confirmedDate(confirmedDate)
                    .status(status)
                    .customerRequirements(generateCustomerRequirements())
                    .agentNotes(status == Constants.AppointmentStatusEnum.COMPLETED ? generateAgentNotes() : null)
                    .viewingOutcome(status == Constants.AppointmentStatusEnum.COMPLETED ? generateViewingOutcome() : null)
                    .customerInterestLevel(status == Constants.AppointmentStatusEnum.COMPLETED ? interestLevels[random.nextInt(interestLevels.length)] : null)
                    .build();

            appointments.add(appointment);
        }

        appointmentRepository.saveAll(appointments);
        log.info("Saved {} appointments to database", appointments.size());
    }

    private String generateCustomerRequirements() {
        String[] requirements = {
                "Looking for a quiet neighborhood with good schools nearby",
                "Need parking space for 2 cars",
                "Prefer modern interior design",
                "Must have good natural lighting",
                "Close to public transportation",
                "Pet-friendly property required"
        };
        return requirements[random.nextInt(requirements.length)];
    }

    private String generateAgentNotes() {
        String[] notes = {
                "Customer was very interested in the property layout",
                "Customer asked about renovation possibilities",
                "Customer wants to bring family for second viewing",
                "Customer comparing with other properties in the area",
                "Customer ready to make an offer if price is negotiable"
        };
        return notes[random.nextInt(notes.length)];
    }

    private String generateViewingOutcome() {
        String[] outcomes = {
                "Customer showed strong interest, potential deal",
                "Customer liked the property but concerned about price",
                "Customer wants to think about it",
                "Customer not interested, looking for different features",
                "Customer ready to proceed with contract negotiation"
        };
        return outcomes[random.nextInt(outcomes.length)];
    }
}

