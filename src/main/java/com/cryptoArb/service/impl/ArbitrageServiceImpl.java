package com.cryptoArb.service.impl;

import com.cryptoArb.service.ArbitrageService;
import org.springframework.stereotype.Service;

/**
 * Minimal implementation of the ArbitrageService to satisfy dependency injection
 * and allow the Spring context to load.
 */
@Service // 1. Mark this as a Spring-managed bean
public class ArbitrageServiceImpl implements ArbitrageService {
    // 2. We don't even need to add any methods yet,
    // because the interface itself is empty.
    // This empty class is enough to satisfy the dependency.
}
