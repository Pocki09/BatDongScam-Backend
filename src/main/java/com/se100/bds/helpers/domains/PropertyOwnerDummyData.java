package com.se100.bds.helpers.domains;

import com.se100.bds.entities.user.PropertyOwner;
import com.se100.bds.entities.user.User;
import com.se100.bds.repositories.user.UserRepository;
import com.se100.bds.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PropertyOwnerDummyData {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void createDummy() {
        createDummyPropertyOwners();
    }

    private void createDummyPropertyOwners() {
        log.info("Creating dummy property owners");

        List<User> owners = new ArrayList<>();

        // Create 20 Property Owners
        for (int i = 1; i <= 20; i++) {
            User user = createUser(
                    String.format("owner%d@example.com", i),
                    String.format("092%07d", i),
                    "Owner",
                    String.format("Number%d", i),
                    String.format("%d Owner Street, District %d, Ho Chi Minh City", i, (i % 7) + 1)
            );

            PropertyOwner propertyOwner = createPropertyOwner(user, String.format("ID%09d", 100000000 + i));
            user.setPropertyOwner(propertyOwner);
            owners.add(user);
        }

        userRepository.saveAll(owners);
        log.info("Saved {} property owners to database", owners.size());
    }

    private User createUser(String email, String phoneNumber, String firstName, String lastName, String address) {
        return User.builder()
                .email(email)
                .phoneNumber(phoneNumber)
                .firstName(firstName)
                .lastName(lastName)
                .address(address)
                .password(passwordEncoder.encode("P@sswd123."))
                .role(Constants.RoleEnum.PROPERTY_OWNER)
                .status(Constants.StatusProfileEnum.ACTIVE)
                .avatarUrl(null)
                .lastLoginAt(null)
                .notifications(new ArrayList<>())
                .violations(new ArrayList<>())
                .build();
    }

    private PropertyOwner createPropertyOwner(User user, String identificationNumber) {
        return PropertyOwner.builder()
                .user(user)
                .identificationNumber(identificationNumber)
                .forRent(0)
                .forSale(0)
                .renting(0)
                .sold(0)
                .approvedAt(LocalDateTime.now().minusDays((long) (Math.random() * 365)))
                .properties(new ArrayList<>())
                .build();
    }
}

