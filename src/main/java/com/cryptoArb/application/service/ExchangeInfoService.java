package com.cryptoArb.application.service;

import com.cryptoArb.adapter.persistence.ExchangeInfoRepository;
import com.cryptoArb.domain_spring.ExchangeInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeInfoService {

    private final ExchangeInfoRepository exchangeInfoRepository;

    @Cacheable(value = "exchangeInfo", key = "#id")
    @Transactional(readOnly = true)
    public Optional<ExchangeInfo> getExchangeInfo(String id) {
        log.info("Fetching ExchangeInfo from database for id: {}", id);
        return exchangeInfoRepository.findById(id);
    }

    @Transactional
    @NonNull
    public ExchangeInfo saveExchangeInfo(@NonNull ExchangeInfo exchangeInfo) {
        return Objects.requireNonNull(
                exchangeInfoRepository.save(exchangeInfo),
                "Repository save returned null for exchange: " + exchangeInfo.getId());
    }
}
