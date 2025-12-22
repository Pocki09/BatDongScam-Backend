package com.se100.bds.data.domains;

import com.se100.bds.models.entities.location.Ward;
import com.se100.bds.models.entities.user.Customer;
import com.se100.bds.models.entities.user.SaleAgent;
import com.se100.bds.models.entities.user.User;
import com.se100.bds.repositories.domains.location.WardRepository;
import com.se100.bds.repositories.domains.user.UserRepository;
import com.se100.bds.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserDummyData {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WardRepository wardRepository;

    public void createDummy() {
        if (any()) {
            log.info("Users already exist, skipping dummy data creation");
            return;
        }
        createDummyAccount();
    }

    public boolean any() {
        long res = userRepository.count();
        List<User> users = userRepository.findAll();
        return res > 0;
    }

    private void createDummyAccount() {
        log.info("Creating dummy accounts");

        List<User> allUsers = new ArrayList<>();
        List<Ward> wards = wardRepository.findAll();
        Random random = new Random();

        // Create 1 Admin
        User admin = createUser(
                "admin@example.com",
                "0901234568",
                "Admin",
                "User",
                wards.isEmpty() ? null : wards.get(random.nextInt(wards.size())),
                Constants.RoleEnum.ADMIN
        );
        allUsers.add(admin);
        log.info("Created Admin user: {}", admin.getEmail());

        // Create 10 Sale Agents
        for (int i = 1; i <= 10; i++) {
            User user = createUser(
                    String.format("agent%d@example.com", i),
                    String.format("090123%04d", 4568 + i),
                    "Agent",
                    String.format("Number%d", i),
                    wards.isEmpty() ? null : wards.get(random.nextInt(wards.size())),
                    Constants.RoleEnum.SALESAGENT
            );

            SaleAgent saleAgent = createSaleAgent(user, String.format("SA%04d", i));
            user.setSaleAgent(saleAgent); // Set bidirectional relationship
            allUsers.add(user);
        }
        log.info("Created 10 Sale Agents");

        // Create 100 Customers
        for (int i = 1; i <= 100; i++) {
            User user = createUser(
                    String.format("customer%d@example.com", i),
                    String.format("091%07d", i),
                    "Customer",
                    String.format("Number%d", i),
                    wards.isEmpty() ? null : wards.get(random.nextInt(wards.size())),
                    Constants.RoleEnum.CUSTOMER
            );

            Customer customer = createCustomer(user, i);
            user.setCustomer(customer); // Set bidirectional relationship
            allUsers.add(user);
        }
        log.info("Created 100 Customers");

        // Save all users (cascade will save SaleAgents and Customers)
        userRepository.saveAll(allUsers);
        log.info("Saved all users to database");

        log.info("Done creating dummy accounts - Total: {} users (including {} sale agents and {} customers)",
                allUsers.size(), 10, 100);
    }

    private User createUser(String email, String phoneNumber, String firstName, String lastName,
                           Ward ward, Constants.RoleEnum role) {
        Random random = new Random();

        // Generate random identification number (9 digits)
        String identificationNumber = String.format("%09d", 100000000 + random.nextInt(900000000));

        // Generate random date of birth (age between 22 and 65 years old)
        int yearsOld = 22 + random.nextInt(44);
        LocalDate dayOfBirth = LocalDate.now().minusYears(yearsOld).minusDays(random.nextInt(365));

        // Random gender
        String gender = random.nextBoolean() ? "Male" : "Female";

        // Issue date should be after 18 years old and before now
        LocalDate issueDate = dayOfBirth.plusYears(18).plusDays(random.nextInt(365 * (yearsOld - 18)));

        // Generate avatar URL based on gender
        String avatarUrl = String.format("https://avatar.iran.liara.run/public/%s?username=%s",
                gender.toLowerCase(), email.split("@")[0]);

        return User.builder()
                .email(email)
                .phoneNumber(phoneNumber)
                .zaloContact(phoneNumber)
                .firstName(firstName)
                .lastName(lastName)
                .ward(ward)
                .password(passwordEncoder.encode("P@sswd123."))
                .role(role)
                .status(Constants.StatusProfileEnum.ACTIVE)
                .avatarUrl(avatarUrl)
                .identificationNumber(identificationNumber)
                .dayOfBirth(dayOfBirth)
                .gender(gender)
                .nation("Vietnam")
                .issueDate(issueDate)
                .issuingAuthority("Public Security Department")
                .frontIdPicturePath(String.format("/uploads/id_cards/%s_front.jpg", identificationNumber))
                .backIdPicturePath(String.format("/uploads/id_cards/%s_back.jpg", identificationNumber))
                .lastLoginAt(LocalDateTime.now().minusDays(random.nextInt(30)))
                .notifications(new ArrayList<>())
                .build();
    }

    private SaleAgent createSaleAgent(User user, String employeeCode) {
        return SaleAgent.builder()
                .user(user)
                .employeeCode(employeeCode)
                .maxProperties(50)
                .hiredDate(LocalDateTime.now().minusMonths((long) (Math.random() * 24))) // Hired in last 2 years
                .assignedProperties(new ArrayList<>())
                .appointments(new ArrayList<>())
                .contracts(new ArrayList<>())
                .build();
    }

    private Customer createCustomer(User user, int index) {
        return Customer.builder()
                .user(user)
                .appointments(new ArrayList<>())
                .contracts(new ArrayList<>())
                .build();
    }
}
