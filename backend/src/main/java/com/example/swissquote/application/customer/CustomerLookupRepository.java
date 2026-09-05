package com.example.swissquote.application.customer;

import java.util.UUID;

public interface CustomerLookupRepository {

    boolean existsById(UUID customerId);
}
