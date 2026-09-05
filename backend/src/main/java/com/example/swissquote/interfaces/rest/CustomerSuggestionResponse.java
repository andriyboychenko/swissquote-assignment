package com.example.swissquote.interfaces.rest;

import java.util.UUID;

public record CustomerSuggestionResponse(String customerId) {

    static CustomerSuggestionResponse fromCustomerId(UUID customerId) {
        return new CustomerSuggestionResponse(customerId.toString());
    }
}
