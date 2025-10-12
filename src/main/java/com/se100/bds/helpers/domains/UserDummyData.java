package com.se100.bds.helpers.domains;

import com.se100.bds.entities.user.Customer;
import com.se100.bds.entities.user.SaleAgent;
import com.se100.bds.entities.user.User;
import com.se100.bds.repositories.user.UserRepository;
import com.se100.bds.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserDummyData {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

        // Create 1 Guest
        User guest = createUser(
                "guest@example.com",
                "0901234567",
                "Guest",
                "User",
                "123 Guest Street, Ho Chi Minh City",
                Constants.RoleEnum.GUEST
        );
        allUsers.add(guest);
        log.info("Created Guest user: {}", guest.getEmail());

        // Create 1 Admin
        User admin = createUser(
                "admin@example.com",
                "0901234568",
                "Admin",
                "User",
                "456 Admin Avenue, Ho Chi Minh City",
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
                    String.format("%d Agent Street, District %d, Ho Chi Minh City", i, (i % 12) + 1),
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
                    String.format("%d Customer Street, District %d, Ho Chi Minh City", i, (i % 12) + 1),
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
                           String address, Constants.RoleEnum role) {
        return User.builder()
                .email(email)
                .phoneNumber(phoneNumber)
                .firstName(firstName)
                .lastName(lastName)
                .address(address)
                .password(passwordEncoder.encode("P@sswd123."))
                .role(role)
                .status(Constants.StatusProfileEnum.ACTIVE)
                .avatarUrl(null)
                .lastLoginAt(null)
                .notifications(new ArrayList<>())
                .violations(new ArrayList<>())
                .build();
    }

    private SaleAgent createSaleAgent(User user, String employeeCode) {
        return SaleAgent.builder()
                .user(user)
                .employeeCode(employeeCode)
                .maxProperties(50)
                .hiredDate(LocalDateTime.now().minusMonths((long) (Math.random() * 24))) // Hired in last 2 years
                .currentMonthRevenue(BigDecimal.ZERO)
                .totalRevenue(BigDecimal.valueOf(Math.random() * 1000000))
                .currentMonthDeals(0)
                .totalDeals((int) (Math.random() * 50))
                .activeProperties(0)
                .performanceTier(Constants.PerformanceTierEnum.BRONZE)
                .currentMonthRanking(0)
                .careerRanking(0)
                .assignedProperties(new ArrayList<>())
                .appointments(new ArrayList<>())
                .contracts(new ArrayList<>())
                .rankings(new ArrayList<>())
                .build();
    }

    private Customer createCustomer(User user, int index) {
        // Vary customer tiers based on index
        Constants.CustomerTierEnum tier;
        if (index <= 10) {
            tier = Constants.CustomerTierEnum.PLATINUM;
        } else if (index <= 30) {
            tier = Constants.CustomerTierEnum.GOLD;
        } else if (index <= 60) {
            tier = Constants.CustomerTierEnum.SILVER;
        } else {
            tier = Constants.CustomerTierEnum.BRONZE;
        }

        return Customer.builder()
                .user(user)
                .currentMonthSpending(BigDecimal.ZERO)
                .totalSpending(BigDecimal.valueOf(Math.random() * 500000))
                .currentMonthPurchases(0)
                .totalPurchases((int) (Math.random() * 5))
                .currentMonthRentals(0)
                .totalRentals((int) (Math.random() * 10))
                .currentMonthSearches(0)
                .currentMonthViewings(0)
                .customerTier(tier)
                .leadScore((int) (Math.random() * 100))
                .appointments(new ArrayList<>())
                .contracts(new ArrayList<>())
                .customerLeads(new ArrayList<>())
                .build();
    }
}
