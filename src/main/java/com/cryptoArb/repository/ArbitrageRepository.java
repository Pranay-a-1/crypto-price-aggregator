package com.cryptoArb.repository;

import com.cryptoArb.domain_spring.ArbitrageOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ArbitrageOpportunity_spring entity.
 *
 * By extending JpaRepository, we get a full set of CRUD methods
 * (save, findById, findAll, delete, etc.) for free.
 */
@Repository
public interface ArbitrageRepository extends JpaRepository<ArbitrageOpportunity, Long> {
    // We don't need to write any methods here yet.
    // JpaRepository provides save(), findById(), findAll(), and deleteById(), which our test uses.
}
