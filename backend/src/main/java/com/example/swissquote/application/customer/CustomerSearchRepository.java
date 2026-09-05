package com.example.swissquote.application.customer;

import java.util.List;
import java.util.UUID;

public interface CustomerSearchRepository {

    List<UUID> findCustomerIds(String query, int limit);
}
