package com.example.swissquote.application.activity;

import java.util.UUID;

public class CustomerActivityNotFoundException extends RuntimeException {

    private final UUID customerId;

    public CustomerActivityNotFoundException(UUID customerId) {
        super("Customer activity was not found for customer " + customerId);
        this.customerId = customerId;
    }

    public UUID customerId() {
        return customerId;
    }
}
