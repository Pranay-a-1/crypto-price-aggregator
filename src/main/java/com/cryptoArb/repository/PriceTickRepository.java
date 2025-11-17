package com.cryptoArb.repository;


import com.cryptoArb.domain_spring.PriceTick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the PriceTick entity.
 *
 * By extending JpaRepository, we get a full set of CRUD methods
 * (save, findById, findAll, delete, etc.) for free.
 *
 * The <PriceTick, Long> generics specify:
 * 1. PriceTick: The entity this repository manages.
 * 2. Long: The data type of the entity's primary key (@Id).
 */
@Repository // Good practice to annotate, though Spring can often infer it
public interface PriceTickRepository extends JpaRepository<PriceTick, Long> {
}
