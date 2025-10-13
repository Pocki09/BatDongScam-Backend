package com.se100.bds.repositories.domains.customer;

import com.se100.bds.entities.AbstractCustomerPreferenceEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.UUID;

@NoRepositoryBean
public interface BaseCustomerPreferenceRepository<T extends AbstractCustomerPreferenceEntity> extends MongoRepository<T, String> {
    boolean existsByCustomerIdAndRefId(UUID customerId, UUID refId);

    List<T> findByCustomerId(UUID customerId);

    void deleteByCustomerIdAndRefId(UUID customerId, UUID refId);
}

