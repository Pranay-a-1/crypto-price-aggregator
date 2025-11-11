package com.cryptoArb.domainOld;

import java.io.Serializable;

public class Exchange_old implements Serializable {

    // Add a version ID
    private static final long serialVersionUID = 1L;

    private final String id;

    public Exchange_old(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }



}
