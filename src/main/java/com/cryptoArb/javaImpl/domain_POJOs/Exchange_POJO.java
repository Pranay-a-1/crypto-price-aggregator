package com.cryptoArb.javaImpl.domain_POJOs;

import java.io.Serializable;

public class Exchange_POJO implements Serializable {

    // Add a version ID
    private static final long serialVersionUID = 1L;

    private final String id;

    public Exchange_POJO(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }



}
