package com.cryptoArb.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record Exchange(String id ) {
}
