package com.se100.bds.services.domains.user.impl;

import com.se100.bds.dtos.requests.auth.RegisterRequest;
import com.se100.bds.dtos.responses.user.meprofile.MeResponse;
import com.se100.bds.dtos.responses.user.propertyprofile.CustomerPropertyProfileResponse;
import com.se100.bds.dtos.responses.user.propertyprofile.PropertyOwnerPropertyProfileResponse;
import com.se100.bds.dtos.responses.user.otherprofile.UserProfileResponse;
import com.se100.bds.mappers.UserMapper;
import com.se100.bds.models.entities.location.Ward;
import com.se100.bds.models.entities.property.Property;
import com.se100.bds.models.entities.user.Customer;
import com.se100.bds.models.entities.user.PropertyOwner;
import com.se100.bds.models.entities.user.User;
import com.se100.bds.exceptions.NotFoundException;
import com.se100.bds.repositories.domains.location.WardRepository;
import com.se100.bds.repositories.domains.user.UserRepository;
import com.se100.bds.securities.JwtUserDetails;
import com.se100.bds.services.MessageSourceService;
import com.se100.bds.services.domains.property.PropertyService;
import com.se100.bds.services.domains.ranking.RankingService;
import com.se100.bds.services.domains.user.UserService;
import com.se100.bds.services.fileupload.CloudinaryService;
import com.se100.bds.utils.Constants;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSourceService messageSourceService;
    private final UserMapper userMapper;
    private final PropertyService propertyService;
    private final RankingService rankingService;
    private final WardRepository wardRepository;
    private final CloudinaryService cloudinaryService;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            MessageSourceService messageSourceService,
            UserMapper userMapper,
            @Lazy PropertyService propertyService,
            RankingService rankingService,
            CloudinaryService cloudinaryService,
            WardRepository wardRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageSourceService = messageSourceService;
        this.userMapper = userMapper;
        this.propertyService = propertyService;
        this.cloudinaryService = cloudinaryService;
        this.rankingService = rankingService;
        this.wardRepository = wardRepository;
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
    public List<User> getAllByName(String name) {
        return Objects.equals(name, "") || name == null ? userRepository.findAll() : userRepository.findAllByFullNameIsLikeIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public MeResponse<?> getAccount() {
        Authentication authentication = getAuthentication();
        if (authentication.isAuthenticated()) {
            try {
                User user = userRepository.findByIdWithLocation(UUID.fromString(getPrincipal(authentication).getId()))
                        .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + UUID.fromString(getPrincipal(authentication).getId())));

                @SuppressWarnings("unchecked")
                MeResponse<Object> meResponse = userMapper.mapTo(user, MeResponse.class);
                LocalDateTime now = LocalDateTime.now();
                int month = now.getMonthValue();
                int year = now.getYear();
                meResponse.setTier(rankingService.getTier(user.getId(), user.getRole(), month, year));

                switch (user.getRole()) {
                    case ADMIN -> {
                        return null;
                    }
                    case CUSTOMER -> {
                        List<Property> properties = propertyService.getAllByUserIdAndStatus(null, user.getId(), null, null);
                        int bought = 0;
                        int rent = 0;
                        int invest = 0;
                        int total = properties.size();

                        for (Property property : properties) {
                           if (property.getTransactionType() == Constants.TransactionTypeEnum.INVESTMENT) {
                               invest++;
                           }
                           if (property.getTransactionType() == Constants.TransactionTypeEnum.SALE) {
                               bought++;
                           }
                           if (property.getTransactionType() == Constants.TransactionTypeEnum.RENTAL) {
                               rent++;
                           }
                        }

                        meResponse.setProfile(
                                CustomerPropertyProfileResponse.builder()
                                        .totalListings(total)
                                        .totalBought(bought)
                                        .totalRented(rent)
                                        .totalInvested(invest)
                                        .build()
                        );
                    }
                    case SALESAGENT -> {

                    }
                    case PROPERTY_OWNER -> {
                        List<Property> properties = propertyService.getAllByUserIdAndStatus(user.getId(), null, null, null);
                        int totalSolds = 0;
                        int totalProjects = 0;
                        int totalRentals = 0;

                        for (Property property : properties) {
                            Constants.TransactionTypeEnum transactionType = property.getTransactionType();

                            if (transactionType == Constants.TransactionTypeEnum.SALE) {
                                totalSolds++;
                            }
                            else if (transactionType == Constants.TransactionTypeEnum.INVESTMENT) {
                                totalProjects++;
                            }
                            else if (transactionType == Constants.TransactionTypeEnum.RENTAL) {
                                totalRentals++;
                            }
                        }

                        meResponse.setProfile(
                                PropertyOwnerPropertyProfileResponse.builder()
                                        .totalListings(properties.size())
                                        .totalSolds(totalSolds)
                                        .totalProjects(totalProjects)
                                        .totalRentals(totalRentals)
                                        .build()
                        );
                    }
                    default -> throw new BadCredentialsException("Bad credentials");
                }

                return meResponse;
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

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse<?> getUserProfileById(UUID id) {
        User user = userRepository.findByIdWithLocation(id)
                .orElseThrow(() -> new NotFoundException(messageSourceService.get("not_found_with_param",
                        new String[]{messageSourceService.get("user")})));

        UserProfileResponse<?> userProfileResponse = userMapper.mapTo(user, UserProfileResponse.class);
        userProfileResponse.setWardId(user.getWard().getId());
        userProfileResponse.setWardName(user.getWard().getWardName());
        userProfileResponse.setDistrictId(user.getWard().getDistrict().getId());
        userProfileResponse.setDistrictName(user.getWard().getDistrict().getDistrictName());
        userProfileResponse.setCityId(user.getWard().getDistrict().getCity().getId());
        userProfileResponse.setCityName(user.getWard().getDistrict().getCity().getCityName());

        int month = LocalDateTime.now().getMonthValue();
        int year = LocalDateTime.now().getYear();

        switch (user.getRole()) {
            case ADMIN -> {
                return userProfileResponse;
            }
            case CUSTOMER -> {
                userProfileResponse.setTier(rankingService.getTier(id, Constants.RoleEnum.CUSTOMER, month, year));
                // TODO: Implement property transaction history
                return userProfileResponse;
            }
            case SALESAGENT -> {
                userProfileResponse.setTier(rankingService.getTier(id, Constants.RoleEnum.SALESAGENT, month, year));
                // TODO: Implement property transaction history
                return userProfileResponse;
            }
            case PROPERTY_OWNER -> {
                userProfileResponse.setTier(rankingService.getTier(id, Constants.RoleEnum.PROPERTY_OWNER, month, year));

                PropertyOwnerPropertyProfileResponse ownerPropertyProfileResponse = new PropertyOwnerPropertyProfileResponse();
                List<Property> properties = propertyService.getAllByUserIdAndStatus(id, null, null,
                                List.of(Constants.PropertyStatusEnum.AVAILABLE, Constants.PropertyStatusEnum.SOLD, Constants.PropertyStatusEnum.RENTED));

                int totalSolds = 0;
                int totalProjects = 0;
                int totalRentals = 0;

                for (Property property : properties) {
                    Constants.TransactionTypeEnum transactionType = property.getTransactionType();
                    Constants.PropertyStatusEnum status = property.getStatus();

                    // Count SALE transactions that are SOLD or AVAILABLE
                    if (transactionType == Constants.TransactionTypeEnum.SALE
                            && (status == Constants.PropertyStatusEnum.SOLD || status == Constants.PropertyStatusEnum.AVAILABLE)) {
                        totalSolds++;
                    }
                    // Count INVESTMENT transactions (Projects) that are AVAILABLE
                    else if (transactionType == Constants.TransactionTypeEnum.INVESTMENT
                            && status == Constants.PropertyStatusEnum.AVAILABLE) {
                        totalProjects++;
                    }
                    // Count RENTAL transactions that are RENTED or AVAILABLE
                    else if (transactionType == Constants.TransactionTypeEnum.RENTAL
                            && (status == Constants.PropertyStatusEnum.RENTED || status == Constants.PropertyStatusEnum.AVAILABLE)) {
                        totalRentals++;
                    }
                }

                ownerPropertyProfileResponse.setTotalListings(properties.size());
                ownerPropertyProfileResponse.setTotalSolds(totalSolds);
                ownerPropertyProfileResponse.setTotalProjects(totalProjects);
                ownerPropertyProfileResponse.setTotalRentals(totalRentals);

                @SuppressWarnings("unchecked")
                UserProfileResponse<PropertyOwnerPropertyProfileResponse> ownerProfileResponse =
                        (UserProfileResponse<PropertyOwnerPropertyProfileResponse>) userProfileResponse;
                ownerProfileResponse.setPropertyProfile(ownerPropertyProfileResponse);
                return ownerProfileResponse;
            }
            default -> throw new BadCredentialsException(messageSourceService.get("invalid_role"));
        }
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
    public User register(final RegisterRequest request, Constants.RoleEnum roleEnum) throws BindException, IOException {
        if (userRepository.existsByEmail(request.getEmail())) {
            BindingResult bindingResult = new BeanPropertyBindingResult(request, "request");
            bindingResult.addError(new FieldError(bindingResult.getObjectName(), "email",
                    "Email already exists"));
            throw new BindException(bindingResult);
        }

        Ward ward = null;
        if (request.getWardId() != null) {
            ward = wardRepository.findById(request.getWardId())
                    .orElseThrow(() -> new NotFoundException(messageSourceService.get("not_found_with_param",
                            new String[]{"Ward"})));
        }

        User user = userMapper.mapTo(request, User.class);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setWard(ward);
        user.setFrontIdPicturePath(cloudinaryService.uploadFile(request.getFrontIdPicture(), "Identification Picture"));
        user.setBackIdPicturePath(cloudinaryService.uploadFile(request.getBackIdPicture(), "Identification Picture"));

        if (roleEnum.equals(Constants.RoleEnum.CUSTOMER)) {
            user.setStatus(Constants.StatusProfileEnum.ACTIVE);
            user.setRole(Constants.RoleEnum.CUSTOMER);

            Customer customer = new Customer();
            customer.setUser(user);
            user.setCustomer(customer);
        } else {
            user.setStatus(Constants.StatusProfileEnum.PENDING_APPROVAL);
            user.setRole(Constants.RoleEnum.PROPERTY_OWNER);

            PropertyOwner propertyOwner = new PropertyOwner();
            propertyOwner.setUser(user);
            user.setPropertyOwner(propertyOwner);
        }

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
