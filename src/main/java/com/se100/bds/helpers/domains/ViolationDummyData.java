package com.se100.bds.helpers.domains;

import com.se100.bds.entities.property.Property;
import com.se100.bds.entities.user.User;
import com.se100.bds.entities.violation.ViolationReport;
import com.se100.bds.repositories.property.PropertyRepository;
import com.se100.bds.repositories.user.UserRepository;
import com.se100.bds.repositories.violation.ViolationRepository;
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
public class ViolationDummyData {

    private final ViolationRepository violationRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final Random random = new Random();

    public void createDummy() {
        createDummyViolations();
    }

    private void createDummyViolations() {
        log.info("Creating dummy violation reports");

        List<User> users = userRepository.findAll();
        List<Property> properties = propertyRepository.findAll();

        if (users.isEmpty()) {
            log.warn("Cannot create violations - no users found");
            return;
        }

        List<ViolationReport> violations = new ArrayList<>();

        // Create 20 violation reports (small number, as violations should be rare)
        for (int i = 0; i < 20; i++) {
            User user = users.get(random.nextInt(users.size()));
            Property property = !properties.isEmpty() && random.nextBoolean()
                    ? properties.get(random.nextInt(properties.size()))
                    : null;

            String[] violationTypes = {
                    "Fraudulent Listing",
                    "Misrepresentation of Property",
                    "Spam or Duplicate Listing",
                    "Inappropriate Content",
                    "Non-compliance with Terms",
                    "Failure to Disclose Information"
            };

            String[] severities = {"MINOR", "MODERATE", "SERIOUS", "CRITICAL"};
            String[] statuses = {"REPORTED", "UNDER_REVIEW", "RESOLVED", "DISMISSED"};

            String violationType = violationTypes[random.nextInt(violationTypes.length)];
            String severity = severities[random.nextInt(severities.length)];
            String status = statuses[random.nextInt(statuses.length)];

            ViolationReport violation = ViolationReport.builder()
                    .user(user)
                    .property(property)
                    .violationType(violationType)
                    .description(generateViolationDescription(violationType))
                    .severity(severity)
                    .status(status)
                    .resolutionNotes(status.equals("RESOLVED") ? generateResolutionNotes() : null)
                    .resolvedAt(status.equals("RESOLVED") ? LocalDateTime.now().minusDays(random.nextInt(30)) : null)
                    .build();

            violations.add(violation);
        }

        violationRepository.saveAll(violations);
        log.info("Saved {} violation reports to database", violations.size());
    }

    private String generateViolationDescription(String violationType) {
        switch (violationType) {
            case "Fraudulent Listing":
                return "Property listing contains false information and misleading claims.";
            case "Misrepresentation of Property":
                return "Property details do not match the actual condition or specifications.";
            case "Spam or Duplicate Listing":
                return "Multiple identical listings posted for the same property.";
            case "Inappropriate Content":
                return "Listing contains inappropriate images or offensive language.";
            case "Non-compliance with Terms":
                return "User violated platform terms and conditions.";
            case "Failure to Disclose Information":
                return "Critical property information was not disclosed to potential buyers.";
            default:
                return "Violation of platform policies.";
        }
    }

    private String generateResolutionNotes() {
        String[] notes = {
                "Warning issued to user. Listing has been corrected.",
                "User account temporarily suspended. Violation resolved after compliance.",
                "Listing removed. User educated on platform policies.",
                "False report. No violation found after investigation.",
                "User provided correct information. Case closed."
        };
        return notes[random.nextInt(notes.length)];
    }
}
