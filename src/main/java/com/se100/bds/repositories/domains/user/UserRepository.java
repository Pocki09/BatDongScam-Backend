package com.se100.bds.repositories.domains.user;

import com.se100.bds.models.entities.user.User;
import com.se100.bds.utils.Constants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phone);

    boolean existsByEmail(String email);

    List<User> findAllByRole(Constants.RoleEnum role);

    @EntityGraph(attributePaths = {"ward", "ward.district", "ward.district.city"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithLocation(@Param("id") UUID id);

    @Query("SELECT u FROM User u WHERE LOWER(CAST(CONCAT(u.lastName, ' ', u.firstName) AS string)) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))")
    List<User> findAllByFullNameIsLikeIgnoreCase(String name);
}