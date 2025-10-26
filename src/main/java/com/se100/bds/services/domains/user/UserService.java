package com.se100.bds.services.domains.user;

import com.se100.bds.dtos.requests.auth.RegisterRequest;
import com.se100.bds.dtos.responses.user.meprofile.MeResponse;
import com.se100.bds.dtos.responses.user.otherprofile.UserProfileResponse;
import com.se100.bds.models.entities.user.User;
import com.se100.bds.securities.JwtUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindException;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User getUser();

    List<User> getAllByName(String name);

    MeResponse<?> getAccount();

    UUID getUserId();

    UserDetails loadUserById(String id);

    JwtUserDetails getPrincipal(Authentication authentication);

    Page<User> findAll(Pageable pageable);

    User findById(UUID id);

    User updateStatus(UUID id, String status);

    User findByEmail(String email);

    UserDetails loadUserByEmail(String email);

    UserProfileResponse<?> getUserProfileById(UUID id);

    User register(RegisterRequest request) throws BindException;

    void delete(String id);

    void activeteUser(String id);
}