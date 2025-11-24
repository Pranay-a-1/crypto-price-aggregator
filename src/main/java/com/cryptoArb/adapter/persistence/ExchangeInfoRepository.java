package com.cryptoArb.adapter.persistence;

import com.cryptoArb.domain_spring.ExchangeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeInfoRepository extends JpaRepository<ExchangeInfo, String> {
}
