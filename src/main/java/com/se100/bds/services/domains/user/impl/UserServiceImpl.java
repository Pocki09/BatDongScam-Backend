package com.se100.bds.services.domains.user.impl;

import com.se100.bds.dtos.requests.auth.RegisterRequest;
import com.se100.bds.entities.user.Customer;
import com.se100.bds.entities.user.User;
import com.se100.bds.exceptions.NotFoundException;
import com.se100.bds.repositories.domains.user.UserRepository;
import com.se100.bds.securities.JwtUserDetails;
import com.se100.bds.services.MessageSourceService;
import com.se100.bds.services.domains.user.UserService;
import com.se100.bds.utils.Constants;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSourceService messageSourceService;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            MessageSourceService messageSourceService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageSourceService = messageSourceService;
    }

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser() {
        Authentication authentication = getAuthentication();
        if (authentication.isAuthenticated()) {
            try {
                return findById(UUID.fromString(getPrincipal(authentication).getId()));
            } catch (ClassCastException e) {
                log.warn("[JWT] User details not found!");
                throw new BadCredentialsException("Bad credentials");
            }
        } else {
            log.warn("[JWT] User not authenticated!");
            throw new BadCredentialsException("Bad credentials");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UUID getUserId() {
        return getUser().getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional
    public User updateStatus(UUID id, String status) {
        User user = findById(id);
        try {
            user.setStatus(Constants.StatusProfileEnum.valueOf(status.toUpperCase()));
            return userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException(messageSourceService.get("invalid_status"));
        }
    }

    /**
     * Find a user by email.
     *
     * @param email String.
     * @return User
     */
    @Transactional(readOnly = true)
    public User findByEmail(final String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(messageSourceService.get("not_found_with_param",
                        new String[]{messageSourceService.get("user")})));
    }

    /**
     * Load user details by username.
     *
     * @param email String
     * @return UserDetails
     * @throws UsernameNotFoundException email not found exception.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(final String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(messageSourceService.get("not_found_with_param",
                        new String[]{messageSourceService.get("user")})));

        return JwtUserDetails.create(user);
    }

    /**
     * Loads user details by UUID string.
     *
     * @param id String
     * @return UserDetails
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserById(final String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException(messageSourceService.get("not_found_with_param",
                        new String[]{messageSourceService.get("user")})));

        return JwtUserDetails.create(user);
    }

    /**
     * Get UserDetails from security context.
     *
     * @param authentication Wrapper for security context
     * @return the Principal being authenticated or the authenticated principal after authentication.
     */
    @Override
    @Transactional(readOnly = true)
    public JwtUserDetails getPrincipal(final Authentication authentication) {
        return (JwtUserDetails) authentication.getPrincipal();
    }

    @Override
    @Transactional
    public User register(final RegisterRequest request) throws BindException {
        if (userRepository.existsByEmail(request.getEmail())) {
            BindingResult bindingResult = new BeanPropertyBindingResult(request, "request");
            bindingResult.addError(new FieldError(bindingResult.getObjectName(), "email",
                    "Email already exists"));
            throw new BindException(bindingResult);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setRole(Constants.RoleEnum.CUSTOMER);
        user.setStatus(Constants.StatusProfileEnum.ACTIVE);

        Customer customer = new Customer();
        customer.setUser(user);
        user.setCustomer(customer);

        customer.setCustomerTier(Constants.CustomerTierEnum.BRONZE);
        customer.setCurrentMonthPurchases(0);
        customer.setTotalPurchases(0);
        customer.setLeadScore(0);
        customer.setCurrentMonthSearches(0);
        customer.setCurrentMonthViewings(0);
        customer.setCurrentMonthRentals(0);
        customer.setTotalRentals(0);
        customer.setCurrentMonthSpending(BigDecimal.ZERO);
        customer.setTotalSpending(BigDecimal.ZERO);

        userRepository.save(user);

        return user;
    }


    @Transactional
    @Override
    public void delete(String id) {
        userRepository.delete(findById(UUID.fromString(id)));
    }

    @Override
    @Transactional
    public void activeteUser(String id) {
        if (!userRepository.existsById(UUID.fromString(id))) {
            throw new NotFoundException(messageSourceService.get("not_found_with_param",
                    new String[]{messageSourceService.get("user")}));
        }
        User user = findById(UUID.fromString(id));
        user.setLastLoginAt(LocalDateTime.now());

        userRepository.save(user);
    }
}
